package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class PecSeekIndex extends MountCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(PecSeekIndex.class);
    public static final byte MESSAGE_LENGTH = 0x01;
    public static final byte RESPONSE_LENGTH = 0x01;

    public PecSeekIndex(Mount mount) {
        super(mount);
    }

    @Override
    public byte[] getCommand() {
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        result[2] = AZM_BOARD;
        result[3] = MC_PEC_SEEK_INDEX;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
    }
}
