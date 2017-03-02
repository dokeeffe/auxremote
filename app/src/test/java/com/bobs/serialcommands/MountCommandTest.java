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
    public void bytesToDegrees_parkPositionDueSouthHorizionAz() throws Exception {
        //actual command = 500410027FEE0001
        assertEquals(180, sut.bytesToDegrees("7FEE00"), 0.1);
    }

    @Test
    public void bytesToDegrees_parkPositionDueSouthHorizionAlt() throws Exception {
        //actual command = 50041102E4E80E01
        assertEquals(360-38.2, sut.bytesToDegrees("E4E80E"), 0.1);
    }

    @Test
    public void bytesToDegrees_westOfMeridian219Deg() throws Exception {
        //actual command = 500410029BAB0501
        assertEquals(219, sut.bytesToDegrees("9BAB05"), 0.5);
    }



    @Test
    public void degreesToBytes() throws Exception {
        //actual values from debug logs
        byte[] result = sut.degreesToBytes(196.6681383054);
        assertEquals("8BDA56", DatatypeConverter.printHexBinary(result));
    }

}