package org.aanguita.jacuzzi.io.serialization;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Created by Alberto on 23/10/2016.
 */
public class HexTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private byte[] array;

    @Before
    public void setup() {
        array = new byte[4];
        array[0] = (byte) 209;
        array[1] = (byte) 166;
        array[2] = (byte) 16;
        array[3] = (byte) 0;
    }

    @Test
    public void asHex() throws Exception {
        array[0] = (byte) 209;
        array[1] = (byte) 166;
        array[2] = (byte) 16;
        array[3] = (byte) 0;
        assertEquals("d1a61000", Hex.asHex(array));
    }

    @Test
    public void asHex1() throws Exception {
        byte[] array = new byte[4];
        array[0] = (byte) 209;
        array[1] = (byte) 166;
        array[2] = (byte) 16;
        array[3] = (byte) 0;
        assertEquals("d1a61000", Hex.asHex(array, false));
        assertEquals("D1A61000", Hex.asHex(array, true));
    }

    @Test
    public void asHex2() throws Exception {
        assertEquals("00", Hex.asHex((byte) 0, false));
        assertEquals("00", Hex.asHex((byte) 0, true));
        assertEquals("10", Hex.asHex((byte) 16, false));
        assertEquals("10", Hex.asHex((byte) 16, true));
        assertEquals("a6", Hex.asHex((byte) 166, false));
        assertEquals("A6", Hex.asHex((byte) 166, true));
        assertEquals("d1", Hex.asHex((byte) 209, false));
        assertEquals("D1", Hex.asHex((byte) 209, true));
    }

    @Test
    public void toHexChar() throws Exception {
        assertEquals('0', Hex.toHexChar("0000", true));
        assertEquals('0', Hex.toHexChar("0000", false));
        assertEquals('3', Hex.toHexChar("0011", true));
        assertEquals('3', Hex.toHexChar("0011", false));
        assertEquals('5', Hex.toHexChar("0101", true));
        assertEquals('5', Hex.toHexChar("0101", false));
        assertEquals('8', Hex.toHexChar("1000", true));
        assertEquals('8', Hex.toHexChar("1000", false));
        assertEquals('A', Hex.toHexChar("1010", true));
        assertEquals('a', Hex.toHexChar("1010", false));
        assertEquals('E', Hex.toHexChar("1110", true));
        assertEquals('e', Hex.toHexChar("1110", false));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid argument. Must be non null or of length 4. Received null");
        Hex.toHexChar(null, true);
        thrown.expectMessage("Invalid argument. Must be non null or of length 4. Received 010");
        Hex.toHexChar("010", true);
        thrown.expectMessage("Invalid argument. Must be a string or of zeros and ones. Received 0150");
        Hex.toHexChar("0150", true);
    }

    @Test
    public void asBinary() throws Exception {
        assertEquals("0000", Hex.asBinary("0"));
        assertEquals("11111111", Hex.asBinary("fF"));
        assertEquals("11000011", Hex.asBinary("C3"));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid hex char received: i");
        Hex.asBinary("ijk");
    }

    @Test
    public void toFourBits() throws Exception {
        assertEquals("0000", Hex.toFourBits('0'));
        assertEquals("0011", Hex.toFourBits('3'));
        assertEquals("0100", Hex.toFourBits('4'));
        assertEquals("1000", Hex.toFourBits('8'));
        assertEquals("1010", Hex.toFourBits('a'));
        assertEquals("1100", Hex.toFourBits('C'));
        assertEquals("1111", Hex.toFourBits('f'));
        assertEquals("1111", Hex.toFourBits('F'));
        assertEquals("1111", Hex.toFourBits('F'));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid hex char received: x");
        Hex.toFourBits('x');
        thrown.expectMessage("Invalid hex char received: J");
        Hex.toFourBits('J');
    }

}