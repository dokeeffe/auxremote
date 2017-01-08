package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import com.bobs.mount.TrackingMode;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class PecSeekIndexTest extends BaseCommandTest {

    private PecSeekIndex sut;

    @Before
    public void setup() {
        super.setup();
        sut = new PecSeekIndex(mockMount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001101900000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        sut.handleMessage(COMPLETE);
        verifyZeroInteractions(mockMount);
    }

}