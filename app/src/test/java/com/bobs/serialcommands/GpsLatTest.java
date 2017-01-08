package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 26/12/16.
 */
public class GpsLatTest extends BaseCommandTest {

    private GpsLat sut;

    @Before
    public void setup() {
        super.setup();
        this.sut = new GpsLat(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001B00100000003", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        sut.handleMessage(new byte[]{0x21,0x23,0x11});
        assertEquals(46.59,mount.getGpsLat(),0.01);
    }

}