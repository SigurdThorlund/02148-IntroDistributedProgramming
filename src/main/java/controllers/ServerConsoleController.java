package controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.MainServer;
import model.Server.ServerInstance;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import static networking.Commands.LocalCommand.SERVER_TEXT_CONSOLE;

public class ServerConsoleController {
    private MainServer mainServer;
    private Space consoleSpace;
    private HashMap<String,TextArea> tabMapping;

    @FXML
    private TextArea serverOutput;

    @FXML
    private TextField ipInput;

    @FXML
    private TabPane consolePane;

    @FXML
    private void enableChangeIP(){
        //When clicked on IP-Area
        ipInput.setEditable(true);
        ipInput.setDisable(false);
        ipInput.requestFocus();
    }

    @FXML
    private void changeIPTextField(){
        //Runs when the user presses enter in the textfield
        String newIP = ipInput.getText();
        ipInput.setEditable(false);
        ipInput.setDisable(true);

        mainServer.connectToIP(newIP);
    }

    public void init(MainServer mainServer){
        this.mainServer = mainServer;
        tabMapping = new HashMap<>();
        consoleSpace = mainServer.getConsoleSpace();

        Task<String> consoleListener = new Task<String>() {
            @Override
            protected String call() throws Exception {
                while (true){
                    try{
                        Object[] textOutput = consoleSpace.get(new ActualField(SERVER_TEXT_CONSOLE),
                                                                            new FormalField(String.class),
                                                                            new FormalField(String.class));
                        String tabName = (String) textOutput[1];

                        TextArea outputField;
                        if (tabName.equals("main")) outputField = serverOutput;
                        else {
                            outputField = tabMapping.get(tabName);
                            if (outputField == null) {
                                consoleSpace.put(textOutput);
                                continue;
                            }
                        }

                        outputField.appendText(textOutput[2] + "\n");}
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };

        new Thread(consoleListener).start();
    }

    //Add a new tab for console output
    public void addTab(ServerInstance lobby){
        try {

            //Load layout
            FXMLLoader loader = new FXMLLoader();
            URL fxmlUrl = getClass().getResource("/ServerTab.fxml");
            loader.setLocation(fxmlUrl);
            Node root = loader.load();


            //Get reference to textarea
            TextArea lobbyTextField = (TextArea) root.lookup("#textArea");
            String lobbyName = lobby.getLobbyName();
            Tab lobbyTab = new Tab(lobbyName,root);
            consolePane.getTabs().add(lobbyTab);

            tabMapping.put(lobbyName,lobbyTextField);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printToServer(String text){
        try {
            consoleSpace.put(SERVER_TEXT_CONSOLE,"main", text);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setIPText(String newIP){
        ipInput.setText(newIP);
    }
}