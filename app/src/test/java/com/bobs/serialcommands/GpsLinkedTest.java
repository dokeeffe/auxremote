package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;

/**
 * Created by dokeeffe on 26/12/16.
 */
public class GpsLinkedTest extends BaseCommandTest {

    private GpsLinked sut;

    @Before
    public void setup() {
        super.setup();
        this.sut = new GpsLinked(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001B03700000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        sut.handleMessage(ACK);
        assertTrue(mount.isGpsConnected());
        sut.handleMessage(PENDING);
        assertFalse(mount.isGpsConnected());
    }

}