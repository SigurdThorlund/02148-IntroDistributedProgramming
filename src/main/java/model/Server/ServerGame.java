package model.Server;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import networking.Commands.ClientCommand;
import networking.Commands.ServerCommand;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import utils.Phase;
import utils.TerrainOption;

import java.util.List;
import java.util.Random;

import static networking.Commands.ClientCommand.*;
import static networking.Commands.LocalCommand.*;
import static networking.Commands.ServerCommand.*;
import static utils.Phase.*;

public class ServerGame {
    public static final int MOVEMENT_DURATION = 10;
    public static final int SHOOT_DURATION = 10;
    public static final int PAUSE_DURATION = 3;

    private SequentialSpace localCommunicationSpace;
    private PlayerData[] playerInfo;
    private SequentialSpace consoleSpace;
    private SequentialSpace gameSpace;
    private String lobbyName;
    private Phase currentPhase;
    private int msg_id = 0;
    private int playerCount;

    private Timeline serverTimer;

    public ServerGame(String lobbyName, SequentialSpace localCommunicationSpace){
        this.localCommunicationSpace = localCommunicationSpace;
        this.lobbyName = lobbyName;
    }

    public void start() {
        try {
            gameSpace = (SequentialSpace) localCommunicationSpace.query(  new ActualField(SERVER_SPACE),
                                                                        new FormalField(SequentialSpace.class)) [1];

            consoleSpace = (SequentialSpace) localCommunicationSpace.query(  new ActualField(SERVER_CONSOLE),
                                                                            new FormalField(SequentialSpace.class)) [1];

            TerrainOption terrainOption = (TerrainOption) localCommunicationSpace.get(  new ActualField(TERRAIN_OPTIONS),
                                                                            new FormalField(TerrainOption.class)) [1];

            //Read playerData
            List<Object[]> playerData = localCommunicationSpace.queryAll(new ActualField(PLAYER_MAPPING),
                    new FormalField(Integer.class),
                    new FormalField(String.class));

            playerCount = playerData.size();
            playerInfo = new PlayerData[playerCount];

            //Fill in PlayerData
            for (int i = 0; i < playerCount; i++){
                Object[] player = playerData.get(i);
                playerInfo[i] = new PlayerData((String)player[2],(int)player[1]);
            }

            //Wait for all players to join
            for (int i = 0; i < playerCount; i++)
                gameSpace.get(new ActualField(CLIENT_JOIN), new FormalField(Integer.class), new FormalField(String.class));

            //Clear space from lobby
            gameSpace.getAll(new FormalField(ServerCommand.class),
                    new FormalField(Integer.class),
                    new FormalField(Object.class),
                    new FormalField(Integer.class));

            //Give seed for terrain generation
            int gameSeed = new Random().nextInt();
            gameSpace.put(SERVER_SEED,gameSeed);
            consoleSpace.put(SERVER_TEXT_CONSOLE, lobbyName, "Started game with seed: " + gameSeed);

            //Sets locations of players
            Random random = new Random();
            for (int i = 0; i < playerCount; i++)
                gameSpace.put(SERVER_SPAWN, i, random.nextInt(terrainOption.getGameWidth()));

            //Timer for phases in game
            serverTimer = new Timeline(
                    new KeyFrame(Duration.seconds(0), event -> {
                        post(SERVER_CHANGE_PHASE, -1, COUNTDOWN);
                        currentPhase = COUNTDOWN;
                    }), //Pause
                    new KeyFrame(Duration.seconds(PAUSE_DURATION), event -> {
                        post(SERVER_CHANGE_PHASE, -1, MOVE_PHASE);
                        currentPhase = MOVE_PHASE;
                    }),//Move
                    new KeyFrame(Duration.seconds(PAUSE_DURATION + MOVEMENT_DURATION), event -> {
                        serverTimer.pause();
                        post(SERVER_CHANGE_PHASE, -1, COUNTDOWN);
                        currentPhase = COUNTDOWN;

                        try {
                            gameSpace.put(SYNC_INIT, -1, "");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }), //Pause
                    new KeyFrame(Duration.seconds(2 * PAUSE_DURATION + MOVEMENT_DURATION),event -> {
                        //All clients have synced - go to shoot phase
                        post(SERVER_CHANGE_PHASE, -1, SHOOT_PHASE);
                        currentPhase = SHOOT_PHASE;
                    }), //Shoot
                    new KeyFrame(Duration.seconds(2 * PAUSE_DURATION + MOVEMENT_DURATION + SHOOT_DURATION)));

            serverTimer.setCycleCount(Animation.INDEFINITE); //Infinite looping
            serverTimer.play();
            listen();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void listen() throws InterruptedException {
        while (true){
            //Format: (ACTION,ACTION_TYPE,USERNAME)
            Object[] playerAction = gameSpace.get(  new FormalField(ClientCommand.class),
                                                    new FormalField(Integer.class),
                                                    new FormalField(Object.class));

            ClientCommand clientCommand = (ClientCommand) playerAction[0];

            //Synchronize positions
            if (clientCommand == SYNC_INIT){
                syncPhase();
                continue;
            }

            //Handle action as normal
            int playerID = (int) playerAction[1];
            PlayerData playerData = playerInfo[playerID];

            switch (clientCommand){
                case CLIENT_SHOOT: {
                    if (!playerData.isAlive())
                        break;

                    //Discard shot when it is not shoot-phase
                    if (currentPhase != SHOOT_PHASE)
                        break;

                    post(SERVER_SHOOT,playerID,playerAction[2]);
                    consoleSpace.put(SERVER_TEXT_CONSOLE,lobbyName, playerInfo[playerID].getName() + " has shot a bullet");
                    break;
                }

                case CLIENT_MOVE: {
                    if (!playerData.isAlive())
                        break;

                    //Discard movement when it is not move-phase
                    if (currentPhase != MOVE_PHASE)
                        break;

                    post(SERVER_MOVE, playerID, playerAction[2]);
                    break;
                }

                case CLIENT_DEAD: {
                    int deadPlayerID = (int) playerAction[2];
                    PlayerData deadPlayer = playerInfo[deadPlayerID];
                    consoleSpace.put(SERVER_TEXT_CONSOLE,lobbyName,
                                        "Player " + deadPlayer.getName()
                                        + " has died, according to "
                                        + playerInfo[playerID].getName() );

                    //Have enough tanks voted that the player is dead?
                    int deathVotes = deadPlayer.incrementDeathVote();
                    if (deathVotes > playerCount / 2) {
                        deadPlayer.setAlive(false);
                        post(SERVER_DEAD, playerID, deadPlayerID);
                        consoleSpace.put(SERVER_TEXT_CONSOLE,lobbyName,  deadPlayer.getName() + " has died");

                        PlayerData winner = testVictory();
                        if (winner != null){
                            serverTimer.stop();
                            post(SERVER_VICTORY, winner.getId(),winner.getName());
                            consoleSpace.put(SERVER_TEXT_CONSOLE,lobbyName,winner.getName() + " has won the game!");
                            return;
                        }
                    }
                    break;
                }

                default: consoleSpace.put(SERVER_TEXT_CONSOLE,lobbyName,
                        "Received a ClientCommand which is unhandled at this stage, "
                        + playerAction[0]);
            }
        }
    }

    private void syncPhase(){
        for (int i = 0; i < playerCount;){
            try {
                Object[] sync_pos = gameSpace.get(  new FormalField(ClientCommand.class),
                                                    new FormalField(Integer.class),
                                                    new FormalField(Object.class));

                if (sync_pos[0] != CLIENT_SYNC_POS)
                    //Discard and empty excess tuples from clients
                    continue;

                //Notify all players of the new location
                post(SERVER_MOVE, (int) sync_pos[1],sync_pos[2]);
                i++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serverTimer.play();
    }

    //Determine whether only a single player is left
    private PlayerData testVictory(){
        PlayerData potentialWinner = null;

        for (int i = 0; i < playerCount; i++){
            PlayerData currentPlayer = playerInfo[i];

            if (currentPlayer.isAlive()){
                if (potentialWinner != null)
                    return null;
                else
                    potentialWinner = currentPlayer;
            }
        }
        return potentialWinner;
    }

    //Sends a message to each client
    private void post(ServerCommand command, int playerID, Object data){
        for (int i = 0; i < playerCount; i++){
            try {
                gameSpace.put(command,playerID,data,msg_id);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        msg_id++;
    }
}