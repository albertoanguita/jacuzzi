package aanguita.jacuzzi.io.serialization;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

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

    public VersionStack(ArrayList<String> versionList) {
        versions = new ArrayDeque<>(versionList);
    }

    public String retrieveVersion() {
        return versions.pop();
    }

    public ArrayList<String> toArrayList() {
        return new ArrayList<>(versions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionStack that = (VersionStack) o;

        return Arrays.equals(versions.toArray(), that.versions.toArray());
    }

    @Override
    public int hashCode() {
        return versions.hashCode();
    }

    @Override
    public String toString() {
        return "VersionStack{" + versions + '}';
    }
}
