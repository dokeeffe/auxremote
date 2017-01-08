package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import com.bobs.mount.TrackingMode;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dokeeffe on 1/2/17.
 */
public class QueryAzMcPositionTest extends BaseCommandTest {
    
    QueryAzMcPosition sut;

    @Before
    public void setup() {
        super.setup();
        sut = new QueryAzMcPosition(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001100100000003", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() {
        byte[] message = new byte[3];
        message[0] = (byte)0x01;
        message[1] = (byte)0xB8;
        message[2] = (byte)0xCF;
        sut.handleMessage(message);
        assertTrue(mount.getRaHours()!=0.0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void handleMessage_eqSouthMode() {
        mount.setTrackingMode(TrackingMode.EQ_SOUTH);
        byte[] message = new byte[3];
        message[0] = (byte)0x20;
        message[1] = (byte)0xB8;
        message[2] = (byte)0xCF;
        sut.handleMessage(message);
    }

}