package org.aanguita.jacuzzi.io.localstorage;

/**
 * Created by Alberto on 12/04/2016.
 */
public interface Updater {

    String update(VersionedLocalStorage versionedLocalStorage, String storedVersion);
}
