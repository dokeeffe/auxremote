package com.bobs.io;

import com.bobs.coord.DefaultCalendarProvider;
import com.bobs.mount.*;
import com.bobs.serialcommands.*;
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
        mount.setTrackingMode(TrackingMode.EQ_NORTH);
        mount.setCalendarProvider(new DefaultCalendarProvider());
        mount.setLongitude(351.0);
        mount.setLatitude(52.0);
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
    public void syncAndCheckPosition() {
        sut.queueCommand(new SetAltMcPosition(mount,10.00));
        sut.queueCommand(new SetAzMcPosition(mount,180.00));
        sut.queueCommand(new QueryAltMcPosition(mount));
        sut.queueCommand(new QueryAzMcPosition(mount));
        sut.waitForQueueEmpty();
        assertNotEquals(0.0,mount.getRaHours(),0.1);
        assertNotEquals(0.0,mount.getDecDegrees(),0.1);
    }

    @Test
    public void gotoAndCheckPosition() throws InterruptedException {
        sut.queueCommand(new Goto(mount,10.00,Axis.ALT,true));
        sut.queueCommand(new Goto(mount,180.00,Axis.AZ, true));
        mount.setAltSlewInProgress(true);
        while(mount.isSlewing()) {
            sut.queueCommand(new QuerySlewDone(mount,Axis.ALT));
            Thread.sleep(100);
        }
        sut.queueCommand(new QueryAltMcPosition(mount));
        sut.queueCommand(new QueryAzMcPosition(mount));
        sut.waitForQueueEmpty();
        assertNotEquals(0.0,mount.getRaHours(),0.1);
        assertNotEquals(0.0,mount.getDecDegrees(),0.1);
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