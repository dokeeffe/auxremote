package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class QueryVersionTest extends BaseCommandTest {

    private QueryVersion sut;

    @Before
    public void setup() {
        super.setup();
        sut = new QueryVersion(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("500111FE00000002", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() throws Exception {
        sut.handleMessage(new byte[]{0x21, 0x22});
        assertEquals("2122",mount.getVersion());
    }

}