package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class SetAzMcPositionTest extends BaseCommandTest {

    private SetAzMcPosition sut;

    @Before
    public void setUp() throws Exception {
        super.setup();
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

}