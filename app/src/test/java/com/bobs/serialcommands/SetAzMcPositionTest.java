package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class SetAzMcPositionTest extends BaseCommandTest {

    private SetAzMcPosition sut;

    @Before
    public void setUp() throws Exception {
        super.setup();
        mount.setAligned(true);
        sut = new SetAzMcPosition(mockMount, 123.45);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5004100457C96201", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        sut.handleMessage(ACK);
        verifyZeroInteractions(mockMount);
    }

    @Test
    public void handleMessage_when_FailToSetPosition_then_alignedFalse() throws Exception {
        sut.handleMessage(PENDING);
        verify(mockMount).setAligned(false);
    }

}