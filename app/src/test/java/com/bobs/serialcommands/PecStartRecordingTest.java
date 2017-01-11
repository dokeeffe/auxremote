package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class PecStartRecordingTest extends BaseCommandTest {

    private PecStartRecording sut;

    @Before
    public void setup() {
        super.setup();
        sut = new PecStartRecording(mockMount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001100C00000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        sut.handleMessage(new byte[1]);
        verifyZeroInteractions(mockMount);
    }

}