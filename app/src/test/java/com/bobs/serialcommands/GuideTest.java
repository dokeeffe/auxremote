package com.bobs.serialcommands;

import com.bobs.mount.Axis;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by dokeeffe on 1/2/17.
 */
public class GuideTest extends BaseCommandTest {

    @Before
    public void setUp() throws Exception {
        super.setup();
    }

    @Test
    public void getCommand() throws Exception {
        Guide guide = new Guide(mount, 100, Axis.ALT, 100);
        assertEquals("5003112664640001", DatatypeConverter.printHexBinary(guide.getCommand()));
        guide = new Guide(mount, 100, Axis.AZ, 100);
        assertEquals("5003102664640001", DatatypeConverter.printHexBinary(guide.getCommand()));
    }

    @Test
    public void handleMessage() {
        Guide guide = new Guide(mount, 100, Axis.ALT, 100);
        guide.handleMessage(ACK);
        verifyZeroInteractions(mockMount);
    }
}