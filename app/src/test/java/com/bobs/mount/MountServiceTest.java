package com.bobs.mount;

import com.bobs.coord.Target;
import com.bobs.serialcommands.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by dokeeffe on 31/12/16.
 */
public class MountServiceTest {

    private MountService mountService;
    private Mount mount;
    private FakeAuxAdapter fakeAuxAdapter;

    @Before
    public void setUp() throws Exception {
        mountService = new MountService();
        fakeAuxAdapter = new FakeAuxAdapter();
        mount = new Mount();
        fakeAuxAdapter.setMount(mount);
        ReflectionTestUtils.setField(mountService, "mount", mount);
        ReflectionTestUtils.setField(mountService, "auxAdapter", fakeAuxAdapter);
        mount.setTrackingState(TrackingState.TRACKING);
        mount.setTrackingMode(TrackingMode.EQ_NORTH);
        mount.setGpsLat(52.2);
        mount.setGpsLon(351.6);
        mount.setLocationSet(true);
    }

    @Test
    public void sync() throws Exception {
        Target target = new Target();
        target.setRaHours(1.2);
        target.setDec(30.3);

        mountService.sync(target);

        ArgumentCaptor<MountCommand> argument = ArgumentCaptor.forClass(MountCommand.class);
        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertTrue(mount.isAligned());
        assertEquals(SetAltMcPosition.class, queuedCommands.get(0).getClass());
        assertEquals(SetAzMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(2).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(3).getClass());
        assertEquals("50041104158BF201", toHex(queuedCommands.get(0).getCommand()));
    }

    private String toHex(byte[] data) {
        return DatatypeConverter.printHexBinary(data);
    }

    @Test(expected = IllegalStateException.class)
    public void slew_notAligned_throwsException() throws Exception {
        mount.setAligned(false);
        Target target = new Target();
        mountService.slew(target);
    }

