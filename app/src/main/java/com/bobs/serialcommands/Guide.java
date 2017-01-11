package com.bobs.serialcommands;

import com.bobs.mount.Axis;
import com.bobs.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experimental!!
 * <p>
 * Guide pulse commands do not seem to work on my mount. Leaving this here for reference only.
 * Reverse engineered from INDI celestron driver.
 */
public class Guide extends MountCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Guide.class);

    public static final byte MESSAGE_LENGTH = 0x03;
    public static final byte RESPONSE_LENGTH = 0x01;
    private final Axis axis;
    private final int rate;
    private final int durationCsec;

    /**
     * Constructor for a guide pulse command.
     * rate should be a signed 8-bit integer in the range (-100,100) that represents
     * the pulse  velocity in % of sidereal. "duration_csec" is an unsigned  8-bit integer
     * (0,255) with  the pulse duration in centiseconds (i.e. 1/100 s  =  10ms).
     * The max pulse duration is 2550 ms.
     *
     * @param mount
     * @param rate  is a
     * @param axis  the Axis ALT or AZ
     */
    public Guide(Mount mount, int rate, Axis axis, int durationCsec) {
        super(mount);
        this.rate = rate;
        this.axis = axis;
        this.durationCsec = durationCsec;
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
        result[3] = MC_PULSE_GUIDE;
        result[4] = (byte) rate;
        result[5] = (byte) durationCsec;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        //nothing to do here.
    }


}
