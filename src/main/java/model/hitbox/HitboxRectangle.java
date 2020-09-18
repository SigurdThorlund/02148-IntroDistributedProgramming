package model.hitbox;

import utils.Vector;

public class HitboxRectangle extends Hitbox {

    private int length;

    private int height;

    public HitboxRectangle(Vector v, int length, int height) {
        super(v);
        this.length = length;
        this.height = height;
    }

    public int getLength() {
        return length;
    }

    public int getHeight() {
        return height;
    }


}
