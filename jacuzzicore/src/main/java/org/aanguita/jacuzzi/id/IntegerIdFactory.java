package org.aanguita.jacuzzi.id;

/**
 * Created by Alberto on 16/03/2016.
 */
public class IntegerIdFactory extends IdFactory<Integer> {

    private static int staticId = 1;

    public IntegerIdFactory() {
        super(1);
    }

    @Override
    protected Integer cloneId() {
        return id;
    }

    @Override
    protected void nextId() {
        id++;
    }

    public static synchronized int getStaticId() {
        return staticId++;
    }
}
