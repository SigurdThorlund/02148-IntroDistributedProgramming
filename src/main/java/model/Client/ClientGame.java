package model.Client;

import controllers.ClientGameController;
import javafx.fxml.FXMLLoader;
import main.MainClient;
import model.Model;
import model.Projectile;
import networking.Commands.ServerCommand;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import utils.Phase;
import utils.ProjectilePacket;
import utils.Vector;

import java.util.List;

import static networking.Commands.ClientCommand.*;
import static networking.Commands.LocalCommand.*;
import static networking.Commands.ServerCommand.SERVER_SEED;
import static networking.Commands.ServerCommand.SERVER_SPAWN;

//Logic for client communication in-game
public class ClientGame implements Client{
    private MainClient mainClient;
    private SequentialSpace localCommunicationSpace;
    private RemoteSpace gameSpace;
    private int playerID;

    public ClientGame(MainClient mainClient, SequentialSpace localCommunicationSpace){
        this.mainClient = mainClient;
        this.localCommunicationSpace = localCommunicationSpace;

        FXMLLoader loader = mainClient.setScene(ClientState.GAME);
        ClientGameController cgc = loader.getController();

        try {
            gameSpace = (RemoteSpace) localCommunicationSpace.query(new ActualField(SERVER_SPACE),
                                                                                new FormalField(RemoteSpace.class))[1];
            playerID = (int) localCommunicationSpace.query(new ActualField(PLAYER_ID), new FormalField(Integer.class))[1];
            //Tell server that the player has joined
            gameSpace.put(CLIENT_JOIN, playerID, "");

            //Wait for seed
            int gameSeed = (int) gameSpace.query(new ActualField(SERVER_SEED), new FormalField(Integer.class))[1];

            //TODO: Get dimensions from localSpace
            cgc.init(Model.DEFAULT_GAME_WIDTH, Model.DEFAULT_GAME_HEIGHT);

            //Spawn players
            List<Object[]> playerMapping = localCommunicationSpace.queryAll(new ActualField(PLAYER_MAPPING),
                                                                            new FormalField(Integer.class),
                                                                            new FormalField(String.class));
            Model master = cgc.getModel();
            master.init(gameSeed, this, playerMapping.size());
            //master.sound_init();

            for (int i = 0; i < playerMapping.size(); i++){
                Object[] playerLocation = gameSpace.query(  new ActualField(SERVER_SPAWN),
                                                            new ActualField(i) ,
                                                            new FormalField(Integer.class));
                int player_x_pos = (int) playerLocation[2];
                if (i == playerID)
                    master.spawn_player(player_x_pos, playerID, playerMapping.get(i)[2].toString());
                else
                    master.spawn_tank(player_x_pos, i, playerMapping.get(i)[2].toString());
            }

            //Start drawing and listening for keystrokes
            cgc.start();
            cgc.addListener();

            new Thread(new ClientGameListener(master,localCommunicationSpace)).start();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shoot(Projectile p) {
        try {
            gameSpace.put(CLIENT_SHOOT,playerID,new ProjectilePacket(p));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void movePlayerTo(Vector position) {
        try{
            gameSpace.put(CLIENT_MOVE, playerID, position);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void syncPlayerPosition(Vector position) {
        try {
            gameSpace.put(CLIENT_SYNC_POS, playerID, position);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exit() {
        try {
            gameSpace.put(CLIENT_LEAVE,playerID,"Closed application");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void goToLobby(){
        try {
            //Cleanup local space
            localCommunicationSpace.get(new ActualField(PLAYER_ID),new FormalField(Integer.class));
            Object[] playerMapping = localCommunicationSpace.get(new ActualField(PLAYER_MAPPING), new ActualField(playerID), new FormalField(String.class));

            localCommunicationSpace.getAll(new ActualField(PLAYER_MAPPING), new FormalField(Integer.class), new FormalField(String.class));

            gameSpace.put(CLIENT_JOIN, -1, playerMapping[2]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mainClient.loadLobby();
    }

    public void tankDead(int tankID) {
        try {
            gameSpace.put(CLIENT_DEAD, playerID, tankID);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ClientGameListener implements Runnable{
    private SequentialSpace localCommunicationSpace;
    private Model master;
    private int msgId = 0;

    ClientGameListener(Model master, SequentialSpace localCommunicationSpace){
        this.master = master;
        this.localCommunicationSpace = localCommunicationSpace;
    }

    @Override
    public void run() {
        try {
            RemoteSpace gameSpace = (RemoteSpace) localCommunicationSpace.query(new ActualField(SERVER_SPACE),new FormalField(RemoteSpace.class))[1];
            int playerID = (int) localCommunicationSpace.query(new ActualField(PLAYER_ID),new FormalField(Integer.class))[1];

            while (true){
                //(CMD,id,data,msg_id)
                Object[] commTuple =  gameSpace.get(new FormalField(ServerCommand.class),
                                                    new FormalField(Integer.class),
                                                    new FormalField(Object.class),
                                                    new ActualField(msgId++));

                int actionOwnerID = (int) commTuple[1];
                switch ((ServerCommand) commTuple[0]){
                    case SERVER_SHOOT: {
                        ProjectilePacket projectilePacket = (ProjectilePacket)commTuple[2];
                        master.addProjectile(actionOwnerID,projectilePacket.toProjectile());
                        break;
                    }

                    case SERVER_MOVE:{
                        if (actionOwnerID == playerID) continue;

                        Vector position = (Vector) commTuple[2];
                        master.moveTankTo(actionOwnerID, position);
                        break;
                    }

                    case SERVER_CHANGE_PHASE:{
                        master.setPhase((Phase) commTuple[2]);
                        break;
                    }

                    case SERVER_DEAD: {
                        //A player is set as dead
                        int deadTankID = (int) commTuple[2];
                        master.setTankAlive(deadTankID,false);
                        break;
                    }

                    case SERVER_VICTORY: {
                        master.victoryScreen((String) commTuple[2]);
                        return;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}