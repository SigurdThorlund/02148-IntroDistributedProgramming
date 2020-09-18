package utils;

import model.Projectile;

public class ProjectilePacket {
    private Vector position;
    private double force;
    private double angle;
    private int blastRadius;

    public ProjectilePacket(Projectile p){
        position = p.getPosition();
        force = p.getStartVelocity();
        angle = p.getAngle();
        blastRadius = p.getBlastRadius();
    }

    public Projectile toProjectile(){
        return new Projectile(position,force,angle,blastRadius);
    }
}
