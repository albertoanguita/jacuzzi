package aanguita.jacuzzi.io.serialization;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by Alberto on 05/01/2016.
 */
public class TestVersionStack {

    @Test
    public void test() throws Exception {

        VersionStack vs1 = new VersionStack("v1");
        VersionStack vs2 = new VersionStack("v2", vs1);
        VersionStack vs3 = new VersionStack("v3", vs2);

        ArrayList<String> ser = vs3.toArrayList();

        VersionStack vs4 = new VersionStack(ser);

        Assert.assertEquals(vs4, vs3);
    }
}
