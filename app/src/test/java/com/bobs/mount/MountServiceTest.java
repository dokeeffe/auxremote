package com.bobs.mount;

import com.bobs.coord.CalendarProvider;
import com.bobs.coord.DefaultCalendarProvider;
import com.bobs.coord.Target;
import com.bobs.serialcommands.*;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import javax.xml.bind.DatatypeConverter;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        CalendarProvider calendarProvider = new DefaultCalendarProvider();
        mount = new Mount();
        fakeAuxAdapter.setMount(mount);
        ReflectionTestUtils.setField(mountService, "mount", mount);
        ReflectionTestUtils.setField(mountService, "auxAdapter", fakeAuxAdapter);
        mount.setTrackingState(TrackingState.TRACKING);
        mount.setTrackingMode(TrackingMode.EQ_NORTH);
        mount.setLatitude(52.2);
        mount.setLongitude(351.6);
        mount.setLocationSet(true);
        mountService.setPecPollInterval(10); //to speedup test
        mount.setCalendarProvider(calendarProvider);
        mount.setGpsUpdateTime(new Date());
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
        mountService.slew(target, false);
    }

    @Test
    public void slew_fastSlewSlowSlewCombo() throws Exception {
        mount.setAligned(true);
        Target target = new Target();
        target.setRaHours(1.2);
        target.setDec(30.3);

        mountService.slew(target, false);

        Iterator<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands().iterator();
        assertEquals(Goto.class, queuedCommands.next().getClass());
        assertEquals(Goto.class, queuedCommands.next().getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.next().getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.next().getClass());
        assertEquals(Goto.class, queuedCommands.next().getClass());
        assertEquals(Goto.class, queuedCommands.next().getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.next().getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.next().getClass());
        assertEquals(8, fakeAuxAdapter.getQueuedCommands().size());
    }

    @Test
    public void slew_slowSlewOnly() throws Exception {
        mount.setAligned(true);
        mount.setRaHours(1.21);
        mount.setDecDegrees(30.31);
        Target target = new Target();
        target.setRaHours(1.2);
        target.setDec(30.3);

        mountService.slew(target, false);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(Goto.class, queuedCommands.get(0).getClass());
        assertEquals(Goto.class, queuedCommands.get(1).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(2).getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.get(3).getClass());
        assertEquals(4, queuedCommands.size());
    }

    @Test
    public void park() throws Exception {
        mount.setAligned(true);
        Target target = new Target();
        target.setRaHours(1.2);
        target.setDec(30.3);

        mountService.park(target);

        Iterator<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands().iterator();
        assertEquals(Goto.class, queuedCommands.next().getClass());
        assertEquals(Goto.class, queuedCommands.next().getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.next().getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.next().getClass());
        assertEquals(Goto.class, queuedCommands.next().getClass());
        assertEquals(Goto.class, queuedCommands.next().getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.next().getClass());
        assertEquals(QuerySlewDone.class, queuedCommands.next().getClass());
        assertEquals(SetGuideRate.class, queuedCommands.next().getClass());
        assertEquals(SetGuideRate.class, queuedCommands.next().getClass());
        assertEquals(TrackingState.PARKED, mount.getTrackingState());
        assertEquals(10, fakeAuxAdapter.getQueuedCommands().size());
    }


    @Test(expected = RuntimeException.class)
    public void enforceSlewLimit_when_belowHorizion_then_exceptionThrown() throws Exception {
        mount.setAligned(true);
        mount.setLatitude(52.25338293632168);
        mount.setLongitude(351.63942525621803);
        Calendar cal = generateTestCalender();
        CalendarProvider mockCalendarProvider = mock(CalendarProvider.class);
        when(mockCalendarProvider.provide()).thenReturn(cal);
        mount.setCalendarProvider(mockCalendarProvider);

        mount.setRaHours(19.696695);
        mount.setDecDegrees(-38.100300);
        mount.setAltSlewInProgress(true);
        mount.setAzSlewInProgress(true);

        mountService.monitorSlew();
    }


    @Test
    public void unpark_then_guideRateSetPositionAndGpsQueried() throws Exception {
        mount.setAligned(false);
        mount.setGpsUpdateTime(null); //force gps update
        mount.setTrackingState(TrackingState.PARKED);
        Target target = new Target();
        target.setRaHours(1.2);
        target.setDec(30.3);

        mountService.unpark(target);

        Iterator<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands().iterator();
        assertEquals(SetGuideRate.class, queuedCommands.next().getClass());
        assertEquals(SetGuideRate.class, queuedCommands.next().getClass());
        assertEquals(SetGuideRate.class, queuedCommands.next().getClass());
        assertEquals(SetAltMcPosition.class, queuedCommands.next().getClass());
        assertEquals(SetAzMcPosition.class, queuedCommands.next().getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.next().getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.next().getClass());
        assertEquals(GpsLinked.class, queuedCommands.next().getClass());
        assertEquals(GpsLat.class, queuedCommands.next().getClass());
        assertEquals(GpsLon.class, queuedCommands.next().getClass());
        assertEquals(QueryCordWrap.class, queuedCommands.next().getClass());
        assertEquals(EnableCordWrap.class, queuedCommands.next().getClass());
        assertEquals(QueryCordWrapPos.class, queuedCommands.next().getClass());
        assertTrue(mount.isAligned());
        assertEquals(TrackingState.TRACKING, mount.getTrackingState());
        assertEquals(13, fakeAuxAdapter.getQueuedCommands().size());
    }

    @Test
    public void startTracking() throws Exception {
        mount.setGuideRate(GuideRate.SIDEREAL);
        mountService.startTracking();

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(SetGuideRate.class, queuedCommands.get(0).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(1).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(2).getClass());
        assertEquals(3, queuedCommands.size());
        GuideRate rate = (GuideRate) ReflectionTestUtils.getField(queuedCommands.get(2), "guideRate");
        assertEquals(GuideRate.SIDEREAL, rate);
        assertEquals(TrackingState.TRACKING, mount.getTrackingState());
    }

    @Test
    public void startTracking_off() throws Exception {
        mount.setGuideRate(GuideRate.OFF);
        mountService.startTracking();

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(SetGuideRate.class, queuedCommands.get(0).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(1).getClass());
        assertEquals(SetGuideRate.class, queuedCommands.get(2).getClass());
        assertEquals(3, queuedCommands.size());
        GuideRate rate = (GuideRate) ReflectionTestUtils.getField(queuedCommands.get(2), "guideRate");
        assertEquals(GuideRate.OFF, rate);
        assertEquals(TrackingState.IDLE, mount.getTrackingState());
    }

    @Test
    public void connect() throws Exception {
        mountService.connect();

        Iterator<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands().iterator();
        assertEquals(EnableCordWrap.class, queuedCommands.next().getClass());
        assertEquals(QueryCordWrap.class, queuedCommands.next().getClass());
        assertEquals(SetGuideRate.class, queuedCommands.next().getClass());
        assertEquals(SetGuideRate.class, queuedCommands.next().getClass());
        assertEquals(SetGuideRate.class, queuedCommands.next().getClass());
        assertEquals(5, fakeAuxAdapter.getQueuedCommands().size());
    }

    @Test
    public void queryMountState_when_gpsInfoUptoDate_then_onlyPositionAndCordwrapAreQueried() throws Exception {
        mount.setGpsUpdateTime(new Date());
        mountService.queryMountState();

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(0).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(QueryCordWrap.class, queuedCommands.get(2).getClass());
        assertEquals(3, queuedCommands.size());
    }

    @Test
    public void queryMountState_when_gpsInfoStale_then_gpsAndPositionIsQueried() throws Exception {
        mount.setGpsUpdateTime(DateUtils.addHours(new Date(), -2));
        mountService.queryMountState();

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(0).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(GpsLinked.class, queuedCommands.get(2).getClass());
        assertEquals(GpsLat.class, queuedCommands.get(3).getClass());
        assertEquals(GpsLon.class, queuedCommands.get(4).getClass());
        assertEquals(QueryCordWrap.class, queuedCommands.get(5).getClass());
        assertEquals(6, queuedCommands.size());
    }

    @Test
    public void queryMountState_when_mountSlewing_then_noQueriesMade() throws Exception {
        mount.setGpsUpdateTime(DateUtils.addHours(new Date(), -2));
        mount.setAltSlewInProgress(true);
        mount.setAzSlewInProgress(true);
        mountService.queryMountState();

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(0, queuedCommands.size());
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
    public void moveAxis_withRate0() throws Exception {
        Target target = new Target();
        target.setMotion("south");
        target.setMotionRate(0);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(1, startRate);
    }

    @Test
    public void moveAxis_withRate1() throws Exception {
        Target target = new Target();
        target.setMotion("south");
        target.setMotionRate(1);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(3, startRate);
    }

    @Test
    public void moveAxis_withRate2() throws Exception {
        Target target = new Target();
        target.setMotion("south");
        target.setMotionRate(2);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(6, startRate);
    }

    @Test
    public void moveAxis_withRate3() throws Exception {
        Target target = new Target();
        target.setMotion("south");
        target.setMotionRate(3);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(9, startRate);
    }

    @Test
    public void moveAxis_south() throws Exception {
        Target target = new Target();
        target.setMotion("south");
        target.setMotionRate(1);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(4, queuedCommands.size());
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
        assertEquals(4, queuedCommands.size());
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
        assertEquals(4, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(2).getClass());
        assertEquals(QueryCordWrap.class, queuedCommands.get(3).getClass());
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
        assertEquals(4, queuedCommands.size());
        assertEquals(Move.class, queuedCommands.get(0).getClass());
        assertEquals(QueryAzMcPosition.class, queuedCommands.get(1).getClass());
        assertEquals(QueryAltMcPosition.class, queuedCommands.get(2).getClass());
        assertEquals(QueryCordWrap.class, queuedCommands.get(3).getClass());
        Axis startAxis = (Axis) ReflectionTestUtils.getField(queuedCommands.get(0), "axis");
        boolean startPosDir = (boolean) ReflectionTestUtils.getField(queuedCommands.get(0), "positive");
        int startRate = (int) ReflectionTestUtils.getField(queuedCommands.get(0), "rate");
        assertEquals(3, startRate);
        assertEquals(Axis.AZ, startAxis);
        assertTrue(startPosDir);
    }

    @Test
    public void moveAxis_abort() throws Exception {
        mount.setGpsUpdateTime(new Date());
        Target target = new Target();
        target.setMotion("abort");
        target.setMotionRate(1);

        mountService.moveAxis(target);

        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(5, queuedCommands.size());
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
    public void updateMount_setLocation() throws Exception {
        Mount updated = new Mount();
        updated.setLatitude(52.20);
        updated.setLongitude(-8.00);
        mountService.updateMount(updated);
        assertEquals(52.2, mount.getLatitude(), 0);
        assertEquals(-8.00, mount.getLongitude(), 0);
    }

    @Test
    public void startPecOperation_index() throws Exception {
        mountService.startPecOperation(PecMode.INDEXING);
        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(2, queuedCommands.size());
        assertEquals(PecSeekIndex.class, queuedCommands.get(0).getClass());
        assertEquals(PecQueryAtIndex.class, queuedCommands.get(1).getClass());

    }

    @Test
    public void startPecOperation_rec() throws Exception {
        mountService.startPecOperation(PecMode.RECORDING);
        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(2, queuedCommands.size());
        assertEquals(PecStartRecording.class, queuedCommands.get(0).getClass());
        assertEquals(PecQueryRecordDone.class, queuedCommands.get(1).getClass());

    }

    @Test
    public void startPecOperation_play() throws Exception {
        mountService.startPecOperation(PecMode.PLAYING);
        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(1, queuedCommands.size());
        assertEquals(PecPlayback.class, queuedCommands.get(0).getClass());

    }

    @Test
    public void startPecOperation_idle() throws Exception {
        mountService.startPecOperation(PecMode.IDLE);
        List<MountCommand> queuedCommands = fakeAuxAdapter.getQueuedCommands();
        assertEquals(2, queuedCommands.size());
        assertEquals(PecStopRecording.class, queuedCommands.get(0).getClass());
        assertEquals(PecPlayback.class, queuedCommands.get(1).getClass());

    }

    private Calendar generateTestCalender() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 12);
        cal.set(Calendar.SECOND, 18);
        return cal;
    }

}