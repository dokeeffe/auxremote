package com.bobs.io;

import com.bobs.mount.Axis;
import com.bobs.mount.Mount;
import com.bobs.mount.PecMode;
import com.bobs.mount.TrackingState;
import com.bobs.serialcommands.Move;
import com.bobs.serialcommands.PecPlayback;
import com.bobs.serialcommands.PecQueryAtIndex;
import com.bobs.serialcommands.PecQueryRecordDone;
import com.bobs.serialcommands.QuerySlewDone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the Fake adapter.
 */
public class FakeNexstarAuxAdapterTest {


    private FakeNexstarAuxAdapter sut;
    private Mount mount;

    @Before
    public void setUp() throws Exception {
        mount = new Mount();
        sut = new FakeNexstarAuxAdapter();
        new Thread(sut).start();
    }

    @Test
    public void queueCommand_pecQueryAtIndex() throws Exception {
        PecQueryAtIndex cmd = new PecQueryAtIndex(mount);
        sut.queueCommand(cmd);
        sut.waitForQueueEmpty();
        assertEquals(PecMode.INDEXING, mount.getPecMode());
        sut.queueCommand(cmd);
        sut.queueCommand(cmd);
        sut.queueCommand(cmd);
        sut.queueCommand(cmd);
        sut.waitForQueueEmpty();
        assertEquals(PecMode.IDLE, mount.getPecMode());
        assertTrue(mount.isPecIndexFound());
    }

    @Test
    public void queueCommand_querySlewDone() throws Exception {
        QuerySlewDone cmd = new QuerySlewDone(mount, Axis.AZ);
        sut.queueCommand(cmd);
        sut.waitForQueueEmpty();
        assertTrue(mount.isAzSlewInProgress());
        sut.queueCommand(cmd);
        sut.queueCommand(cmd);
        sut.queueCommand(cmd);
        sut.queueCommand(cmd);
        sut.waitForQueueEmpty();
        assertFalse(mount.isAzSlewInProgress());
    }

    @Test
    public void queueCommand_queryRecordDone() throws Exception {
        PecQueryRecordDone cmd = new PecQueryRecordDone(mount);
        sut.queueCommand(cmd);
        sut.waitForQueueEmpty();
        assertEquals(PecMode.RECORDING, mount.getPecMode());
        sut.queueCommand(cmd);
        sut.queueCommand(cmd);
        sut.queueCommand(cmd);
        sut.queueCommand(cmd);
        sut.waitForQueueEmpty();
        assertEquals(PecMode.IDLE, mount.getPecMode());
    }

    @Test
    public void queueCommand_pecPlayback() throws Exception {
        sut.queueCommand(new PecPlayback(mount, true));
        sut.waitForQueueEmpty();
        assertEquals(PecMode.PLAYING, mount.getPecMode());
        sut.queueCommand(new PecPlayback(mount, false));
        sut.waitForQueueEmpty();
        assertEquals(PecMode.IDLE, mount.getPecMode());
    }

    @Test
    public void queueCommand_move0() throws Exception {
        sut.queueCommand(new Move(mount, 0, Axis.ALT, true));
        sut.waitForQueueEmpty();
        assertEquals(TrackingState.TRACKING, mount.getTrackingState());
    }

    @Test
    public void queueCommand_move1() throws Exception {
        sut.queueCommand(new Move(mount, 1, Axis.ALT, true));
        sut.waitForQueueEmpty();
        assertEquals(TrackingState.SLEWING, mount.getTrackingState());
    }


    @After
    public void stop() throws Exception {
        sut.stop();
    }

    @Test
    public void isConnected() throws Exception {
        Thread.sleep(100); //thread startup....
        assertTrue(sut.isConnected());
    }

}