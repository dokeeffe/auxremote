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
public class Move extends MountCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Move.class);

    public static final byte MESSAGE_LENGTH = 0x02;
    public static final byte RESPONSE_LENGTH = 0x01;
    private final Axis axis;
    private final boolean positive;
    private final int rate;

    /**
     * Constructor
     *
     * @param mount
     * @param rate     0 to 9 (0 means stop)
     * @param axis     the Axis ALT or AZ
     * @param positive the direction positive or negative
     */
    public Move(Mount mount, int rate, Axis axis, boolean positive) {
        super(mount);
        this.rate = rate;
        this.axis = axis;
        this.positive = positive;
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
        if (positive) {
            result[3] = MC_MOVE_POS;
        } else {
            result[3] = MC_MOVE_NEG;
        }
        result[4] = (byte) rate;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        if (message[0] != ACK) {
            LOGGER.error("Expected ACK, but got {}", DatatypeConverter.printHexBinary(message));
        } else {
            mount.setTrackingState(rate == 0 ? TrackingState.TRACKING : TrackingState.SLEWING);
            if (Axis.ALT == axis) {
                mount.setAltSlewInProgress(rate == 0 ? false: true);
            } else {
                mount.setAzSlewInProgress(rate == 0 ? false: true);
            }

        }
    }

}
