package org.aanguita.jacuzzi.id;

/**
 * A base class for all classes that want to have a unique String identifier
 */
public class StringIdClass {

    /**
     * Unique string identifier
     */
    private final String id;

    /**
     * Class constructor
     */
    public StringIdClass() {
        id = AlphaNumFactory.getStaticId();
    }

    /**
     * Returns the unique id of this fsm
     *
     * @return the unique id of this fsm
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringIdClass)) return false;

        StringIdClass that = (StringIdClass) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
