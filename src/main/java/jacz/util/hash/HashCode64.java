package jacz.util.hash;

/**
 * 64-bit hash code
 */
public final class HashCode64 extends HashCode {

    private HashCode32 hash1;

    private HashCode32 hash2;

    public HashCode64(Long hash) {
        this(Long.toHexString(hash));
    }

    public HashCode64(String str) throws NumberFormatException {
        if (str.length() > 16) {
            throw new IllegalArgumentException("Invalid String (too many chars): " + str);
        }
        if (str.length() > 8) {
            hash1 = new HashCode32(str.substring(0, str.length() - 8));
            hash2 = new HashCode32(str.substring(str.length() - 8, str.length()));
        } else {
            hash1 = new HashCode32("0");
            hash2 = new HashCode32(str);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HashCode64)) {
            return false;
        }
        HashCode64 otherHashCode64 = (HashCode64) obj;
        return hash1.equals(otherHashCode64.hash1) && hash2.equals(otherHashCode64.hash2);
    }

    @Override
    public int hashCode() {
        return hash2.hashCode();
    }

    @Override
    public String toString() {
        return hash1.toString() + hash2.toString();
    }
}
