package com.bobs.serialcommands;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bobs.coord.CoordTransformer;
import com.bobs.coord.Target;
import com.bobs.mount.Mount;
import com.bobs.mount.TrackingMode;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class QueryAzMcPosition extends MountCommand {

    public static final byte MESSAGE_LENGTH = 0x01;
    public static final byte RESPONSE_LENGTH = 0x03;
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryAzMcPosition.class);

    public QueryAzMcPosition(Mount mount) {
        super(mount);
    }

    @Override
    public byte[] getCommand() {
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        result[2] = AZM_BOARD;
        result[3] = MC_GET_POSITION;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        CoordTransformer coordTransformer = new CoordTransformer();
        String hex = DatatypeConverter.printHexBinary(message);
        double azimuthDegreesReportedByMount = bytesToDegrees(hex);
        LOGGER.debug("ALTAZ raw data = {}", azimuthDegreesReportedByMount);
        if (TrackingMode.EQ_NORTH.equals(mount.getTrackingMode())) {
            Target position = coordTransformer.buildTargetFromNexstarEqNorth(
                    mount.getCalendarProvider().provide(),
                    mount.getLongitude(),
                    azimuthDegreesReportedByMount,
                    mount.getDecDegrees());
            LOGGER.debug("RA {}", position.getRaHours());
            mount.setRaHours(position.getRaHours());
        } else {
            throw new UnsupportedOperationException("Currently only EQ_NORTH is supported.");
        }
    }
}
