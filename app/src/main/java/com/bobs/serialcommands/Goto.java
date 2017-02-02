package com.bobs.serialcommands;

import com.bobs.mount.Axis;
import com.bobs.mount.Mount;
import com.bobs.mount.TrackingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class Goto extends MountCommand {

    public static final byte MESSAGE_LENGTH = 0x04;
    public static final byte RESPONSE_LENGTH = 0x01;
    private static final Logger LOGGER = LoggerFactory.getLogger(Goto.class);
    private final Double position;
    private final Axis axis;
    private final boolean fast;

    /**
     * Constructor
     *
     * @param mount
     * @param position
     * @param axis
     * @param fast
     */
    public Goto(Mount mount, Double position, Axis axis, boolean fast) {
        super(mount);
        this.position = position;
        this.axis = axis;
        this.fast = fast;
    }

    @Override
    public byte[] getCommand() {
        byte[] ticks = degreesToBytes(this.position);
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        if (Axis.ALT == axis) {
            result[2] = ALT_BOARD;
        } else {
            result[2] = AZM_BOARD;
        }
        if (fast) {
            result[3] = MC_GOTO_FAST;
        } else {
            result[3] = MC_GOTO_SLOW;
        }
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
        } else {
            mount.setTrackingState(TrackingState.SLEWING);
        }
    }


}
