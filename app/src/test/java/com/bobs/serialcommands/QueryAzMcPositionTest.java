package com.bobs.serialcommands;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;

import com.bobs.coord.CoordTransformer;
import com.bobs.coord.Target;
import com.bobs.mount.TrackingMode;

/**
 * Created by dokeeffe on 1/2/17.
 */
public class QueryAzMcPositionTest extends BaseCommandTest {

    QueryAzMcPosition sut;

    @Before
    public void setup() {
        super.setup();
        sut = new QueryAzMcPosition(mount);
    }

    @Test
    public void getCommand() throws Exception {
        assertEquals("5001100100000003", DatatypeConverter.printHexBinary(sut.getCommand()));
    }

    @Test
    public void handleMessage() {
        //arrange
        CoordTransformer coordTransformer = new CoordTransformer();
        double azPos = 2.42143406995738; //01B8CF
        Target position = coordTransformer.buildTargetFromNexstarEqNorth(Calendar.getInstance(), mount.getLongitude(), azPos, mount.getDecDegrees());
        mount.setRaHours(position.getRaHours() - 0.1);
        byte[] message = new byte[3];
        message[0] = (byte) 0x01;
        message[1] = (byte) 0xB8;
        message[2] = (byte) 0xCF;

        //act
        sut.handleMessage(message);

        //assert
        assertEquals(position.getRaHours(), mount.getRaHours(), 0.00001);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void handleMessage_eqSouthMode() {
        mount.setTrackingMode(TrackingMode.EQ_SOUTH);
        byte[] message = new byte[3];
        message[0] = (byte) 0x20;
        message[1] = (byte) 0xB8;
        message[2] = (byte) 0xCF;
        sut.handleMessage(message);
    }

}