package com.bobs.serialcommands;

import com.bobs.mount.PecMode;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class PecStartPlaybackTest extends BaseCommandTest {

    private PecStartPlayback sut;

    @Before
    public void setup() {
        super.setup();
        sut = new PecStartPlayback(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001100D00000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage_ack() throws Exception {
        sut.handleMessage(ACK);
        assertEquals(PecMode.PLAYING, mount.getPecMode());
    }

    @Test
    public void handleMessage_badMessage() throws Exception {
        sut.handleMessage(new byte[]{0x22});
        assertEquals(PecMode.IDLE, mount.getPecMode());
    }
}