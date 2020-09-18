package model;

import utils.Vector;

import static model.Model.EXPLOSION_TIME_MS;
import static model.Model.TIMESTEP_MS;

public class Explosion {
    private Vector position;
    private double finalBlastRadius;
    private double currentBlastRadius;
    private int numSteps = (int) (EXPLOSION_TIME_MS / TIMESTEP_MS);


    public Explosion (Vector position, double blastRadius) {
        this.position = position;
        this.finalBlastRadius = blastRadius;
    }

    public void updateExplosion() {
        currentBlastRadius += (finalBlastRadius/numSteps);
    }

    public boolean finished() {
        return currentBlastRadius > finalBlastRadius;
    }

    public double getExplosionWidth() {
        return currentBlastRadius * 2;
    }

    public Vector getPosition() {
        return position;
    }

    public double getCurrentBlastRadius() {
        return currentBlastRadius;
    }

    public double getFinalBlastRadius() {
        return finalBlastRadius;
    }
}
