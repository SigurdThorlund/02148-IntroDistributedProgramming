package model.terrain;

//Interval of terrain inside slice
public class TerrainInterval{
    int min; //Inclusive
    int max; //Exclusive

    public TerrainInterval(int min_y, int end){
        this.min = min_y;
        this.max = end;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public void setMin(int min_y) {
        this.min = min_y;
    }

    public void setMax(int max_y) {
        this.max = max_y;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)",min,max);
    }
}
