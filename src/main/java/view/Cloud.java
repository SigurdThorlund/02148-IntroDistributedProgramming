package view;

import javafx.scene.paint.Color;
import model.terrain.Terrain;
import utils.Vector;

import java.util.Random;

class Cloud {
    Vector pos;
    int length;
    int height;
    double speed;

    Color color;

    Cloud(Terrain terrain) {
        Random r = new Random();
        int x = r.nextInt(terrain.getSegmentCount());
        int y = r.nextInt((400 - 250) + 1) + 250;
        pos = new Vector(x,y);
        length = r.nextInt((80 - 30) + 1) + 30;
        height = r.nextInt((40 - 20) + 1) + 20;
        double opacity = 0.35 + r.nextDouble() * (0.85 - 0.35);
        speed = 0.05 + r.nextDouble() * (0.67 - 0.05);
        color = Color.web(Color.DARKGREY.toString(), opacity);
    }
}
