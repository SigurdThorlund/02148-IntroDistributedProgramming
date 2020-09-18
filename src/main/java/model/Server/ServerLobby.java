package model.Server;

import networking.Commands.ClientCommand;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import utils.TerrainOption;

import java.util.List;

import static networking.Commands.LocalCommand.*;
import static networking.Commands.ServerCommand.*;

public class ServerLobby{
    private String lobbyName;
    private SequentialSpace lobbySpace;
    private SequentialSpace consoleSpace;
    private SequentialSpace localCommunicationSpace;
    private int msg_id = 0;

    private int hostPlayer = -1;

    //Creates a new lobby and opens a gate
    ServerLobby(String lobbyName, SequentialSpace localCommunicationSpace){
        this.lobbyName = lobbyName;
        this.localCommunicationSpace = localCommunicationSpace;

        try {
            lobbySpace = (SequentialSpace) localCommunicationSpace.query(new ActualField(SERVER_SPACE),
                                                                         new FormalField(SequentialSpace.class))[1];
            consoleSpace = (SequentialSpace) localCommunicationSpace.query( new ActualField(SERVER_CONSOLE),
                                                                            new FormalField(SequentialSpace.class))[1];
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        while (true){
            //Format: (ACTION,ACTION_TYPE,USERNAME)
            Object[] playerAction = lobbySpace.get( new FormalField(ClientCommand.class),
                                                    new FormalField(Integer.class),
                                                    new FormalField(Object.class));

            switch ((ClientCommand) playerAction[0]){
                case CLIENT_JOIN: {
                    String playerName = (String) playerAction[2];

                    List<Object[]> playerMapping = localCommunicationSpace.queryAll(new ActualField(PLAYER_MAPPING),
                                                                                    new FormalField(Integer.class),
                                                                                    new FormalField(String.class));
                    //Find id for player new player
                    int playerID = findSmallestID(playerMapping);

                    //Add player
                    localCommunicationSpace.put(PLAYER_MAPPING,playerID,playerName);
                    lobbySpace.put(SERVER_JOIN,playerID,playerName); //For user
                    lobbySpace.put(SERVER_JOIN,playerID,playerName, msg_id++); //For all users

                    //Assign host if none is present
                    if (hostPlayer == -1) {
                        hostPlayer = playerID;
                        lobbySpace.put(SERVER_HOST_ID,hostPlayer, "",msg_id++);
                    }

                    consoleSpace.put(SERVER_TEXT_CONSOLE,lobbyName, playerName + " has joined the lobby with id " + playerID);
                    break;
                }

                case CLIENT_START:{
                    TerrainOption terrainOption = (TerrainOption) playerAction[2];
                    lobbySpace.put(SERVER_START, hostPlayer, terrainOption,msg_id++);
                    localCommunicationSpace.put(TERRAIN_OPTIONS, terrainOption);
                    return;
                }
                default: consoleSpace.put(SERVER_TEXT_CONSOLE, lobbyName, "Received a client-command which is not supported at this stage, " +  playerAction[0]);
            }
        }
    }

    private int findSmallestID(List<Object[]> playerMapping){
        int idCount = playerMapping.size();

        boolean[] seenIDs = new boolean[idCount];
        for (Object[] player : playerMapping){
            seenIDs[(int) player[1]] = true;
        }

        for (int i = 0; i < idCount; i++){
            if (!seenIDs[i])
                return i;
        }

        return idCount;
    }
}