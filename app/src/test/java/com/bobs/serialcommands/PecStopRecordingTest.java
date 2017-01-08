package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class PecStopRecordingTest extends BaseCommandTest {

    private PecStopRecording sut;

    @Before
    public void setup() {
        super.setup();
        sut = new PecStopRecording(mockMount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001101600000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        sut.handleMessage(new byte[1]);
        verifyZeroInteractions(mockMount);
    }

}