package aanguita.jacuzzi.hash;

/**
 * 32-bit hash code
 */
public final class HashCode32 extends HashCode {

    private final Integer hash;

    public HashCode32(Integer hash) {
        this.hash = hash;
    }

    /**
     * @param str hexadecimal number (non-signed). min: "00000000" or "0x00000000, max: "FFFFFFFF" or "0xFFFFFFFF"
     * @throws NumberFormatException
     */
    public HashCode32(String str) throws NumberFormatException {
        if (str.length() > 8) {
            throw new NumberFormatException("Invalid String (too many chars): " + str);
        }
        try {
            // a positive long (from 0 to 2^^32 - 1). If this number is bigger than Integer.MAX_VALUE, the
            // Long.intValue method itself will take care of performing the correct conversion
            Long l = Long.parseLong(str, 16);
            hash = l.intValue();
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid String (non-hexadecimal chars): " + str);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HashCode32)) {
            return false;
        }
        HashCode32 otherHashCode32 = (HashCode32) obj;
        return hash.equals(otherHashCode32.hash);
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    @Override
    public String toString() {
        return Integer.toHexString(hash);
    }
}
