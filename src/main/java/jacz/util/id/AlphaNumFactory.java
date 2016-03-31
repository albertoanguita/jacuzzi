package jacz.util.id;

import jacz.util.string.AlphanumericString;

/**
 * Created by Alberto on 17/03/2016.
 */
public class AlphaNumFactory extends IdFactory<String> {

    private static String staticId = "1";

    private static AlphanumericString.CharTypeSequence charTypeSequence = new AlphanumericString.CharTypeSequence(AlphanumericString.CharType.NUMERIC, AlphanumericString.CharType.UPPERCASE);

    public AlphaNumFactory() {
        super("1");
    }

    @Override
    protected String cloneId() {
        return id;
    }

    @Override
    protected void nextId() {
        id = AlphanumericString.nextAlphanumericString(id, charTypeSequence);
    }

    public static synchronized String getStaticId() {
        String oldId = staticId;
        staticId = AlphanumericString.nextAlphanumericString(staticId, charTypeSequence);
        return oldId;
    }
}