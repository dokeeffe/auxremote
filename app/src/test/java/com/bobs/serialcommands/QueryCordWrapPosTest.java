package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class QueryCordWrapPosTest extends BaseCommandTest {

    private QueryCordWrapPos sut;

    @Before
    public void setup() {
        super.setup();
        sut = new QueryCordWrapPos(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001103C00000003", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        byte[] message = new byte[3];
        message[0] = (byte) 0x20;
        message[1] = (byte) 0xB8;
        message[2] = (byte) 0xCF;
        sut.handleMessage(message);
        assertEquals(46, mount.getCordWrapPosition(), 0.1);
    }

}