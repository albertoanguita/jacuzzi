package aanguita.jacuzzi.hash;

/**
 * 128-bit hash code
 */
public final class HashCode128 extends HashCode {

    private HashCode64 id1;

    private HashCode64 id2;

    /**
     * @param str 128-bit long code in hexadecimal format
     * @throws NumberFormatException
     */
    public HashCode128(String str) throws NumberFormatException {
        if (str.length() > 32) {
            throw new NumberFormatException("Invalid String (too many chars): " + str);
        }
        if (str.length() > 16) {
            id1 = new HashCode64(str.substring(0, str.length() - 16));
            id2 = new HashCode64(str.substring(str.length() - 16, str.length()));
        } else {
            id1 = new HashCode64("0");
            id2 = new HashCode64(str);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HashCode128)) {
            return false;
        }
        HashCode128 otherHashCode128 = (HashCode128) obj;
        return id1.equals(otherHashCode128.id1) && id2.equals(otherHashCode128.id2);
    }

    @Override
    public int hashCode() {
        return id2.hashCode();
    }

    @Override
    public String toString() {
        return id1.toString() + id2.toString();
    }
}
