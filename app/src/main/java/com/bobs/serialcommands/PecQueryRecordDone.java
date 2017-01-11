package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import com.bobs.mount.PecMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class PecQueryRecordDone extends MountCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(PecQueryRecordDone.class);
    public static final byte MESSAGE_LENGTH = 0x01;
    public static final byte RESPONSE_LENGTH = 0x01;

    public PecQueryRecordDone(Mount mount) {
        super(mount);
    }

    @Override
    public byte[] getCommand() {
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        result[2] = AZM_BOARD;
        result[3] = MC_PEC_RECORD_DONE;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        if (OPERATION_COMPLETE == message[0]) {
            mount.setPecMode(PecMode.IDLE);
        } else if (OPERATION_PENDING == message[0]) {
            mount.setPecMode(PecMode.RECORDING);
        } else {
            LOGGER.error("Unexpected response for MC_PEC_RECORD_DONE command {}", message[0]);
        }
    }
}
