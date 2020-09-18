package main;

import controllers.ClientGameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Model;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class Main extends Application {
    private final int GAME_WIDTH = 800;
    private final int GAME_HEIGHT = 450;

    public static void main(String[] args) {
        SequentialSpace space = new SequentialSpace();
        try {
            space.put("Hello world");
            Object[] result = space.get(new FormalField(String.class));
            System.out.println(result[0]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("TANK GAME");
        //StackPane root = new StackPane();
        //Scene gameScene = new Scene(root, GAME_WIDTH, GAME_HEIGHT);

        FXMLLoader loader = new FXMLLoader();
        URL fxmlUrl = getClass().getResource("/ClientGame.fxml");
        loader.setLocation(fxmlUrl);
        Parent root = loader.load();

        //Setup reference to main
        ClientGameController cgc = loader.getController();
        cgc.init(Model.DEFAULT_GAME_WIDTH, Model.DEFAULT_GAME_HEIGHT);

        Model master = cgc.getModel();
        master.init(new Random().nextInt(),null, 1);
        master.spawn_player(200,0, "Me");
        cgc.start();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        cgc.addListener();
    }
}