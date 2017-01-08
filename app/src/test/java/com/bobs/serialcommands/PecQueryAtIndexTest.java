package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import com.bobs.mount.PecMode;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class PecQueryAtIndexTest extends BaseCommandTest {

    private PecQueryAtIndex sut;

    @Before
    public void setUp() throws Exception {
        super.setup();
        sut = new PecQueryAtIndex(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001101800000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage_complete() throws Exception {
        sut.handleMessage(COMPLETE);
        assertEquals(PecMode.IDLE, mount.getPecMode());
        assertTrue(mount.isPecIndexFound());
    }

    @Test
    public void handleMessage_pending() throws Exception {
        sut.handleMessage(PENDING);
        assertEquals(PecMode.INDEXING, mount.getPecMode());
        assertFalse(mount.isPecIndexFound());
    }

}