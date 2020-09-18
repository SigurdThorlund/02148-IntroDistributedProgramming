package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.MotionBlur;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.Random;

import model.Explosion;
import model.Model;
import model.Projectile;
import model.Tank;
import model.terrain.Terrain;
import model.terrain.TerrainInterval;
import utils.Vector;

public class GameView {
    private double width = Model.DEFAULT_GAME_WIDTH;
    private double height = Model.DEFAULT_GAME_WIDTH;
    private double scalar = 1.0;

    private GraphicsContext gcObject;
    private GraphicsContext gcTerrain;
    private GraphicsContext gcBackground;
    private Terrain terrain;
    private ArrayList<Cloud> clouds;


    public GameView(GraphicsContext gcBackground, GraphicsContext gcTerrain, GraphicsContext gcObject, Terrain t, double width, double height) {
        this.gcObject = gcObject;
        this.gcTerrain = gcTerrain;
        this.gcBackground = gcBackground;
        this.width = width;
        this.height = height;
        this.terrain = t;
        drawBackground();
        drawTerrain(terrain);

        clouds = new ArrayList<>();
        Random r = new Random();
        int numClouds = r.nextInt(6);
        for (int i = 0; i < numClouds; i++) {
            clouds.add(new Cloud(terrain));
        }
        drawClouds();

    }

    public void drawBackground() {
        gcBackground.clearRect(0, 0, width * scalar, height * scalar);
        gcBackground.setFill(Color.LIGHTBLUE);
        gcBackground.fillRect(0, 0, width * scalar, height * scalar);
    }

    public void drawTerrain(Terrain t) {
        gcTerrain.clearRect(0, 0, width * scalar, height * scalar);
        Stop[] stops = new Stop[]{new Stop(0.6, Color.WHITE), new Stop(0, new Color(0.9, 0.9, 1.0, 1.0))};
        gcTerrain.setFill(new LinearGradient(0, terrain.getMaxHeight() * scalar, 0, 0, false, CycleMethod.NO_CYCLE, stops));
        gcTerrain.setEffect(new MotionBlur(0, 2));

        for (int i = 0; i < t.getSegmentCount(); i++)
            for (TerrainInterval ti : t.getTerrainSlices()[i].getIntervals())
                gcTerrain.fillRect(i * scalar, (height - ti.getMax()) * scalar, scalar, (ti.getMax() - ti.getMin()) * scalar);

        gcTerrain.setEffect(null);
    }

    public void drawObjects(Tank[] tanks, ArrayList<Projectile> projectiles, ArrayList<Explosion> explosions) {
        gcObject.clearRect(0, 0, width * scalar, height * scalar);
        drawExplosions(explosions);
        drawTanks(tanks);
        drawProjectiles(projectiles);
        drawClouds();
    }

    private void drawTanks(Tank[] tanks) {
        for (int i = 0; i < tanks.length; i++) {
            Tank tank = tanks[i];
            if (tank.isAlive()) {
                Color c = Color.hsb((360 / tanks.length) * i, 0.5, 0.5);
                gcObject.setFill(c);
            } else {
                gcObject.setFill(Color.DARKGREY);
            }
            gcObject.fillRect((tank.getPosition().getXpos() - 0.5 * tank.getWidth()) * scalar, scalar * (terrain.getMaxHeight() - (tank.getPosition().getYpos() + tank.getHeight() * 0.5)), tank.getWidth() * scalar, tank.getHeight() * scalar);
            gcObject.save();

            if (tank.isAlive()) {
                gcObject.setFill(Color.BLACK);
                gcObject.transform(new Affine(new Rotate(-tank.getAngle() * (180.0 / Math.PI), tank.getBarrel().pivotPoint().getXpos() * scalar, (terrain.getMaxHeight() - tank.getBarrel().pivotPoint().getYpos()) * scalar)));
                gcObject.fillRect(tank.getBarrel().getPosition().getXpos() * scalar, (terrain.getMaxHeight() - tank.getBarrel().getPosition().getYpos()) * scalar, tank.getBarrel().WIDTH * scalar, tank.getBarrel().HEIGHT * scalar);
                gcObject.restore();
            }

            String displayName = tank.getPlayerName();

            if (displayName.length() >= 8) {
                displayName = tank.getPlayerName().substring(0,8);
            }

            gcObject.setFill(Color.BLACK);
            gcObject.fillText(displayName,  (tank.getPosition().getXpos() - displayName.length()*2)*scalar,(height-tank.getPosition().getYpos() + 20)*scalar);
        }
    }

    private void drawProjectiles(ArrayList<Projectile> projectiles) {
        for (Projectile p : projectiles) {
            gcObject.setFill(Color.BLACK);
            gcObject.fillRect((p.getPosition().getXpos() + p.getWidth() * 0.5) * scalar, (terrain.getMaxHeight() - p.getPosition().getYpos() + p.getHeight() * 0.5) * scalar, p.getWidth() * scalar, p.getHeight() * scalar);
        }
    }

    private void drawExplosions(ArrayList<Explosion> explosions) {
        for (Explosion e : explosions) {
            gcObject.setFill(Color.YELLOW);
            gcObject.setGlobalAlpha(1 - e.getCurrentBlastRadius() / e.getFinalBlastRadius());

            double blastRadius = e.getCurrentBlastRadius();
            Vector position = e.getPosition();
            gcObject.fillOval((position.getXpos() - blastRadius) * scalar, (height - position.getYpos() - blastRadius) * scalar, blastRadius * 2 * scalar, 2 * blastRadius * scalar);
        }
        gcObject.setGlobalAlpha(1);
    }

    public void setScale(double cell_size) {
        scalar = cell_size;
    }


    public void drawClouds() {

        ArrayList<Cloud> toRemove = new ArrayList<>();
        drawBackground();
        Random r = new Random();
        if (r.nextInt(350) == 0) {
            Cloud c = new Cloud(terrain);
            c.pos.setXpos(0 - 0.5 * c.length);
            clouds.add(c);
        }


        for (Cloud c : clouds) {
            if (c.pos.getXpos() - c.length / 2 > terrain.getSegmentCount()) {
                toRemove.add(c);
                continue;
            }

            gcBackground.setFill(c.color);
            gcBackground.fillRect(scalar * (c.pos.getXpos() - c.length * 0.5), scalar * (height - c.pos.getYpos() - c.height * 0.5), scalar * c.length, scalar * c.height);
            c.pos.translate(c.speed, 0);
        }
        clouds.removeAll(toRemove);
    }


}
