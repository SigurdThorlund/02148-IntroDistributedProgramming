package model.hitbox;

import utils.Vector;

import java.awt.*;

public abstract class Hitbox {

    public Vector getPosition() {
        return position;
    }

    private Vector position;
    // x and y is the position of the center of the Hitbox

    public Hitbox(Vector pos) {
        this.position = pos;
    }

    private boolean rectRectCollision(HitboxRectangle hbA, HitboxRectangle hbB) {

        Point ulA = new Point(hbA.getX() - hbA.getLength() / 2, hbA.getY() - hbA.getHeight() / 2);

        Point ulB = new Point(hbB.getX() - hbB.getLength() / 2, hbB.getY() - hbB.getHeight() / 2);

        Rectangle rectA = new Rectangle(ulA.x, ulA.y, hbA.getLength(), hbA.getHeight());
        Rectangle rectB = new Rectangle(ulB.x, ulB.y, hbB.getLength(), hbB.getHeight());

        return rectA.intersects(rectB);
    }

    private boolean circleCircleCollision(HitboxCircle hbA, HitboxCircle hbB) {
        return (Math.hypot(hbA.getX() - hbB.getX(), hbA.getY() - hbB.getY()) - hbA.getRadius() - hbB.getRadius() <= 0);
    }

    private boolean rectCircleCollision(HitboxRectangle hbr, HitboxCircle hbc) {

        int circleDistanceX = Math.abs(hbc.getX() - hbr.getX());
        int circleDistanceY = Math.abs(hbc.getY() - hbr.getY());

        int hbh = hbr.getHeight();
        int hbl = hbr.getLength();

        if (circleDistanceX > hbl / 2 + hbc.getRadius()) {
            return false;
        }

        if (circleDistanceY > hbh / 2 + hbc.getRadius()) {
            return false;
        }

        if (circleDistanceX <= hbl / 2) {
            return true;
        }
        if (circleDistanceX <= hbh / 2) {
            return true;
        }

        int cornerDistance_sq = ((circleDistanceX - hbl / 2) * (circleDistanceX - hbl / 2)) +
                ((circleDistanceY - hbh / 2) * (circleDistanceY - hbh / 2));

        return (cornerDistance_sq <= (hbc.getRadius() * hbc.getRadius()));
    }


    public boolean collision(HitboxList hbl) {
        return hbl.collision(this);
    }

    public int getX() {
        return (int) position.getXpos();
    }

    public int getY() {
        return (int) position.getYpos();
    }

    public boolean collision(Hitbox hb) {

        if (this.getClass().equals(HitboxCircle.class) && hb.getClass().equals(HitboxCircle.class)) {
            HitboxCircle hbc = (HitboxCircle) hb;
            HitboxCircle thisHb = (HitboxCircle) this;
            return circleCircleCollision(thisHb, hbc);

        } else if (this.getClass().equals(HitboxRectangle.class) && hb.getClass().equals(HitboxRectangle.class)) {
            HitboxRectangle hbr = (HitboxRectangle) hb;
            HitboxRectangle thisHb = (HitboxRectangle) this;
            return rectRectCollision(thisHb, hbr);

        } else if (this.getClass().equals(HitboxRectangle.class) && hb.getClass().equals(HitboxCircle.class)) { // else it is a rectangle and circle
            HitboxCircle hbc = (HitboxCircle) hb;
            HitboxRectangle thisHb = (HitboxRectangle) this;
            return rectCircleCollision(thisHb, hbc);

        } else if (this.getClass().equals(HitboxCircle.class) && hb.getClass().equals(HitboxRectangle.class)) {
            HitboxRectangle hbc = (HitboxRectangle) hb;
            HitboxCircle thisHb = (HitboxCircle) this;
            return rectCircleCollision(hbc, thisHb);
        }

        return false;
    }
}
