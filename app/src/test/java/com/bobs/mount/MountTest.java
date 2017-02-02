package com.bobs.mount;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

}