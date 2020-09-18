package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.Client.ClientLobby;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import static networking.Commands.LocalCommand.SERVER_IP;
import static networking.Commands.LocalCommand.LOBBY_NAME;


public class ClientLobbyController{
    private ClientLobby clientLobby;
    private SequentialSpace localCommunicationSpace;

    private ObservableList<String> players;
    private boolean isHost;

    @FXML
    private Label ipText;

    @FXML
    private Label lobbyText;

    @FXML
    private ListView<String> listPlayers;

    @FXML
    private Button btnStart;

    @FXML
    private void startGame(){
        clientLobby.startGame();
    }


    public void init(ClientLobby clientLobby, SequentialSpace localCommunicationSpace){
        this.clientLobby = clientLobby;
        this.localCommunicationSpace = localCommunicationSpace;

        players = FXCollections.observableArrayList();
        listPlayers.setItems(players);

        try {
            ipText.setText(String.valueOf(localCommunicationSpace.query(new ActualField(SERVER_IP),new FormalField(String.class))[1]));
            lobbyText.setText((String) localCommunicationSpace.query(new ActualField(LOBBY_NAME), new FormalField(String.class))[1]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addPlayer(String playerName){
        players.add(playerName);
        listPlayers.refresh();

        if (isHost && players.size() > 1){
            btnStart.setDisable(false);
        }
    }

    public void removePlayer(String playerName){
        players.remove(playerName);
        listPlayers.refresh();

        if (players.size() < 2)
            btnStart.setDisable(true);
    }

    public void setHost(boolean state) {
        isHost = true;
        btnStart.setVisible(state);
        if (players.size() > 1){
            btnStart.setDisable(false);
        }
    }
}
