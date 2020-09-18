package model.terrain;

import model.GameObject;
import model.hitbox.Hitbox;
import model.hitbox.HitboxRectangle;
import utils.Vector;

import java.util.Random;

public class Terrain {
    private int segmentCount;
    private int max_height;
    private TerrainSlice[] terrainSlices;
    private int FILTER_SIZE = 25;

    public Terrain(int width, int max_height) {
        segmentCount = width;
        this.max_height = max_height;
        terrainSlices = new TerrainSlice[segmentCount];
        Random random = new Random();
    }

    public int getHeightForX(int x) {
        if (terrainSlices[x].getIntervals().size() > 1) {
            return 50;
        } else {
            return terrainSlices[x].getIntervals().get(0).getMax();
        }
    }

    private int f(double x, double a, double b) {
        return (int) Math.abs(a * (Math.cos(x / a) + Math.sin(x / b)));
    }

    //Generates terrain to play on
    public void generate(int seed) {
        Random random = new Random();
        random.setSeed(seed);
        final int min_height = 10;
        int a = random.nextInt(150) + 20;
        int b = random.nextInt(150) + 20 - a;

        int last_height = (int) (min_height * 0.98 + (0.02 * random.nextInt(max_height)) + 100);

        int[] heights = new int[segmentCount + FILTER_SIZE];

        for (int i = 0; i < segmentCount + FILTER_SIZE; i++) {
            int new_height1 = (int) (last_height * 0.98 + 0.02 * random.nextInt(max_height));
            int new_height2 = f(i, a, b) + 100;
            int new_height = (int) (0.3 * new_height1 + 0.7 * new_height2);
//            terrainSlices[i] = new TerrainSlice(new_height);
            heights[i] = new_height;
            last_height = new_height1;
        }

        terrainSlices = filter(heights);
    }

    private TerrainSlice[] filter(int[] heights) {

        TerrainSlice[] ts = new TerrainSlice[segmentCount];

        int sum = 0;

        for (int i = 0; i < segmentCount + FILTER_SIZE; i++) {
            sum += heights[i];
            if (i >= FILTER_SIZE) {
                ts[i - FILTER_SIZE] = new TerrainSlice(sum / FILTER_SIZE);
                sum -= heights[i - FILTER_SIZE];
            } else {
                ts[i] = new TerrainSlice(heights[i]);
            }
        }
        return ts;
    }

    public boolean delete_circle(Vector v, int r) {
        int centerX = (int) Math.round(v.getXpos());
        int centerY = (int) Math.round(v.getYpos());
        int rsq = r*r;

        boolean deletionHappened = false;
        for (int x = 0; x <= r; x++){

            int yOffset = (int) Math.round(Math.sqrt( rsq - x*x));

            if (centerX + x < segmentCount)
                deletionHappened |= terrainSlices[centerX + x].delete(centerY - yOffset,centerY + yOffset + 1);

            if (centerX - x >= 0)
                deletionHappened |= terrainSlices[centerX - x].delete(centerY - yOffset,centerY + yOffset + 1);
        }
        return deletionHappened;
    }

    public void draw() {
        for (int i = 0; i < segmentCount; i++) {
            terrainSlices[i].draw();
            System.out.println();
        }
    }

    public TerrainSlice[] getTerrainSlices() {
        return terrainSlices;
    }

    public int getSegmentCount() {
        return segmentCount;
    }

    public int getMaxHeight() {
        return max_height;
    }

    public boolean collision(GameObject go) {
        Hitbox hb = go.getHitBox();

        if (hb.getClass().equals(HitboxRectangle.class)) {
            HitboxRectangle hbR = (HitboxRectangle) hb;

            int xStart = hbR.getX() - hbR.getLength() / 2;
            int xEnd = hbR.getX() + hbR.getLength() / 2;
            int y = hbR.getY() - hbR.getHeight() / 2;

            for (int i = xStart; i<= xEnd; i++) {
                if (i >= segmentCount)
                    return false;
                if (i < 0)
                    continue;

                for (TerrainInterval ti : terrainSlices[i].getIntervals() ) {
                    if (y <= ti.getMax() && y >= ti.getMin()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

