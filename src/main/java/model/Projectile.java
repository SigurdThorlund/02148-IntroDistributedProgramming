package model;

import model.hitbox.Hitbox;
import model.hitbox.HitboxRectangle;
import utils.Direction;
import utils.Vector;

import static model.Model.ACCELERATION;
import static model.Model.PROJECTILE_TIMESTEP_MS;

public class Projectile extends GameObject {
    private final int WIDTH = 5;
    private final int HEIGHT = 5;
    private int damage = 30;

    private double time;
    private double startVelocity;
    private double angle;
    private double xVelocity;
    private double yVelocity;

    private Vector position;
    private Vector startPosition;
    private Hitbox hitbox;
    private int blastRadius;

    public Projectile(Vector pos, double force, double angle, int blastRadius) {
        this.startPosition = new Vector(pos.getXpos() - 0.5 * WIDTH, pos.getYpos() + 0.5 * HEIGHT);
        this.position = new Vector(pos.getXpos() - 0.5 * WIDTH, pos.getYpos() + 0.5 * HEIGHT);
        this.startVelocity = force;
        this.angle = angle;
        this.blastRadius = blastRadius;

        yVelocity = startVelocity * Math.sin(angle);
        xVelocity = startVelocity * Math.cos(angle);
        this.hitbox = new HitboxRectangle(position, WIDTH, HEIGHT);
    }

    @Override
    public Vector getPosition() {
        return position;
    }

    @Override
    public double getWidth() {
        return WIDTH;
    }

    @Override
    public double getHeight() {
        return HEIGHT;
    }

    public double getAngle() {
        return angle;
    }

    public double getStartVelocity() {
        return startVelocity;
    }

    @Override
    public void updatePosition(Direction dir) {
        time = time + PROJECTILE_TIMESTEP_MS / 1000;

        position.setXpos(startPosition.getXpos() + xVelocity * time);
        position.setYpos(startPosition.getYpos() + (yVelocity * time - 0.5 * ACCELERATION * Math.pow(time, 2)));
    }

    @Override
    public Hitbox getHitBox() {
        return hitbox;
    }

    public int getBlastRadius() {
        return blastRadius;
    }

    public int getDamage() {
        return damage;
    }
}
