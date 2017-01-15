package com.bobs.serialcommands;

import com.bobs.mount.Axis;
import com.bobs.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class QuerySlewDone extends MountCommand {

    public static final byte MESSAGE_LENGTH = 0x01;
    public static final byte RESPONSE_LENGTH = 0x01;
    private static final Logger LOGGER = LoggerFactory.getLogger(QuerySlewDone.class);
    private final Axis axis;

    public QuerySlewDone(Mount mount, Axis axis) {
        super(mount);
        this.axis = axis;
    }

    @Override
    public byte[] getCommand() {
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        if (Axis.ALT == axis) {
            result[2] = ALT_BOARD;
        } else {
            result[2] = AZM_BOARD;
        }
        result[3] = MC_SLEW_DONE;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        boolean slewing = true;
        if (OPERATION_COMPLETE == message[0]) {
            slewing = false;
            LOGGER.info("{} Slew Complete", axis);
        }
        if (Axis.ALT == axis) {
            mount.setAltSlewInProgress(slewing);
        } else {
            mount.setAzSlewInProgress(slewing);
        }
    }
}
