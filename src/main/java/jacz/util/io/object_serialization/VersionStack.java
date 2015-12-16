package jacz.util.io.object_serialization;

import java.io.Serializable;
import java.util.ArrayDeque;

/**
 * A version stack for storing the version of a class and its parents
 */
public class VersionStack implements Serializable {

    private ArrayDeque<String> versions;

    public VersionStack(String version) {
        versions = new ArrayDeque<>();
        versions.push(version);
    }

    public VersionStack(String version, VersionStack parentVersions) {
        versions = new ArrayDeque<>(parentVersions.versions);
        versions.push(version);
    }

    public String retrieveVersion() {
        return versions.pop();
    }

    @Override
    public String toString() {
        return "VersionStack{" + versions + '}';
    }
}
