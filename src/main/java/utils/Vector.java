package utils;

import static java.lang.Math.round;

public class Vector {

    private double xpos;
    private double ypos;

    public Vector(double xpos, double ypos) {
        this.xpos = xpos;
        this.ypos = ypos;
    }

    public Vector(Vector v) {
        this.xpos = v.getXpos();
        this.ypos = v.getYpos();
    }

    public void translate(double dx, double dy) {
        this.xpos += dx;
        this.ypos += dy;
    }

    public void translate(Vector position) {
        this.xpos += position.getXpos();
        this.ypos += position.getYpos();
    }

    public String toString() {
        return "(" + String.format("%.2f", xpos) + " : " + String.format("%.2f", ypos) + ")";
    }

    public void rotate(double angleRad) {
        double oldX = xpos;
        double oldY = ypos;

        xpos = Math.cos(angleRad) * oldX - Math.sin(angleRad) * oldY;
        ypos = Math.sin(angleRad) * oldX + Math.cos(angleRad) * oldY;
    }


    public double getXpos() {
        return xpos;
    }

    public void setXpos(double newVal) {
        this.xpos = newVal;
    }

    public double getYpos() {
        return ypos;
    }

    public void setYpos(double newVal) {
        this.ypos = newVal;
    }

    public void setPos(Vector v){
        this.xpos = v.getXpos();
        this.ypos = v.getYpos();
    }

}
