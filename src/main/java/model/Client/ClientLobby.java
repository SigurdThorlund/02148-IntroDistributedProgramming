package model.Client;

import controllers.ClientLobbyController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import main.MainClient;
import model.Model;
import networking.Commands.ServerCommand;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import utils.TerrainOption;

import static networking.Commands.ClientCommand.CLIENT_START;
import static networking.Commands.LocalCommand.*;
import static networking.Commands.ServerCommand.SERVER_JOIN;

//Logic for client communication inside the lobby
public class ClientLobby implements Client{
    private SequentialSpace localCommunicationSpace;
    private RemoteSpace lobbySpace;
    private int playerID;
    private String playerName;
    private MainClient mainClient;

    public ClientLobby(MainClient mainClient, SequentialSpace localCommunicationSpace) {
        this.mainClient = mainClient;
        //Initialize UI
        FXMLLoader loader = mainClient.setScene(ClientState.LOBBY);
        ClientLobbyController clientLobbyController = loader.getController();
        clientLobbyController.init(this, localCommunicationSpace);

        try {
            //Get reference to remote space
            Object[] lobbySpaceO = localCommunicationSpace.query(new ActualField(SERVER_SPACE), new FormalField(RemoteSpace.class));
            lobbySpace = (RemoteSpace) lobbySpaceO[1];

            //Get username and find id
            Object[] userNameO = localCommunicationSpace.query(new ActualField(PLAYER_NAME), new FormalField(String.class));
            playerName = (String) userNameO[1];

            Object[] userIdO = lobbySpace.get(new ActualField(SERVER_JOIN), new FormalField(Integer.class), new ActualField(playerName));
            playerID = (int) userIdO[1];

            //Remember playerID
            localCommunicationSpace.put(PLAYER_ID, playerID);

             LobbyListener lobbyListener = new LobbyListener(lobbySpace, localCommunicationSpace, clientLobbyController);

             //See when thread is done - entering game
             lobbyListener.stateProperty().addListener(((observable, oldValue, newValue) -> {
                 if (newValue == Worker.State.SUCCEEDED)
                    mainClient.loadGame();
             }));

            new Thread(lobbyListener).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startGame(){
        try {
            lobbySpace.put(CLIENT_START, playerID, new TerrainOption(Model.DEFAULT_GAME_WIDTH, Model.DEFAULT_GAME_HEIGHT));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exit() {
        //TODO: Notify server of exit
        System.out.println("Notifying server of exit");
    }
}


class LobbyListener extends Task<Void> {
    private RemoteSpace remoteSpace;
    private SequentialSpace localCommunicationSpace;
    private ClientLobbyController clientLobbyController;
    private int playerID;
    private int msgId = 0;

    LobbyListener(RemoteSpace remoteSpace, SequentialSpace localCommunicationSpace, ClientLobbyController clientLobbyController){
        this.remoteSpace = remoteSpace;
        this.localCommunicationSpace = localCommunicationSpace;
        this.clientLobbyController = clientLobbyController;

        try {
            Object[] playerIDO = localCommunicationSpace.query(new ActualField(PLAYER_ID), new FormalField(Integer.class));
            playerID = (int) playerIDO[1];
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected Void call() throws Exception {
        while (true){
            try {
                //(CMD,id,data,msg_id)
                Object[] commTuple =  remoteSpace.query(new FormalField(ServerCommand.class),
                        new FormalField(Integer.class
                        ),
                        new FormalField(Object.class),
                        new ActualField(msgId));

                int actionOwnerID = (int) commTuple[1];
                switch ((ServerCommand) commTuple[0]){
                    case SERVER_JOIN: {
                        Platform.runLater(() -> clientLobbyController.addPlayer((String) commTuple[2]));
                        localCommunicationSpace.put(PLAYER_MAPPING, actionOwnerID, commTuple[2]);
                        System.out.println("User " + commTuple[2] + " has joined the lobby.");
                        break;
                    }

                    case SERVER_LEAVE: {
                        System.out.println("User " + commTuple[2] + " has left the lobby.");
                        break;

                    } case SERVER_HOST_ID: {
                        int hostId = (int) commTuple[1];
                        if (hostId == playerID)
                            Platform.runLater(() -> clientLobbyController.setHost(true));
                        break;
                    } case SERVER_START:{
                        return null; //Exit thread with success
                    }

                    default: System.out.println("Unexpected command: " + commTuple[0]);
                }

                msgId++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
