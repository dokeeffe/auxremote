package com.bobs.serialcommands;

import com.bobs.mount.TrackingMode;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 26/12/16.
 */
public class QueryAltMcPositionTest extends BaseCommandTest {

    private QueryAltMcPosition sut;

    @Before
    public void setup() {
        super.setup();
        sut = new QueryAltMcPosition(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001110100000003", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() {
        mount.setDecDegrees(-80.0);
        byte[] message = new byte[4];
        message[0] = (byte) 0x01;
        message[1] = (byte) 0xB8;
        message[2] = (byte) 0xCF;
        sut.handleMessage(message);
        assertEquals(-79.8, mount.getDecDegrees(), 0.1);
    }

    @Test
    public void handleMessage_badData() {
        mount.setDecDegrees(1.1);
        byte[] message = new byte[4];
        message[0] = (byte) 0x01;
        message[1] = (byte) 0xB8;
        message[2] = (byte) 0xCF;
        sut.handleMessage(message);
        assertEquals(1.1, mount.getDecDegrees(), 0.1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void handleMessage_eqSouthMode() {
        mount.setTrackingMode(TrackingMode.EQ_SOUTH);
        byte[] message = new byte[4];
        message[0] = (byte) 0x20;
        message[1] = (byte) 0xB8;
        message[2] = (byte) 0xCF;
        sut.handleMessage(message);
    }
}