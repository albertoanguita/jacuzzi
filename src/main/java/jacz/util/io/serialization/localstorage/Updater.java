package jacz.util.io.serialization.localstorage;

/**
 * Created by Alberto on 12/04/2016.
 */
public interface Updater {

    public String update(VersionedLocalStorage versionedLocalStorage, String storedVersion);
}
