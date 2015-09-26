package jacz.util.hash;

import jacz.util.io.object_serialization.MutableOffset;
import jacz.util.io.object_serialization.Serializer;

import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * A random-length CRC implementation
 */
public class CRC {

    public static byte[] addCRC(byte[] data, int CRCBytes, boolean addHeader) {
        byte[] header = null;
        if (addHeader) {
            // add length of data and length of CRC
            header = Serializer.addArrays(Serializer.serialize(data.length), Serializer.serialize(CRCBytes));
        }
        return Serializer.addArrays(header, data, calculateCRC(data, CRCBytes));
    }

    public static byte[] calculateCRC(byte[] data, int CRCBytes) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        int bytesForCRC = Math.min(CRCBytes, 4);
        byte[] crcData = Serializer.serialize(crc32.getValue());
        crcData = Arrays.copyOfRange(crcData, crcData.length - bytesForCRC, crcData.length);
        CRCBytes -= bytesForCRC;
        if (CRCBytes == 0) {
            return crcData;
        } else {
            return Serializer.addArrays(crcData, calculateCRC(Serializer.addArrays(data, crcData), CRCBytes));
        }
    }

    /**
     * Extract the byte[] data from an array which contains CRC information.
     *
     * @param data the data with CRC. It must have the CRC header
     * @return the original data, properly validated
     */
    public static byte[] extractDataWithCRC(byte[] data) throws InvalidCRCException {
        return extractDataWithCRC(data, new MutableOffset());
    }

    /**
     * Extract the byte[] data from an array which contains CRC information.
     *
     * @param data the data with CRC. It must have the CRC header
     * @return the original data, if the CRC validation is ok
     * @throws InvalidCRCException if the CRC validation failed
     */
    public static byte[] extractDataWithCRC(byte[] data, MutableOffset mutableOffset) throws InvalidCRCException {
        int dataLength = Serializer.deserializeIntValue(data, mutableOffset);
        int CRCLength = Serializer.deserializeIntValue(data, mutableOffset);
        byte[] originalData = Arrays.copyOfRange(data, mutableOffset.value(), mutableOffset.value() + dataLength);
        mutableOffset.add(dataLength);
        byte[] existingCRC = Arrays.copyOfRange(data, mutableOffset.value(), mutableOffset.value() + CRCLength);
        mutableOffset.add(CRCLength);
        if (Arrays.equals(existingCRC, calculateCRC(originalData, CRCLength))) {
            return originalData;
        } else {
            throw new InvalidCRCException();
        }
    }
}
