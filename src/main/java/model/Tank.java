package model;

import model.hitbox.Hitbox;
import model.hitbox.HitboxCircle;
import model.terrain.TerrainInterval;
import utils.Direction;
import utils.Vector;

import model.terrain.Terrain;
import model.hitbox.HitboxRectangle;

import java.util.ArrayList;

public class Tank extends GameObject {
    private static final double STEP_SIZE = 6;
    private final int WIDTH = 20;
    private final int HEIGHT = 20;
    private final double SPEED = 0.8;

    private Vector position;
    private Barrel barrel;
    private int health = 100;

    private boolean isAlive = true;

    private HitboxRectangle hitbox;
    private double gameWidth;
    private Terrain t;
    private String playerName;

    private double angleRad = Math.PI / 2;

    public Tank(double xpos, Terrain t, String name) {
        double constrainedCenter = Math.min(t.getSegmentCount() - 1, xpos + 0.5 * WIDTH);
        position = new Vector(constrainedCenter, t.getHeightForX((int) constrainedCenter) + 0.5 * getHeight());

        this.hitbox = new HitboxRectangle(position, WIDTH, HEIGHT);
        this.gameWidth = t.getSegmentCount();
        this.t = t;
        this.playerName = name;

        barrel = new Barrel(this);
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public Vector getPosition() {
        return position;
    }

    public int getHealth() {
        return health;
    }


    @Override
    public double getWidth() {
        return WIDTH;
    }

    @Override
    public double getHeight() {
        return HEIGHT;
    }

    public void setPosition(Vector v) {
        position.setPos(v);
        barrel.getPosition().setYpos(position.getYpos() + barrel.HEIGHT * 0.5);
        barrel.getPosition().setXpos(position.getXpos());
    }

    public void updatePosition(Direction dir) {
        boolean moved = true;

        if (dir == Direction.LEFT) {
            position.translate(-SPEED, 0);
        } else if (dir == Direction.RIGHT) {
            position.translate(SPEED, 0);
        }

        if (position.getXpos() < 0 || position.getXpos() >= gameWidth) {
            if (dir == Direction.LEFT) {
                position.translate(SPEED, 0);

            } else if (dir == Direction.RIGHT) {
                position.translate(-SPEED, 0);
            }
            moved = false;
            return;
        }

        int y = (int) position.getYpos();

        // gets next terrain interval
        int idx = getNextTerrainIntervalIndex(position);
        TerrainInterval tiNew = null;
        if (idx >= 0) {
            tiNew = t.getTerrainSlices()[(int) (position.getXpos())].getIntervals().get(idx);
        }


        // if no terrain left (all terrain for x has been destroyed)
        if (idx < 0) {
            position.setYpos(0.5 * HEIGHT);

            barrel.getPosition().setYpos(position.getYpos() + barrel.HEIGHT * 0.5);
            barrel.getPosition().setXpos(position.getXpos());

            // there is terrain left, and we move to it
        } else if (tiNew != null && (tiNew.getMax() - y - 0.5 * HEIGHT) <= STEP_SIZE) {
            position.setYpos(tiNew.getMax() + 0.5 * HEIGHT);

            barrel.getPosition().setYpos(position.getYpos() + barrel.HEIGHT * 0.5);
            barrel.getPosition().setXpos(position.getXpos());
        } else {
            if (dir == Direction.LEFT) {
                position.translate(SPEED, 0);
            } else if (dir == Direction.RIGHT) {
                position.translate(-SPEED, 0);
            }
            moved = false;
        }

        if (moved) {
//            hitbox.getPosition().setXpos();
        }
    }

    private int getNextTerrainIntervalIndex(Vector pos) {

//        int curY = (int) (t.getMaxHeight() - pos.getYpos() + 5);
        int curY = (int) pos.getYpos();
        int x = (int) (pos.getXpos());
        ArrayList<TerrainInterval> tiList = t.getTerrainSlices()[x].getIntervals();

        int idx = -1;
        int minY = -1;

        for (int i = 0; i < tiList.size(); i++) {
            TerrainInterval ti = tiList.get(i);

            if (ti.getMin() > curY) {
                continue;
            } else if (minY == -1) {
                minY = ti.getMin();
                idx = i;
            }

            if (curY - ti.getMin() < curY - minY) {
                minY = ti.getMin();
                idx = i;
            }

        }
        return idx;
    }


    @Override
    public Hitbox getHitBox() {
        return hitbox;
    }

    public double getAngle() {
        return angleRad;
    }

    private final double BARREL_ANGLE_INCREMENT_RAD = Math.toRadians(1.0);

    public void incrementAngle(Direction dir) {

        if (dir.equals(Direction.UP)) {
            angleRad = angleRad + BARREL_ANGLE_INCREMENT_RAD;
        } else {
            angleRad = angleRad - BARREL_ANGLE_INCREMENT_RAD;
        }

        if (angleRad > Math.PI) {
            angleRad = Math.PI;
        } else if (angleRad < 0) {
            angleRad = 0;
        }
    }


    public boolean gameObjectCollision(GameObject go) {
        if (go.getClass().equals(Projectile.class)) {
            Projectile p = (Projectile) go;
            if (p.getHitBox().collision(hitbox)) {
                return true;
            }
        }
        return false;
    }

    public boolean projectileDamage(Projectile p) {
        HitboxCircle blast = new HitboxCircle(p.getPosition(), p.getBlastRadius());

        //Player is hit by explosion
        if (blast.collision(hitbox)) {
            int hyp = (int) Math.hypot(Math.abs(p.getPosition().getXpos() - position.getXpos()), Math.abs(p.getPosition().getYpos() - position.getYpos()));
            if (hyp <= p.getBlastRadius()) {
                health -= p.getDamage();
            } else {
                int damage = (p.getDamage() *p.getBlastRadius()) / hyp;
                health -= damage;
            }
        }

        if (health < 0) health = 0;
        return health > 0;
    }

    public Barrel getBarrel() {
        return barrel;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean state) {
        isAlive = state;
    }

    public void setAngleRad(double angleRad){
        this.angleRad = angleRad;
    }

    public class Barrel {
        public final double WIDTH = 20.0;
        public final double HEIGHT = 5.0;
        private Vector position;

        private Tank tank;

        private Barrel(Tank tank) {
            this.tank = tank;
            position = new Vector(tank.getPosition().getXpos(), tank.getPosition().getYpos() + HEIGHT * 0.5);
        }

        public Vector pivotPoint() {
            return tank.getPosition();
        }

        public Vector barrelEnd() {
//            Vector v = new Vector(pivotPoint().getXpos() + (Math.cos(tank.angleRad) * WIDTH) - 0.5 * HEIGHT, pivotPoint().getYpos() - Math.sin(tank.angleRad) * WIDTH - 0.5 * HEIGHT);
            Vector v = new Vector(WIDTH, 0);
            v.rotate(tank.angleRad);
            v.translate(position);
            return v;

        }

        public Vector getPosition() {
            return position;
        }
    }
}

