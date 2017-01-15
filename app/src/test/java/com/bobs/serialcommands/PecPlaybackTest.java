package com.bobs.serialcommands;

import com.bobs.mount.PecMode;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class PecPlaybackTest extends BaseCommandTest {

    private PecPlayback sut;

    @Before
    public void setup() {
        super.setup();
        sut = new PecPlayback(mount, true);
        mount.setPecMode(PecMode.IDLE);
    }

    @Test
    public void getCommand_start() throws Exception {
        assertEquals("5002100D01000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void getCommand_stop() throws Exception {
        sut = new PecPlayback(mount, false);
        assertEquals("5002100D00000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage_ack_start() throws Exception {
        sut.handleMessage(ACK);
        assertEquals(PecMode.PLAYING, mount.getPecMode());
    }

    @Test
    public void handleMessage_ack_stop() throws Exception {
        sut = new PecPlayback(mount, false);
        sut.handleMessage(ACK);
        assertEquals(PecMode.IDLE, mount.getPecMode());
    }

    @Test
    public void handleMessage_badMessage() throws Exception {
        sut.handleMessage(new byte[]{0x22});
        assertEquals(PecMode.IDLE, mount.getPecMode());
    }
}