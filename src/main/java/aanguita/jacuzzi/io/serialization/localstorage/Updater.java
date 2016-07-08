package aanguita.jacuzzi.io.serialization.localstorage;

/**
 * Created by Alberto on 12/04/2016.
 */
public interface Updater {

    String update(VersionedLocalStorage versionedLocalStorage, String storedVersion);
}
