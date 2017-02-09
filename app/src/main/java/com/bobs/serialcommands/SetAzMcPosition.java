package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class SetAzMcPosition extends MountCommand {

    public static final byte MESSAGE_LENGTH = 0x04;
    public static final byte RESPONSE_LENGTH = 0x01;
    private static final Logger LOGGER = LoggerFactory.getLogger(SetAltMcPosition.class);
    private final double position;

    public SetAzMcPosition(Mount mount, double position) {
        super(mount);
        this.position = position;
    }

    @Override
    public byte[] getCommand() {
        byte[] ticks = degreesToBytes(this.position);
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        result[2] = AZM_BOARD;
        result[3] = MC_SET_POSITION;
        result[4] = ticks[0];
        result[5] = ticks[1];
        result[6] = ticks[2];
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        if (message[0] != ACK) {
            LOGGER.error("Expected ACK, but got {}", DatatypeConverter.printHexBinary(message));
            mount.setError("SYNC ERROR SetAzMcPosition");
        }
    }
}
