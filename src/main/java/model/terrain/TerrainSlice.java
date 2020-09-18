package model.terrain;

import java.util.ArrayList;

//One vertical column of the terrain
public class TerrainSlice {
    private ArrayList<TerrainInterval> intervals;

    TerrainSlice(int height) {
        intervals = new ArrayList<>();
        intervals.add(new TerrainInterval(0, height));
    }

    //Deletes an interval of the terrain, min inclusive, max exclusive
    boolean delete(int delete_min, int delete_max) {
        boolean deletionHappened = false;

        /*
            Creates 2 intervals (0,delete_min), (delete_max, TERRAIN_HEIGHT)
            and finds the overlapping intervals with (ti.min,ti.max)
        */

        for (int i = intervals.size() - 1; i >= 0 ; i--){
            TerrainInterval ti = intervals.get(i);

            //TerrainInterval is too low
            if (ti.min >= delete_max) continue;

            //TerrainInterval is too far up
            else if (ti.max < delete_min) return deletionHappened;

            int lowerL = ti.min;
            int lowerR = Math.min(ti.max, delete_min);

            int upperL = Math.max(ti.min, delete_max);
            int upperR = ti.max;

            //Max is taken for easy testing whether deletion has happened
            int lowerLength = Math.max(lowerR - lowerL, 0);
            int upperLength = Math.max(upperR - upperL,0);

            //If new size is not the same as the original, something must've gotten deleted
            if (lowerLength + upperLength != ti.max - ti.min)
                deletionHappened = true;

            if (lowerLength > 0) {
                //Overwrite values for current interval
                ti.min = lowerL;
                ti.max = lowerR;
                if (upperLength > 0){
                    intervals.add(i + 1, new TerrainInterval(upperL, upperR));
                    return deletionHappened;
                }
            } else if (upperLength > 0){
                ti.min = upperL;
                ti.max = upperR;
            } else
                intervals.remove(i);
        }
        return deletionHappened;
    }

    //Draw column
    void draw() {
        int y = 0;
        for (TerrainInterval interval : intervals) {
            while (y < interval.max) {
                if (interval.min <= y)
                    System.out.print("*");
                else
                    System.out.print(" ");
                y++;
            }
        }
    }

    public ArrayList<TerrainInterval> getIntervals() {
        return intervals;
    }
}
