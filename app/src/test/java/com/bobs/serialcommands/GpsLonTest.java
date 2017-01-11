package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 26/12/16.
 */
public class GpsLonTest extends BaseCommandTest {

    private GpsLon sut;

    @Before
    public void setup() {
        super.setup();
        this.sut = new GpsLon(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001B00200000003", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        sut.handleMessage(new byte[]{0x01, 0x23, 0x03});
        assertEquals(1.5, mount.getGpsLon(), 0.1);
    }

}