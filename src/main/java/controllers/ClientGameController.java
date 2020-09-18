package controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.Model;
import model.Server.ServerGame;
import utils.Direction;
import utils.Phase;
import view.GameView;

import static model.Model.COUNTDOWN_TIME_MS;
import static model.Model.TIMESTEP_MS;
import static utils.Phase.*;

public class ClientGameController{
    private final double FORCE_SPEED = 0.02;

    private GameView gameView;
    private Scene gameScene;
    private Model master;

    private Phase phase;

    private boolean progress_bar_forward;
    private double force = 0;

    private int countdownTimer = COUNTDOWN_TIME_MS;

    private boolean hasSynced;

    @FXML
    private Canvas spriteCanvas;

    @FXML
    private Canvas terrainCanvas;

    @FXML
    private Canvas backgroundCanvas;

    @FXML
    private ProgressBar progress_bar;

    @FXML
    private Text angle_text;

    @FXML
    private Text time_text;

    @FXML
    private Text hp_text;

    @FXML
    private Text state_text;

    @FXML
    private Text victoryText;

    @FXML
    private AnchorPane victoryArea;

    private final Timeline animation = new Timeline(new KeyFrame(Duration.millis(TIMESTEP_MS), event -> {
        countdownTimer -= TIMESTEP_MS;

        //Update positions of objects
        master.updateExplosions();
        master.updateProjectilePositions();
        master.readSpace();

        //Draw if anything has been updated
        displayTime();
        displayHealth();
        gameView.drawObjects(master.getTanks(), master.getProjectiles(), master.getExplosions());

        if (master.collisionDetection())
            gameView.drawTerrain(master.getTerrain());
    }));

