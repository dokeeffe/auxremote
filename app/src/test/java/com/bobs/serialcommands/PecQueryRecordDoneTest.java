package com.bobs.serialcommands;

import com.bobs.mount.PecMode;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class PecQueryRecordDoneTest extends BaseCommandTest {

    private PecQueryRecordDone sut;

    @Before
    public void setup() {
        super.setup();
        sut = new PecQueryRecordDone(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001101500000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage_complete() throws Exception {
        sut.handleMessage(COMPLETE);
        assertEquals(PecMode.IDLE, mount.getPecMode());
    }

    @Test
    public void handleMessage_pending() throws Exception {
        sut.handleMessage(PENDING);
        assertEquals(PecMode.RECORDING, mount.getPecMode());
    }

}