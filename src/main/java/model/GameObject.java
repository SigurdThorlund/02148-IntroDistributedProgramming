package model;

import model.hitbox.Hitbox;
import utils.Direction;
import utils.Vector;

public abstract class GameObject {

    public abstract Vector getPosition();

    public abstract double getWidth();

    public abstract double getHeight();

    //Figure out solution to this
    public abstract void updatePosition(Direction dir);

    public abstract Hitbox getHitBox();
}
