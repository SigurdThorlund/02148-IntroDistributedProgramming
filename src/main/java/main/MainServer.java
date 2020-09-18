package main;

import controllers.ServerConsoleController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.Server.ServerInstance;
import networking.Network;
import org.jspace.*;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

import static networking.Commands.ClientCommand.CLIENT_JOIN;
import static networking.Commands.LocalCommand.SERVER_TEXT_CONSOLE;
import static networking.Commands.ServerCommand.SERVER_LOBBY_NAME;

public class MainServer extends Application {
    private SequentialSpace consoleSpace;
    private ServerConsoleController serverConsoleController;
    private String ip;

    public static void main(String[] args) {
        launch(args);
    }

    public SequentialSpace getConsoleSpace() {
        return consoleSpace;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        consoleSpace = new SequentialSpace();

        primaryStage.setTitle("Tanks - Server");
        primaryStage.getIcons().add(new Image("/icon_server.png"));

        //Load FXML layout
        FXMLLoader loader = new FXMLLoader();
        URL fxmlUrl = getClass().getResource("/ServerConsole.fxml");
        loader.setLocation(fxmlUrl);
        Parent root = loader.load();

        serverConsoleController = loader.getController();
        serverConsoleController.init(this);

        connectToIP(computeDefaultIp());

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    //Computes the ip the server is initially set to
    private String computeDefaultIp(){
        //https://stackoverflow.com/questions/19476872/java-get-local-ip
        try {
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            while (n.hasMoreElements()) {
                NetworkInterface e = n.nextElement();
                Enumeration<InetAddress> a = e.getInetAddresses();

                //Skips localhost
                if (e.getDisplayName().contains("Loopback"))
                    continue;

                //Skips local ip used by VirtualBox
                if (e.getDisplayName().contains("VirtualBox"))
                    continue;

                while (a.hasMoreElements()) {
                    InetAddress addr = a.nextElement();
                    String ip = addr.getHostAddress();

                    //Skips Ipv6 addresses
                    if (ip.contains(":"))
                        continue;

                    return ip;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void connectToIP(String newIP){
        if (newIP.equals(ip))
            return;
        ip = newIP;

        SpaceRepository remoteSpaces = new SpaceRepository();
        SequentialSpace entrySpace = new SequentialSpace();
        remoteSpaces.add("entry", entrySpace);

        if (remoteSpaces.addGate(Network.createEntryURI(newIP))){
            serverConsoleController.printToServer("Server started on: " + ip);
            serverConsoleController.setIPText(ip);

            new Thread(new Connector(entrySpace, consoleSpace, remoteSpaces, this)).start();
        } else{
            serverConsoleController.printToServer("Error: Unable to host server on " + ip);
        }
    }

    public void addTab(ServerInstance serverLobby){
        Platform.runLater(() -> serverConsoleController.addTab(serverLobby));
    }
}

//Allows users to join the lobby
class Connector implements Runnable{
    private MainServer mainServer;
    private Space entrySpace;
    private Space localCommunicationSpace;
    private SpaceRepository remoteSpaces;
    private SequentialSpace lobbies;

    Connector(Space entrySpace, Space localCommunicationSpace, SpaceRepository remoteSpaces, MainServer mainServer){
        this.entrySpace = entrySpace;
        this.localCommunicationSpace = localCommunicationSpace;
        this.mainServer = mainServer;
        this.remoteSpaces = remoteSpaces;
        this.lobbies = new SequentialSpace();
    }

    @Override
    public void run() {
        while (true){
            try {
                Object[] connectedPlayer  = entrySpace.get(new ActualField(CLIENT_JOIN),new FormalField(String.class), new FormalField(String.class));
                String playerName = (String) connectedPlayer[1];
                String lobbyName = (String) connectedPlayer[2];

                //Respond with lobby location
                Object[] lobbyO = lobbies.queryp(new ActualField(lobbyName), new FormalField(ServerInstance.class));

                ServerInstance lobby = null;
                if (lobbyO == null){
                    //Create new lobby
                    lobby = new ServerInstance(lobbyName, remoteSpaces, mainServer);
                    lobby.start();

                    lobbies.put(lobbyName,lobby);
                } else
                    lobby = (ServerInstance) lobbyO[1];

                entrySpace.put(SERVER_LOBBY_NAME,playerName,lobbyName);

                //To print
                localCommunicationSpace.put(SERVER_TEXT_CONSOLE,"main",playerName + " has joined the server");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}