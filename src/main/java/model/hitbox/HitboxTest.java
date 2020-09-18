package model.hitbox;

import utils.Vector;

import java.util.Arrays;

public class HitboxTest {
    public static void main(String[] args) throws Exception {
        Hitbox circleA = new HitboxCircle(new Vector(50, 50), 20);
        Hitbox circleB = new HitboxCircle(new Vector(40, 50), 20);
        Hitbox circleC = new HitboxCircle(new Vector(100, 90), 15);
        Hitbox circleD = new HitboxCircle(new Vector(100, 100), 5);
        Hitbox circleE = new HitboxCircle(new Vector(125, 45), 5);

        Hitbox rectA = new HitboxRectangle(new Vector(50,50), 10, 15);
        Hitbox rectB = new HitboxRectangle(new Vector(0,0), 10, 15);
        Hitbox rectC = new HitboxRectangle(new Vector(130,48), 10, 15);
        Hitbox rectD = new HitboxRectangle(new Vector(50,52), 105, 5);

        HitboxList listA = new HitboxList(Arrays.asList(circleA,circleB,circleE));

        System.out.println("Circle vs Circle tests");
        System.out.println("Expected: true - Actual: " + circleA.collision(circleB));
        System.out.println("Expected: false - Actual: " + circleA.collision(circleC));
        System.out.println("Expected: false - Actual: " + circleD.collision(circleB));
        System.out.println("Expected: true - Actual: " + circleC.collision(circleD));
        System.out.println();

        System.out.println("Rectangle vs Rectangle tests");
        System.out.println("Expected: false - Actual: " + rectA.collision(rectB));
        System.out.println("Expected: false - Actual: " + rectC.collision(rectD));
        System.out.println("Expected: true - Actual: " + rectA.collision(rectD));
        System.out.println("Expected: false - Actual: " + rectA.collision(rectC));
        System.out.println();

        System.out.println("Circle vs Rectangle tests");
        System.out.println("Expected: false - Actual: " + circleA.collision(rectB));
        System.out.println("Expected: true - Actual: " + circleA.collision(rectA));
        System.out.println("Expected: false - Actual: " + circleD.collision(rectD));
        System.out.println("Expected: true - Actual: " + circleE.collision(rectC));
        System.out.println();


        System.out.println("HitboxList vs Hitbox tests");
        System.out.println("Expected: false - Actual: " + listA.collision(circleC));
        System.out.println("Expected: false - Actual: " + circleC.collision(listA));
        System.out.println("Expected: true - Actual: " + listA.collision(rectD));
        System.out.println("Expected: true - Actual: " + circleE.collision(listA));
        System.out.println();

    }
}
