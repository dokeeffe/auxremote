package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class GpsLat extends MountCommand {

    public static final byte MESSAGE_LENGTH = 0x01;
    public static final byte RESPONSE_LENGTH = 0x03;
    private static final Logger LOGGER = LoggerFactory.getLogger(GpsLat.class);

    public GpsLat(Mount mount) {
        super(mount);
    }

    @Override
    public byte[] getCommand() {
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        result[2] = GPS;
        result[3] = GPS_GET_LAT;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        String hex = DatatypeConverter.printHexBinary(message);
        double gpsLat = bytesToDegrees(hex);
        LOGGER.info("GPS reports lat {}", gpsLat);
        mount.setLatitude(gpsLat);
    }
}
