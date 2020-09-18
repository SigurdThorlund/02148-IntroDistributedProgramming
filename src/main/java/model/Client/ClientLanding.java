package model.Client;

//Logic for client communication just after launch

import controllers.ClientLandingController;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import main.MainClient;
import networking.Network;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;

import static networking.Commands.ClientCommand.CLIENT_JOIN;
import static networking.Commands.LocalCommand.*;
import static networking.Commands.ServerCommand.SERVER_LOBBY_NAME;

public class ClientLanding implements Client{
    private ClientLandingController clientLandingController;

    public ClientLanding(MainClient mainClient, SequentialSpace localCommunicationSpace){
        //Load controller
        FXMLLoader loader = mainClient.setScene(ClientState.LANDING);
        clientLandingController = loader.getController();
        clientLandingController.init(localCommunicationSpace);

        ClientLandingListener connector = new ClientLandingListener(localCommunicationSpace);

        //Add listener for thread
        connector.stateProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED){
                mainClient.loadLobby();

            } else if (newValue == Worker.State.FAILED){
                clientLandingController.setErrorReason(connector.getException().getMessage());
                connector.restart();
            }
        }));

        connector.reset();
        connector.start();
    }

    public void putConnectionInfo(String username, String ip, String lobbyName){
        clientLandingController.putConnectionInfo(username,ip, lobbyName);
    }

    @Override
    public void exit() {
        //Called when user closes the program
    }
}


//Connects to the server
class ClientLandingListener extends Service<Boolean> {
    private SequentialSpace localCommunicationSpace;

    ClientLandingListener(SequentialSpace localCommunicationSpace){
        this.localCommunicationSpace = localCommunicationSpace;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Object[] userIP = localCommunicationSpace.get(  new FormalField(String.class),
                                                                new FormalField(String.class),
                                                                new FormalField(String.class));
                String username = (String) userIP[0];
                String serverIP = (String) userIP[1];
                String lobbyName = (String) userIP[2];

                RemoteSpace entrySpace = new RemoteSpace(Network.createEntryURI(serverIP));

                //Notify server that it has been joined
                entrySpace.put(CLIENT_JOIN, username, lobbyName);

                //Obtain lobby location
                Object[] lobbyInformation = entrySpace.get(new ActualField(SERVER_LOBBY_NAME), new ActualField(username), new FormalField(String.class));
                String lobbyLocation = (String) lobbyInformation[2];

                RemoteSpace lobbySpace = new RemoteSpace(Network.createLobbyURI(serverIP, lobbyLocation));
                //Notify lobby it has been joined
                lobbySpace.put(CLIENT_JOIN,-1, username);

                localCommunicationSpace.put(SERVER_SPACE,lobbySpace);
                localCommunicationSpace.put(SERVER_IP,serverIP);
                localCommunicationSpace.put(LOBBY_NAME, lobbyName);
                localCommunicationSpace.put(PLAYER_NAME,username);

                return true;
            }
        };
    }
}