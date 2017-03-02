package com.bobs.serialcommands;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bobs.coord.CoordTransformer;
import com.bobs.mount.Mount;
import com.bobs.mount.TrackingMode;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class QueryAltMcPosition extends MountCommand {

    public static final byte MESSAGE_LENGTH = 0x01;
    public static final byte RESPONSE_LENGTH = 0x03;
    public static final int BAD_DATA_THRESHOLD = 45;
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryAltMcPosition.class);

    public QueryAltMcPosition(Mount mount) {
        super(mount);
    }

    @Override
    public byte[] getCommand() {
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        result[2] = ALT_BOARD;
        result[3] = MC_GET_POSITION;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        if(!badPositionMessage(message)) {
            CoordTransformer coordTransformer = new CoordTransformer();
            String hex = DatatypeConverter.printHexBinary(message);
            double positionAngle = bytesToDegrees(hex);
            if (TrackingMode.EQ_NORTH.equals(mount.getTrackingMode())) {
                double dec = coordTransformer.convertAltPositionAngleToDecForEqNorth(positionAngle);
                LOGGER.debug("DEC {}", dec);
                mount.setDecDegrees(dec);
            } else {
                throw new UnsupportedOperationException("Currently only EQ_NORTH is supported.");
            }
        }
    }
}
