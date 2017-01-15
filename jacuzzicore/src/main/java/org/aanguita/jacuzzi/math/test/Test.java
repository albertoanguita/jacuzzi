package org.aanguita.jacuzzi.math.test;

import org.aanguita.jacuzzi.math.Functions;

/**
 * Created with IntelliJ IDEA.
 * User: Alberto
 * Date: 8/05/14
 * Time: 10:24
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String[] args) {

        double min = -1;
        double max = 1;
        double stretch = 1;
        System.out.println(Functions.sigmoid(-3, min, max, stretch));
        System.out.println(Functions.sigmoid(-2, min, max, stretch));
        System.out.println(Functions.sigmoid(-1, min, max, stretch));
        System.out.println(Functions.sigmoid(0, min, max, stretch));
        System.out.println(Functions.sigmoid(1, min, max, stretch));
        System.out.println(Functions.sigmoid(2, min, max, stretch));
        System.out.println(Functions.sigmoid(3, min, max, stretch));
    }
}
