package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static com.bobs.serialcommands.MountCommand.OPERATION_COMPLETE;
import static org.junit.Assert.*;

/**
 * Created by dokeeffe on 1/2/17.
 */
public class QueryCordWrapTest extends BaseCommandTest {

    private QueryCordWrap sut;

    @Before
    public void setUp() throws Exception {
        super.setup();
        sut = new QueryCordWrap(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001103B00000001", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage_ack() throws Exception {
        sut.handleMessage(COMPLETE);
        assertTrue(mount.isCordWrapEnabled());

    }

    @Test
    public void handleMessage_nonack() throws Exception {
        sut.handleMessage(new byte[]{0x22});
        assertFalse(mount.isCordWrapEnabled());
    }

}