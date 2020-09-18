package model.hitbox;

import utils.Vector;

public class HitboxCircle extends Hitbox {

    private int radius;

    public HitboxCircle(Vector v, int radius) {
        super(v);
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }
}
