package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import model.Client.*;
import org.jspace.SequentialSpace;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainClient extends Application {
    private Stage primaryStage;
    private SequentialSpace localCommunicationSpace;
    private Client currentClientState;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        localCommunicationSpace = new SequentialSpace();

        primaryStage.setTitle("Tanks");
        primaryStage.getIcons().add(new Image("/icon_client.png"));

        //Load landing scene for clients
        ClientLanding clientLanding = new ClientLanding(this, localCommunicationSpace);
        currentClientState = clientLanding;
        primaryStage.show();

        //Entered name & ip in console
        List<String> params = getParameters().getRaw();

        if(params.size() == 3){
            clientLanding.putConnectionInfo(params.get(0), params.get(1), params.get(2));
        }

        //Handler for closing window
        primaryStage.setOnCloseRequest(event -> {
            currentClientState.exit();
            System.exit(0);
        });

        //Toggleable fullscreen
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            KeyCode key = event.getCode();
            if (key.getName().equals("F11")){
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
            }
        });
    }

    public void loadLobby(){
        currentClientState = new ClientLobby(this,localCommunicationSpace);
    }

    public void loadGame() {
        currentClientState = new ClientGame(this,localCommunicationSpace);
    }

    public FXMLLoader setScene(ClientState state) {
        FXMLLoader loader = new FXMLLoader();
        try {
            URL fxmlUrl = getClass().getResource('/' + state.toString() + ".fxml");
            loader.setLocation(fxmlUrl);
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return loader;
    }
}