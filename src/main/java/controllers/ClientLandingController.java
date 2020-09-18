package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jspace.SequentialSpace;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientLandingController implements Initializable {
    private SequentialSpace localCommunicationSpace;

    @FXML
    private TextField inputIP;

    @FXML
    private TextField inputUsername;

    @FXML
    private TextField inputLobby;

    @FXML
    private Label connectionTextArea;

    @FXML
    private Label errorTextArea;

    @FXML
    private Label errorText;

    @FXML
    private void informationEntered(){
        //When user presses enter in the input field
        String ip = inputIP.getText();
        String username = inputUsername.getText();
        String lobbyName = inputLobby.getText();

        if (!ip.isEmpty() && !username.isEmpty() && !lobbyName.isEmpty()) {
            putConnectionInfo(username,ip,lobbyName);
        }
    }

   //Called when the controller is instantiated
    public void initialize(URL location, ResourceBundle resources) {
        connectionTextArea.managedProperty().bind(connectionTextArea.visibleProperty());
        errorTextArea.managedProperty().bind(errorTextArea.visibleProperty());

        connectionTextArea.setVisible(false);
        errorTextArea.setVisible(false);
    }

    public void init(SequentialSpace localCommunicationSpace){
        this.localCommunicationSpace = localCommunicationSpace;
    }

    public void putConnectionInfo(String playerName, String ip, String lobbyName){
        inputUsername.setText(playerName);
        inputIP.setText(ip);
        inputLobby.setText(lobbyName);

        connectionTextArea.setVisible(true);
        errorTextArea.setVisible(false);

        try {
            //Send information to communication thread
            localCommunicationSpace.put(playerName,ip, lobbyName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Connecting to server failed
    public void setErrorReason(String text){
        errorText.setText(text);
        errorTextArea.setVisible(true);
        connectionTextArea.setVisible(false);
    }
}