package com.bobs.serialcommands;

import com.bobs.mount.Axis;
import com.bobs.mount.TrackingState;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 1/2/17.
 */
public class GotoTest extends BaseCommandTest {

    @Before
    public void setUp() throws Exception {
        super.setup();
        mount.setTrackingState(TrackingState.IDLE);
    }

    @Test
    public void getCommand() throws Exception {
        Goto gotoCommand = new Goto(mount, 24.0, Axis.ALT, true);
        assertEquals("5004110211111101", DatatypeConverter.printHexBinary(gotoCommand.getCommand()));
        gotoCommand = new Goto(mount, 24.0, Axis.AZ, true);
        assertEquals("5004100211111101", DatatypeConverter.printHexBinary(gotoCommand.getCommand()));
        gotoCommand = new Goto(mount, 24.0, Axis.ALT, false);
        assertEquals("5004111711111101", DatatypeConverter.printHexBinary(gotoCommand.getCommand()));
        gotoCommand = new Goto(mount, 24.0, Axis.AZ, false);
        assertEquals("5004101711111101", DatatypeConverter.printHexBinary(gotoCommand.getCommand()));
    }

    @Test
    public void handleMessage_ack() throws Exception {
        Goto gotoCommand = new Goto(mount, 24.0, Axis.ALT, true);
        gotoCommand.handleMessage(ACK);
        assertEquals(TrackingState.SLEWING, mount.getTrackingState());

    }

    @Test
    public void handleMessage_nonack() throws Exception {
        Goto gotoCommand = new Goto(mount, 24.0, Axis.ALT, true);
        gotoCommand.handleMessage(new byte[]{0x22});
        assertEquals(TrackingState.IDLE, mount.getTrackingState());
    }

}