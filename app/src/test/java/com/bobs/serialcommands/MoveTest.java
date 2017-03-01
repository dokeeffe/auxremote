package com.bobs.serialcommands;

import com.bobs.mount.Axis;
import com.bobs.mount.TrackingState;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by dokeeffe on 1/2/17.
 */
public class MoveTest extends BaseCommandTest {

    @Before
    public void setUp() throws Exception {
        super.setup();
        mount.setTrackingState(TrackingState.IDLE);
    }

    @Test
    public void getCommand() throws Exception {
        Move move = new Move(mount, 9, Axis.ALT, true);
        assertEquals("5002112409000001", DatatypeConverter.printHexBinary(move.getCommand()));
        move = new Move(mount, 9, Axis.AZ, true);
        assertEquals("5002102409000001", DatatypeConverter.printHexBinary(move.getCommand()));
        move = new Move(mount, 9, Axis.ALT, false);
        assertEquals("5002112509000001", DatatypeConverter.printHexBinary(move.getCommand()));
        move = new Move(mount, 9, Axis.AZ, false);
        assertEquals("5002102509000001", DatatypeConverter.printHexBinary(move.getCommand()));
        move = new Move(mount, 0, Axis.ALT, true);
        assertEquals("5002112400000001", DatatypeConverter.printHexBinary(move.getCommand()));
        move = new Move(mount, 0, Axis.AZ, true);
        assertEquals("5002102400000001", DatatypeConverter.printHexBinary(move.getCommand()));
        move = new Move(mount, 0, Axis.ALT, false);
        assertEquals("5002112500000001", DatatypeConverter.printHexBinary(move.getCommand()));
        move = new Move(mount, 0, Axis.AZ, false);
        assertEquals("5002102500000001", DatatypeConverter.printHexBinary(move.getCommand()));
    }

    @Test
    public void handleMessage_ack_alt_move() throws Exception {
        Move MoveCommand = new Move(mount, 1, Axis.ALT, true);
        MoveCommand.handleMessage(ACK);
        assertEquals(TrackingState.SLEWING, mount.getTrackingState());
        assertTrue(mount.isAltSlewInProgress());
    }

    @Test
    public void handleMessage_ack_az_move() throws Exception {
        Move MoveCommand = new Move(mount, 1, Axis.AZ, true);
        MoveCommand.handleMessage(ACK);
        assertEquals(TrackingState.SLEWING, mount.getTrackingState());
        assertTrue(mount.isAzSlewInProgress());
    }

    @Test
    public void handleMessage_ack_for_abort() throws Exception {
        mount.setAltSlewInProgress(true);
        mount.setAzSlewInProgress(true);
        Move MoveCommand = new Move(mount, 0, Axis.ALT, true);
        MoveCommand.handleMessage(ACK);
        assertEquals(TrackingState.TRACKING, mount.getTrackingState());
        assertFalse(mount.isAltSlewInProgress());
        assertFalse(mount.isAzSlewInProgress());
    }



    @Test
    public void handleMessage_nonack() throws Exception {
        Move MoveCommand = new Move(mount, 1, Axis.ALT, true);
        MoveCommand.handleMessage(new byte[]{0x22});
        assertEquals(TrackingState.IDLE, mount.getTrackingState());
    }

}