    private EventHandler<KeyEvent> keyHandler = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            if (!master.getPlayer().isAlive()) {
                return;
            }
            switch (event.getCode()) {
                case LEFT:
                    if (phase.equals(MOVE_PHASE)) {
                        master.updatePlayerPosition(Direction.LEFT);
                    } else if (phase.equals(SHOOT_PHASE)) {
                        master.incrementAngle(Direction.UP);
                        displayAngle();
                    }
                    break;
                case RIGHT:
                    if (phase.equals(MOVE_PHASE)) {
                        master.updatePlayerPosition(Direction.RIGHT);
                    } else if (phase.equals(SHOOT_PHASE)) {
                        master.incrementAngle(Direction.DOWN);
                        displayAngle();
                    }
                    break;
                case SPACE:
                    if (phase.equals(SHOOT_PHASE)) {
                        incrementForce();
                        displayForce();
                    }
                    break;
            }
        }
    };

    @FXML
    private void exit(){
        System.exit(0);
    }

    @FXML
    private void goToLobby(){
        master.goToLobby();
    }

    public Model getModel() {
        return master;
    }

    private void incrementForce() {
            if (progress_bar_forward) {
                force = force + FORCE_SPEED;
            } else {
                force = force - FORCE_SPEED;
            }

            if (force > 1.0) {
                progress_bar_forward = false;
                force = 1.0;
            } else if (force < 0.0) {
                progress_bar_forward = true;
                force = 0.0;
            }
    }

    //Move into view if possible
    private void displayAngle() {
        double player_angle = master.getPlayer().getAngle()*180 / Math.PI;
        if (player_angle > 90) {
            player_angle = 180.0 - player_angle;
        }

        angle_text.setText(String.format("%.0f", player_angle));
    }

    private void displayTime() {
        int timerValue = countdownTimer / 1000;

        if (timerValue >= 0)
            time_text.setText(String.valueOf(countdownTimer / 1000));

        //Place dots instead of going negative
        else {
            timerValue += 10000 * 3;
            timerValue %= 3;

            String dots = "";

            for (int i = 0; i <= 2 - timerValue; i++)
                dots += ".";

            time_text.setText(dots);
        }

    }

    private void displayForce() {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setHue(-(force * 0.66));

        progress_bar.setEffect(colorAdjust);
        progress_bar.setProgress(force);
    }

    private void displayHealth() {
        hp_text.setText(String.valueOf(master.getPlayer().getHealth()));
    }

    private void handleRelease(KeyEvent event) {
        if (!master.getPlayer().isAlive())
            return;
        if (event.getCode().equals(KeyCode.SPACE)) {
            if (phase.equals(SHOOT_PHASE)) {
                master.shoot(force);

                //Reset force
                force = 0;
                progress_bar_forward = true;
            }
        }
    }

    public void changePhase(Phase newPhase) {
        String stateText = "";
        switch (newPhase){
            case MOVE_PHASE: {
                stateText = "Movement";
                countdownTimer = ServerGame.MOVEMENT_DURATION * 1000;
                break;
            }
            case SHOOT_PHASE:{
                stateText = "Shoot";
                countdownTimer = ServerGame.SHOOT_DURATION * 1000;
                break;
            }
            case COUNTDOWN:{
                stateText = "Intermission";
                countdownTimer = ServerGame.PAUSE_DURATION * 1000;

                //If last phase was movement, sync position
                if (phase == MOVE_PHASE)
                    master.syncPlayerPosition();
                break;
            }
        }
        phase = newPhase;

        //Set state text
        String finalStateText = stateText;
        Platform.runLater(() -> state_text.setText(finalStateText));
    }

    public void init(int width, int height) {
        master = new Model(width,height, this);
    }

    public void start() {
        GraphicsContext gcSprite = spriteCanvas.getGraphicsContext2D();
        GraphicsContext gcTerrain = terrainCanvas.getGraphicsContext2D();
        GraphicsContext gcBackground = backgroundCanvas.getGraphicsContext2D();

        gameView = new GameView(gcBackground, gcTerrain, gcSprite,master.getTerrain(), backgroundCanvas.getWidth(), backgroundCanvas.getHeight());

        phase = COUNTDOWN;
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    public void addListener() {
        gameScene = spriteCanvas.getScene();
        gameScene.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        gameScene.setOnKeyReleased(this::handleRelease);

        //Adds windowsize listeners
        ChangeListener<Number> resizeListener = (observable, oldValue, newValue) -> {
            resizeView((int) gameScene.getWidth(), (int)gameScene.getHeight());
        };
        gameScene.widthProperty().addListener(resizeListener);
        gameScene.heightProperty().addListener(resizeListener);
    }

    public void resizeView(int sceneWidth, int sceneHeight) {
        double width = master.getGameWidth();
        double height = master.getGameHeight();

        //Calculates the new cellsize and chooses the smallest one.
        double cellSizeWidth = sceneWidth / width;
        double cellSizeHeight = sceneHeight / height;

        //New size of a single cell in pixels
        double cell_size = Math.max(Math.min(cellSizeWidth, cellSizeHeight),1);

        gameView.setScale(cell_size);

        //New dimensions for the canvas in pixels
        int canvasWidth = (int) (width * cell_size);
        int canvasHeight = (int) (height * cell_size);

        //Sets the dimensions
        spriteCanvas.setWidth(canvasWidth);
        spriteCanvas.setHeight(canvasHeight);
        terrainCanvas.setWidth(canvasWidth);
        terrainCanvas.setHeight(canvasHeight);
        backgroundCanvas.setWidth(canvasWidth);
        backgroundCanvas.setHeight(canvasHeight);

        gameView.drawObjects(master.getTanks(), master.getProjectiles(), master.getExplosions());
        gameView.drawTerrain(master.getTerrain());
        gameView.drawBackground();
    }

    public void victoryScreen(String playerName){
        animation.stop();
        Platform.runLater(() -> {
            victoryArea.setDisable(false);
            victoryArea.setVisible(true);

            //Remove listeners
            Scene gameScene = victoryArea.getScene();
            gameScene.setEventDispatcher(null);
            gameScene.setOnKeyReleased(null);

            victoryText.setText(playerName + " has won the game!");
        });
    }
}
