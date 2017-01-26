package com.bobs.serialcommands;

import com.bobs.coord.AltAz;
import com.bobs.coord.Target;
import com.bobs.mount.TrackingMode;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

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
        AltAz altAz = new AltAz();
        double azPos = 2.42143406995738; //01B8CF
        Target position = altAz.buildFromNexstarEqNorth(Calendar.getInstance(), mount.getLongitude(), azPos, mount.getDecDegrees());
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

    /**
     * Test for the intermittant case where erronious data comes back from the mount.
     * This incorrect position data should be ignored.
     */
    @Test
    public void handleMessage_badUpdateFromMount() {
        //arrange
        AltAz altAz = new AltAz();
        double azPos = 2.42143406995738; //01B8CF
        Target position = altAz.buildFromNexstarEqNorth(Calendar.getInstance(), mount.getLongitude(), azPos, mount.getDecDegrees());
        mount.setRaHours(position.getRaHours());
        byte[] message = new byte[3];
        message[0] = (byte) 0x88;
        message[1] = (byte) 0x88;
        message[2] = (byte) 0x88;

        //act
        sut.handleMessage(message);

        //assert
        assertEquals(position.getRaHours(), mount.getRaHours(), 0.000001);
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