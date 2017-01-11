package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by dokeeffe on 1/2/17.
 */
public class EnableCordWrapTest extends BaseCommandTest {

    private EnableCordWrap sut;

    @Before
    public void setUp() throws Exception {
        sut = new EnableCordWrap(mockMount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001103800000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() {
        sut.handleMessage(PENDING);
        verifyZeroInteractions(mockMount);
    }

}