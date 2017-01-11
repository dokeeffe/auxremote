package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

/**
 * Build a command to send to the GPS module to query status.
 */
public class GpsRecieverStatus extends MountCommand {

    public static final byte MESSAGE_LENGTH = 0x01;
    public static final byte RESPONSE_LENGTH = 0x02;
    private static final Logger LOGGER = LoggerFactory.getLogger(GpsRecieverStatus.class);

    public GpsRecieverStatus(Mount mount) {
        super(mount);
    }

    @Override
    public byte[] getCommand() {
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        result[2] = GPS;
        result[3] = GPS_REC_STATUS;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        String hex = DatatypeConverter.printHexBinary(message);
        LOGGER.debug("GPS STATUS: {}", hex);
        int i = Integer.parseInt(hex, 16);
        String bits = String.format("%8s", Integer.toBinaryString(0x10000 | i)).substring(1);
        LOGGER.debug("********   GPS STATUS BITS: {}", bits);
        String bit1513 = bits.substring(12, 15);
        if (bits.substring(12, 15).equals("110")) {
            mount.setGpsReceiverStatus("Acquiring Satellites");
        }
        if (bits.substring(7, 8).equals("1")) {
            mount.setGpsReceiverStatus("Filter Reset To Raw GPS Solution");
        }
    }
}
