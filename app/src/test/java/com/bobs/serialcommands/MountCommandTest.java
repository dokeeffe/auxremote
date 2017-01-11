package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 29/12/16.
 */
public class MountCommandTest extends BaseCommandTest {

    private MountCommand sut;

    @Before
    public void setup() {
        sut = new MountCommand(null) {
            @Override
            public byte[] getCommand() {
                return new byte[0];
            }

            @Override
            public void handleMessage(byte[] message) {

            }
        };
    }

    @Test
    public void bytesToDegrees() throws Exception {
        assertEquals(360, sut.bytesToDegrees("ffffff"), 0);
    }

    @Test
    public void degreesToBytes() throws Exception {
        //actual values from debug logs
        byte[] result = sut.degreesToBytes(196.6681383054);
        assertEquals("8BDA56", DatatypeConverter.printHexBinary(result));
    }

}