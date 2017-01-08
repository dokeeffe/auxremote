package com.bobs.serialcommands;

import com.bobs.mount.Axis;
import com.bobs.mount.Mount;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class QuerySlewDoneTest extends BaseCommandTest {

    private QuerySlewDone altQuery;
    private QuerySlewDone azQuery;

    @Before
    public void setUp() throws Exception {
        altQuery = new QuerySlewDone(mount, Axis.ALT);
        azQuery = new QuerySlewDone(mount, Axis.AZ);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001111300000001", DatatypeConverter.printHexBinary(altQuery.getCommand()));
        assertEquals("5001101300000001", DatatypeConverter.printHexBinary(azQuery.getCommand()));
    }

    @Test
    public void handleMessage_azComplete() throws Exception {
        mount.setAzSlewInProgress(true);
        azQuery.handleMessage(COMPLETE);
        assertFalse(mount.isAzSlewInProgress());
    }

    @Test
    public void handleMessage_altComplete() throws Exception {
        mount.setAltSlewInProgress(true);
        altQuery.handleMessage(COMPLETE);
        assertFalse(mount.isAltSlewInProgress());
    }

    @Test
    public void handleMessage_azPending() throws Exception {
        mount.setAzSlewInProgress(true);
        azQuery.handleMessage(PENDING);
        assertTrue(mount.isAzSlewInProgress());
    }

    @Test
    public void handleMessage_altPending() throws Exception {
        mount.setAltSlewInProgress(true);
        altQuery.handleMessage(PENDING);
        assertTrue(mount.isAltSlewInProgress());
    }

}