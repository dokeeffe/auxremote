package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 26/12/16.
 */
public class GpsRecieverStatusTest extends BaseCommandTest {

    private GpsRecieverStatus sut;

    @Before
    public void setup() {
        super.setup();
        this.sut = new GpsRecieverStatus(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001B00800000002", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        sut.handleMessage(new byte[]{0x01, 0x00});
        assertEquals("Filter Reset To Raw GPS Solution", mount.getGpsReceiverStatus());
    }
}