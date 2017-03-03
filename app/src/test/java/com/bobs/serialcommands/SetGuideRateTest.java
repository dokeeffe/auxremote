package com.bobs.serialcommands;

import com.bobs.mount.GuideRate;
import com.bobs.mount.TrackingState;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static com.bobs.serialcommands.MountCommand.ALT_BOARD;
import static com.bobs.serialcommands.MountCommand.AZM_BOARD;
import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class SetGuideRateTest extends BaseCommandTest {

    @Before
    public void setUp() throws Exception {
        super.setup();
    }

    @Test
    public void getCommand_siderealRate() throws Exception {
        SetGuideRate gr = new SetGuideRate(mount, AZM_BOARD, GuideRate.SIDEREAL);
        assertEquals("50031006FFFF0001", DatatypeConverter.printHexBinary(gr.getCommand()));
    }

    @Test
    public void getCommand_lunarRate() throws Exception {
        SetGuideRate gr = new SetGuideRate(mount, AZM_BOARD, GuideRate.LUNAR);
        assertEquals("50031006FFFD0001", DatatypeConverter.printHexBinary(gr.getCommand()));
    }

    @Test
    public void getCommand_solarRate() throws Exception {
        SetGuideRate gr = new SetGuideRate(mount, AZM_BOARD, GuideRate.SOLAR);
        assertEquals("50031006FFFE0001", DatatypeConverter.printHexBinary(gr.getCommand()));
    }

    @Test
    public void getCommand_offRate() throws Exception {
        SetGuideRate gr = new SetGuideRate(mount, AZM_BOARD, GuideRate.OFF);
        assertEquals("5004100600000001", DatatypeConverter.printHexBinary(gr.getCommand()));
    }

    @Test
    public void getCommand_altOffRate() throws Exception {
        SetGuideRate gr = new SetGuideRate(mount, ALT_BOARD, GuideRate.OFF);
        assertEquals("5004110600000001", DatatypeConverter.printHexBinary(gr.getCommand()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCommand_badCommand() throws Exception {
        SetGuideRate gr = new SetGuideRate(mount, (byte) 0x22, GuideRate.OFF);
        gr.getCommand();
    }


    @Test
    public void handleMessage_switchOnTracking() throws Exception {
        mount.setTrackingState(TrackingState.IDLE);
        SetGuideRate gr = new SetGuideRate(mount, AZM_BOARD, GuideRate.SIDEREAL);
        gr.handleMessage(COMPLETE);
        assertEquals(TrackingState.TRACKING, mount.getTrackingState());
    }

    @Test
    public void handleMessage_switchOffTracking() throws Exception {
        mount.setTrackingState(TrackingState.TRACKING);
        SetGuideRate gr = new SetGuideRate(mount, AZM_BOARD, GuideRate.OFF);
        gr.handleMessage(COMPLETE);
        assertEquals(TrackingState.IDLE, mount.getTrackingState());
    }

}