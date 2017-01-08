package com.bobs.serialcommands;

import com.bobs.mount.Axis;
import com.bobs.mount.Mount;
import com.bobs.mount.TrackingState;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static com.bobs.serialcommands.MountCommand.ACK;
import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 1/2/17.
 */
public class MoveTest extends BaseCommandTest {

    @Before
    public void setUp() throws Exception {
        super.setup();
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
    public void handleMessage_ack() throws Exception {
        Move MoveCommand = new Move(mount, 1, Axis.ALT, true);
        MoveCommand.handleMessage(ACK);
        assertEquals(TrackingState.SLEWING,mount.getTrackingState());
    }

    @Test
    public void handleMessage_ack_for_abort() throws Exception {
        Move MoveCommand = new Move(mount, 0, Axis.ALT, true);
        MoveCommand.handleMessage(ACK);
        assertEquals(TrackingState.TRACKING,mount.getTrackingState());
    }

    @Test
    public void handleMessage_nonack() throws Exception {
        Move MoveCommand = new Move(mount, 1, Axis.ALT, true);
        MoveCommand.handleMessage(new byte[]{0x22});
        assertEquals(TrackingState.IDLE,mount.getTrackingState());
    }

}