    @Test
    public void slew_fastSlewSlowSlewCombo() throws Exception {
        mount.setAligned(true);
        Target target = new Target();
        target.setRaHours(1.2);
        target.setDec(30.3);

        mountService.slew(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(Goto.class, queuedCommands.get(0).getClass());
        assertEquals(Goto.class, queuedCommands.get(1).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(2).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(3).getClass());
        assertEquals(Goto.class, queuedCommands.get(4).getClass());
        assertEquals(Goto.class, queuedCommands.get(5).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(6).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(7).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(8).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(9).getClass());
        assertEquals(10, queuedCommands.size());
    }

    @Test
    public void slew_slowSlewOnly() throws Exception {
        mount.setAligned(true);
        mount.setRaHours(1.21);
        mount.setDecDegrees(30.31);
        Target target = new Target();
        target.setRaHours(1.2);
        target.setDec(30.3);

        mountService.slew(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(Goto.class, queuedCommands.get(0).getClass());
        assertEquals(Goto.class, queuedCommands.get(1).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(2).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(3).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(4).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(5).getClass());
        assertEquals(6, queuedCommands.size());
    }

    @Test
    public void park() throws Exception {
        mount.setAligned(true);
        Target target = new Target();
        target.setRaHours(1.2);
        target.setDec(30.3);

        mountService.park(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(Goto.class, queuedCommands.get(0).getClass());
        assertEquals(Goto.class, queuedCommands.get(1).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(2).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(3).getClass());
        assertEquals(Goto.class, queuedCommands.get(4).getClass());
        assertEquals(Goto.class, queuedCommands.get(5).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(6).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(7).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(8).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(9).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(10).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(11).getClass());
        assertEquals(TrackingState.PARKED, mount.getTrackingState());
        assertEquals(12, queuedCommands.size());
    }

    @Test
    public void unpark() throws Exception {
        mount.setAligned(false);
        mount.setTrackingState(TrackingState.PARKED);
        Target target = new Target();
        target.setRaHours(1.2);
        target.setDec(30.3);

        mountService.unpark(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(SetGuideRate.class, queuedCommands.get(0).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(1).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(2).getClass());
        assertEquals(SetAltMcPosition.class, queuedCommands.get(3).getClass());
        assertEquals(SetAzMcPosition.class, queuedCommands.get(4).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(5).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(6).getClass());
        assertTrue(mount.isAligned());
        assertEquals(TrackingState.TRACKING, mount.getTrackingState());
        assertEquals(7, queuedCommands.size());
    }

    @Test
    public void startTracking() throws Exception {
        mountService.startTracking();

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(SetGuideRate.class, queuedCommands.get(0).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(1).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(2).getClass());
        assertEquals(3, queuedCommands.size());
    }

    @Test
    public void connect() throws Exception {
        mountService.connect();

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(QueryCordWrapPos.class, queuedCommands.get(0).getClass());
        assertEquals(EnableCordWrap.class, queuedCommands.get(1).getClass());
        assertEquals(QueryCordWrap.class, queuedCommands.get(2).getClass());
        assertEquals(3, queuedCommands.size());
    }

    @Test
    public void queryMountState() throws Exception {
        mountService.queryMountState();

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(0).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(2, queuedCommands.size());
    }

    @Test
    public void guide_west() throws Exception {
        Target target = new Target();
        target.setGuidePulseDurationMs(10.0);
        target.setMotion("west");

        mountService.guide(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(2, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(Move.class, queuedCommands.get(1).getClass());
        Axis startAxis = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean startPosDir = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        int stopRate = (int) ReflectionTestUtils.getField(queuedCommands.get(1), "rate");
        assertEquals(1, startRate);
        assertEquals(Axis.AZ, startAxis);
        assertTrue(startPosDir);
        assertEquals(0, stopRate);
    }

    @Test
    public void guide_east() throws Exception {
        Target target = new Target();
        target.setGuidePulseDurationMs(10.0);
        target.setMotion("east");

        mountService.guide(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(2, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(Move.class, queuedCommands.get(1).getClass());
        Axis startAxis = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean startPosDir = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        int stopRate = (int) ReflectionTestUtils.getField(queuedCommands.get(1), "rate");
        assertEquals(1, startRate);
        assertEquals(Axis.AZ, startAxis);
        assertFalse(startPosDir);
        assertEquals(0, stopRate);
    }

    @Test
    public void guide_north() throws Exception {
        Target target = new Target();
        target.setGuidePulseDurationMs(10.0);
        target.setMotion("north");

        mountService.guide(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(2, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(Move.class, queuedCommands.get(1).getClass());
        Axis startAxis = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean startPosDir = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        int stopRate = (int) ReflectionTestUtils.getField(queuedCommands.get(1), "rate");
        assertEquals(1, startRate);
        assertEquals(Axis.ALT, startAxis);
        assertTrue(startPosDir);
        assertEquals(0, stopRate);
    }

    @Test
    public void guide_south() throws Exception {
        Target target = new Target();
        target.setGuidePulseDurationMs(10.0);
        target.setMotion("south");

        mountService.guide(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(2, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(Move.class, queuedCommands.get(1).getClass());
        Axis startAxis = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean startPosDir = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        int stopRate = (int) ReflectionTestUtils.getField(queuedCommands.get(1), "rate");
        assertEquals(1, startRate);
        assertEquals(Axis.ALT, startAxis);
        assertFalse(startPosDir);
        assertEquals(0, stopRate);
    }

    @Test
    public void moveAxis_south() throws Exception {
        Target target = new Target();
        target.setMotion("south");
        target.setMotionRate(1);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(3, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(2).getClass());
        Axis startAxis = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean startPosDir = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(3, startRate);
        assertEquals(Axis.ALT, startAxis);
        assertFalse(startPosDir);
    }

    @Test
    public void moveAxis_north() throws Exception {
        Target target = new Target();
        target.setMotion("north");
        target.setMotionRate(1);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(3, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(2).getClass());
        Axis startAxis = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean startPosDir = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(3, startRate);
        assertEquals(Axis.ALT, startAxis);
        assertTrue(startPosDir);
    }

    @Test
    public void moveAxis_east() throws Exception {
        Target target = new Target();
        target.setMotion("east");
        target.setMotionRate(1);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(3, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(2).getClass());
        Axis startAxis = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean startPosDir = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(3, startRate);
        assertEquals(Axis.AZ, startAxis);
        assertFalse(startPosDir);
    }

    @Test
    public void moveAxis_west() throws Exception {
        Target target = new Target();
        target.setMotion("west");
        target.setMotionRate(1);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(3, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(2).getClass());
        Axis startAxis = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean startPosDir = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(3, startRate);
        assertEquals(Axis.AZ, startAxis);
        assertTrue(startPosDir);
    }

    @Test
    public void moveAxis_abort() throws Exception {
        Target target = new Target();
        target.setMotion("abort");
        target.setMotionRate(1);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(4, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(Move.class, queuedCommands.get(1).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(2).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(3).getClass());
        Axis ax1 = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean posDir1 = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int rate1 = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(0, rate1);
        assertEquals(Axis.ALT, ax1);
        assertFalse(posDir1);
        Axis ax2 = (Axis) ReflectionTestUtils.getField(queuedCommands.get(1), "axis");
        boolean posDir2 = (boolean) ReflectionTestUtils.getField(queuedCommands.get(1), "positive");
        int rate2 = (int) ReflectionTestUtils.getField(queuedCommands.get(1), "rate");
        assertEquals(0, rate2);
        assertEquals(Axis.AZ, ax2);
        assertFalse(posDir2);
    }

    @Test
    public void getMount() throws Exception {
        assertEquals(mount, mountService.getMount());
    }

    @Test(expected = IllegalStateException.class)
    public void getMount_notConnected_throwsException() throws Exception {
        fakeAuxAdapter.setConnected(false);
        assertEquals(mount, mountService.getMount());
    }

    @Test
    public void updateMount_changeSerialPort() throws Exception {
        Mount updated = new Mount();
        updated.setSerialPort("/dev/ttyUSB11");
        mountService.updateMount(updated);
        assertEquals("/dev/ttyUSB11", mount.getSerialPort());
    }

    @Test
    public void updateMount_changeSlewLimitAlt() throws Exception {
        Mount updated = new Mount();
        updated.setSlewLimitAlt(11.11);
        mountService.updateMount(updated);
        assertEquals(11.11, mount.getSlewLimitAlt(), 0);
    }

    @Test
    public void updateMount_changeGuideRate() throws Exception {
        Mount updated = new Mount();
        updated.setGuideRate(GuideRate.LUNAR);
        mountService.updateMount(updated);
        assertEquals(GuideRate.LUNAR, mount.getGuideRate());
    }

    @Test
    public void updateMount_changeSlewLimitAz() throws Exception {
        Mount updated = new Mount();
        updated.setSlewLimitAz(22.22);
        mountService.updateMount(updated);
        assertEquals(22.22, mount.getSlewLimitAz(), 0);
    }

    @Test
    public void updateMount_changePecMode() throws Exception {
        Mount updated = new Mount();
        updated.setPecMode(PecMode.PLAYING);
        mountService.updateMount(updated);
        assertEquals(PecMode.PLAYING, mount.getPecMode());
    }

    @Test
    public void startPecOperation_index() throws Exception {
        mount.setPecMode(PecMode.INDEXING);
        mountService.startPecOperation();
        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(2, queuedCommands.size());
        assertEquals(PecSeekIndex.class, queuedCommands.get(0).getClass());
        assertEquals(PecQueryAtIndex.class, queuedCommands.get(1).getClass());

    }

    @Test
    public void startPecOperation_rec() throws Exception {
        mount.setPecMode(PecMode.RECORDING);
        mountService.startPecOperation();
        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(2, queuedCommands.size());
        assertEquals(PecStartRecording.class, queuedCommands.get(0).getClass());
        assertEquals(PecQueryRecordDone.class, queuedCommands.get(1).getClass());

    }

    @Test
    public void startPecOperation_play() throws Exception {
        mount.setPecMode(PecMode.PLAYING);
        mountService.startPecOperation();
        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(1, queuedCommands.size());
        assertEquals(PecStartPlayback.class, queuedCommands.get(0).getClass());

    }

    @Test
    public void startPecOperation_idle() throws Exception {
        mount.setPecMode(PecMode.IDLE);
        mountService.startPecOperation();
        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(1, queuedCommands.size());
        assertEquals(PecStopRecording.class, queuedCommands.get(0).getClass());

    }

}