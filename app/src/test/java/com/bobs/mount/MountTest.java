package com.bobs.mount;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * Created by dokeeffe on 2/2/17.
 */
public class MountTest {

    @Test
    public void isGpsInfoOld_when_newMount_then_true() {
        Mount mount = new Mount();
        assertTrue(mount.isGpsInfoOld());
    }

    @Test
    public void isGpsInfoOld_when_moreThan1HourOld_then_true() {
        Mount mount = new Mount();
        mount.setGpsUpdateTime(DateUtils.addHours(new Date(), -2));
        assertTrue(mount.isGpsInfoOld());
    }

    @Test
    public void isGpsInfoOld_when_lessThan1HourOld_then_false() {
        Mount mount = new Mount();
        mount.setGpsUpdateTime(DateUtils.addMinutes(new Date(), -20));
        assertFalse(mount.isGpsInfoOld());
    }

    @Test
    public void saveState_then_fileIsSaved() {
        Mount mount = new Mount();
        mount.saveState();
        File file = new File(System.getProperty("user.home"), "auxremote-mount.json");
        assertTrue(file.exists());
    }

    @Test
    public void loadPersistedState_whenFileExists_then_stateRestored() {
        Mount mount = new Mount();
        mount.setLatitude(11.11);
        mount.setLongitude(22.22);
        mount.setLocationSet(true);
        mount.setSerialPort("/dev/ttyUSB0");
        mount.setTrackingState(TrackingState.TRACKING);
        mount.setRaHours(2.22);
        mount.setDecDegrees(32.1);
        mount.setAligned(true);
        mount.saveState();
        Mount restored = new Mount();

        restored.loadPersistedState(new File(System.getProperty("user.home"), "auxremote-mount.json"));
        assertEquals(11.11, restored.getLatitude(), 0);
        assertEquals(22.22, restored.getLongitude(), 0);
        assertTrue(restored.isLocationSet());
        assertEquals("/dev/ttyUSB0", restored.getSerialPort());
        assertEquals(2.22, restored.getRaHours(), 0);
        assertEquals(32.1, restored.getDecDegrees(), 0);
        assertTrue(restored.isAligned());
    }

    @Test
    public void loadPersistedState_whenNoFileExists_then_defaultSet() {
        File file = new File(System.getProperty("user.home"), "auxremote-mount.json");
        if (file.exists()) {
            file.delete();
        }

        Mount restored = new Mount();
        restored.loadPersistedState(new File(System.getProperty("user.home"), "auxremote-mount.json"));
        assertNotNull(restored);
    }
}