package com.bobs.serialcommands;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by dokeeffe on 26/12/16.
 */
public class AzmtMcPositionTest extends BaseCommandTest {

    private QueryAzMcPosition sut;

    @Before
    public void setup() {
        super.setup();
        sut = new QueryAzMcPosition(mount);
    }

    @Test
    public void testHandleMessage() {
        byte[] message = {(byte) 0x02, (byte) 0x7d, (byte) 0xc6};
        sut.handleMessage(message);
        assertTrue(mount.getRaHours() > 0.0 && mount.getRaHours() < 24.0);
    }
}