package model;

import controllers.ClientGameController;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import model.Client.ClientGame;
import model.terrain.Terrain;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import utils.Direction;
import utils.Phase;
import utils.Vector;

import java.util.ArrayList;

public class Model {

    public final static int DEFAULT_GAME_WIDTH = 800;
    public final static int DEFAULT_GAME_HEIGHT = 450;
    public final static double TIMESTEP_MS = 14.67;
    public final static double PROJECTILE_TIMESTEP_MS = 60;
    public final static double ACCELERATION = 9.81;
    public final static int FORCE_SCALAR = 100;
    public final static double EXPLOSION_TIME_MS = 500;

    public final static int COUNTDOWN_TIME_MS = 3000;
    public final static int MOVE_TIME_MS = 10000;
    public final static int SHOOT_TIME_MS = 10000;

    private Tank[] tanks;
    private Tank player;
    private Terrain terrain;
    private int gameWidth, gameHeight;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Explosion> explosions;
    private ArrayList<Explosion> explosionsToRemove;
    private ClientGame gameClient;
    private ClientGameController cgc;

    private String bgMusicFile = "music/Robot_City.mp3";
    private String shootFile = "effects/Howitzer_Cannon_Fire.mp3";
    private String explosionFile = "effects/Big_Explosion_Distant.mp3";
    private MediaPlayer bgMusicPlayer;
    private MediaPlayer shootEffectPlayer;
    private MediaPlayer explosionEffectPlayer;
    private Media bgMusic;
    private Media shootEffect;
    private Media explosionEffect;

    private Phase phase;
    private boolean justPressed;

    private SequentialSpace toAddSpace;

    public Model(int gameWidth, int gameHeight, ClientGameController cgc) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.cgc = cgc;

        toAddSpace = new SequentialSpace();
    }


    public void init(int seed, ClientGame gameClient, int playerCount) {
        this.gameClient = gameClient;

        terrain = new Terrain(gameWidth, gameHeight);
        terrain.generate(seed);
        projectiles = new ArrayList<>();
        explosions = new ArrayList<>();
        explosionsToRemove = new ArrayList<>();
        tanks = new Tank[playerCount];
        phase = Phase.MOVE_PHASE;

        sound_init();
    }

    public void sound_init() {
        try {
            //bgMusic = new Media("file:///" + System.getProperty("user.dir").replace('\\', '/') + "/src/main/resources/" + bgMusicFile);
            bgMusic = new Media(getClass().getClassLoader().getResource(bgMusicFile).toExternalForm());
            bgMusicPlayer = new MediaPlayer(bgMusic);
            bgMusicPlayer.setVolume(0.1);
            bgMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgMusicPlayer.play();

            shootEffect = new Media(getClass().getClassLoader().getResource(shootFile).toExternalForm());
            explosionEffect = new Media(getClass().getClassLoader().getResource(explosionFile).toExternalForm());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public Tank getPlayer() {
        return player;
    }

    public Tank[] getTanks() { return tanks; }

    public int getGameWidth() {
        return gameWidth;
    }

    public int getGameHeight() {
        return gameHeight;
    }

    public ArrayList<Explosion> getExplosions() {
        return explosions;
    }

    public ArrayList<Projectile> getProjectiles() { return projectiles; }

    public void setPhase(Phase newPhase) {
        cgc.changePhase(newPhase);
    }

    public void victoryScreen(String playerName) {
        cgc.victoryScreen(playerName);
    }

    public void goToLobby() {
        bgMusicPlayer.stop();
        gameClient.goToLobby();
    }

    public void spawn_player(int xposition, int id, String name) {
        player = new Tank(xposition, terrain, name);
        tanks[id] = player;
    }

    public void spawn_tank(int xposition, int id, String name){
        tanks[id] = new Tank(xposition,terrain, name);
    }

    public void setTankAlive(int tankID, boolean status){
        tanks[tankID].setAlive(status);
    }

    public void updateExplosions() {
        for (Explosion e : explosions) {
            if (e.finished()) {
                explosionsToRemove.add(e);
            }
            e.updateExplosion();
        }
        explosions.removeAll(explosionsToRemove);
    }
    public void updatePlayerPosition(Direction dir) {
        player.updatePosition(dir);
        if (justPressed)
            gameClient.movePlayerTo(player.getPosition());

        justPressed = !justPressed;
    }

    public void syncPlayerPosition(){
        gameClient.syncPlayerPosition(player.getPosition());
    }

    public void shoot(double force) {
        Projectile p = new Projectile(player.getBarrel().barrelEnd(), force * FORCE_SCALAR, player.getAngle(), 20);
        gameClient.shoot(p);
    }

    public void incrementAngle(Direction dir) {
        player.incrementAngle(dir);
    }


    public void updateProjectilePositions() {
        for (Projectile p : projectiles)
            p.updatePosition(Direction.NONE);
    }

    public boolean collisionDetection() {
        boolean needsUpdate = false;
        ArrayList<Projectile> removeProjectiles = new ArrayList<>();

        for (Projectile p : projectiles) {
            //Test if projectile is outside bounds
            if (p.getPosition().getXpos() <= 0 || p.getPosition().getXpos() >= terrain.getSegmentCount()) {
                removeProjectiles.add(p);
                continue;
            }

            boolean tankHit = false;
            for (Tank tank : tanks)
                if (tank.gameObjectCollision(p))
                    tankHit = true;

            if (terrain.collision(p) || tankHit) {
                explosions.add(new Explosion(p.getPosition(),p.getBlastRadius()));
                explosionEffectPlayer = new MediaPlayer(explosionEffect);
                explosionEffectPlayer.play();
                if (terrain.delete_circle(p.getPosition(), p.getBlastRadius())) {
                    needsUpdate = true;
                }

                for (int i = 0; i < tanks.length; i++) {
                    Tank tank = tanks[i];

                    //Don't update if is already dead
                    if (!tank.isAlive())
                        continue;

                    //Tank died
                    if (!tank.projectileDamage(p))
                        gameClient.tankDead(i);

                    tank.updatePosition(Direction.NONE);
                }
                removeProjectiles.add(p);
            }
        }
        projectiles.removeAll(removeProjectiles);
        return needsUpdate;
    }


    public void addProjectile(int ownerID, Projectile p){
        try {
            toAddSpace.put(ownerID,p);
            shootEffectPlayer = new MediaPlayer(shootEffect);
            shootEffectPlayer.setVolume(0.5);
            shootEffectPlayer.play();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void moveTankTo(int tankID,Vector position){
        try {
            //Replace other movement
            toAddSpace.getp(new ActualField(tankID), new FormalField(Vector.class));
            toAddSpace.put(tankID, position);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Fetch updates from remotespace posted in localspace
    public void readSpace() {
        for (Object[] update : toAddSpace.getAll(new FormalField(Integer.class), new FormalField(Object.class))){
            int tankID = (int) update[0];

            Class updateClass = update[1].getClass();
            if (updateClass == Projectile.class) {
                Projectile p = (Projectile) update[1];
                projectiles.add(p);

                tanks[tankID].setAngleRad(p.getAngle());
            } else if (updateClass == Vector.class)
                tanks[tankID].setPosition((Vector) update[1]);
        }
    }
}