package org.aanguita.jacuzzi.hash;

import org.aanguita.jacuzzi.io.serialization.FragmentedByteArray;
import org.aanguita.jacuzzi.io.serialization.Offset;
import org.aanguita.jacuzzi.io.serialization.Serializer;

import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * A random-length CRC implementation
 */
public class CRC {

    public static byte[] addCRC(byte[] data, int CRCBytes, boolean addHeader) {
        FragmentedByteArray byteArray = new FragmentedByteArray();
        byte[] header = null;
        if (addHeader) {
            // add length of data and length of CRC
            header = Serializer.addArrays(Serializer.serialize(data.length), Serializer.serialize(CRCBytes));
            byteArray.add(Serializer.serialize(data.length), Serializer.serialize(CRCBytes));
        }
        return byteArray.add(data, calculateCRC(data, CRCBytes)).generateArray();
    }

    public static byte[] calculateCRC(byte[] data, int CRCBytes) {
        if (CRCBytes == 0) {
            return new byte[0];
        } else {
            CRC32 crc32 = new CRC32();
            crc32.update(data);
            int bytesForCRC = Math.min(CRCBytes, 4);
            byte[] crcData = Serializer.serialize(crc32.getValue());
            crcData = Arrays.copyOfRange(crcData, crcData.length - bytesForCRC, crcData.length);
            CRCBytes -= bytesForCRC;
            return Serializer.addArrays(crcData, calculateCRC(Serializer.addArrays(data, crcData), CRCBytes));
        }
    }

    /**
     * Extract the byte[] data from an array which contains CRC information.
     *
     * @param data the data with CRC. It must have the CRC header
     * @return the original data, properly validated
     */
    public static byte[] extractDataWithCRC(byte[] data) throws CRCMismatchException {
        return extractDataWithCRC(data, new Offset());
    }

    /**
     * Extract the byte[] data from an array which contains CRC information.
     *
     * @param data the data with CRC. It must have the CRC header
     * @return the original data, if the CRC validation is ok
     * @throws CRCMismatchException if the CRC validation failed
     */
    public static byte[] extractDataWithCRC(byte[] data, Offset offset) throws CRCMismatchException {
        int dataLength = Serializer.deserializeIntValue(data, offset);
        int CRCLength = Serializer.deserializeIntValue(data, offset);
        byte[] originalData = Arrays.copyOfRange(data, offset.value(), offset.value() + dataLength);
        offset.add(dataLength);
        byte[] existingCRC = Arrays.copyOfRange(data, offset.value(), offset.value() + CRCLength);
        offset.add(CRCLength);
        if (Arrays.equals(existingCRC, calculateCRC(originalData, CRCLength))) {
            return originalData;
        } else {
            throw new CRCMismatchException();
        }
    }
}
