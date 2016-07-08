package org.aanguita.jacuzzi.io.serialization.localstorage;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Alberto on 13/04/2016.
 */
public class VersionedLocalStorageTest implements Updater {

    @Test
    public void test() throws IOException {

        String path = "versionedLocalStorage.db";

        VersionedLocalStorage vls = VersionedLocalStorage.createNew(path, "1");
        vls.setString("1", "one");

        Assert.assertEquals("1", vls.getVersion());

        // storage is closed and used again

        vls = new VersionedLocalStorage(path);
        Assert.assertEquals("1", vls.getVersion());
        Assert.assertEquals("one", vls.getString("1"));
        Assert.assertEquals(null, vls.getString("2"));
        vls.updateVersion("2");
        Assert.assertEquals("2", vls.getVersion());
        vls = new VersionedLocalStorage(path);
        Assert.assertEquals("2", vls.getVersion());
        Assert.assertEquals("one", vls.getString("1"));
        Assert.assertEquals(null, vls.getString("2"));

        vls = new VersionedLocalStorage(path, this, "3");
        Assert.assertEquals("one", vls.getString("1"));
        Assert.assertEquals("two", vls.getString("2"));

        vls.clear();
        Assert.assertEquals("3", vls.getVersion());
    }

    @Override
    public String update(VersionedLocalStorage versionedLocalStorage, String storedVersion) {
        if (storedVersion.equals("2")) {
            versionedLocalStorage.setString("2", "two");
            return "3";
        } else {
            throw new RuntimeException();
        }
    }
}
