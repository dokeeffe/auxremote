package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class GpsLinked extends MountCommand {

    public static final byte MESSAGE_LENGTH = 0x01;
    public static final byte RESPONSE_LENGTH = 0x01;
    private static final Logger LOGGER = LoggerFactory.getLogger(GpsLinked.class);

    public GpsLinked(Mount mount) {
        super(mount);
    }

    @Override
    public byte[] getCommand() {
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        result[2] = GPS;
        result[3] = GPS_LINKED;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        mount.setGpsConnected(message != null && message[0] == 0x01);
    }
}
