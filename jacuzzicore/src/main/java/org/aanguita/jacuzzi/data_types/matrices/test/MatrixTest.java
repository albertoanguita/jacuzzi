package org.aanguita.jacuzzi.data_types.matrices.test;

import org.aanguita.jacuzzi.data_types.matrices.Matrix;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 08-jul-2010
 * Time: 16:53:56
 * To change this template use File | Settings | File Templates.
 */
public class MatrixTest {


    public static void main(String args[]) {

        Matrix<Integer> m = new Matrix<>(2, 0);

        m.setDimensionSizes(2, 2);
        m.set(1, 0, 0);
        m.set(2, 0, 1);
        m.set(3, 1, 0);
        m.set(4, 1, 1);


        m.setDimensionSizes(3, 3);

        m.set(5, 2, 2);


        System.out.println("FIN");
    }
}
