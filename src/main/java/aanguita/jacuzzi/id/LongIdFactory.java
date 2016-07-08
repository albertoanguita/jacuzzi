package aanguita.jacuzzi.id;

/**
 * Created by Alberto on 16/03/2016.
 */
public class LongIdFactory extends IdFactory<Long> {

    private static long staticId = 1;

    public LongIdFactory() {
        super(1L);
    }

    @Override
    protected Long cloneId() {
        return id;
    }

    @Override
    protected void nextId() {
        id++;
    }

    public static synchronized long getStaticId() {
        return staticId++;
    }
}
