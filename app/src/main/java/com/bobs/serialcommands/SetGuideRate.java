package com.bobs.serialcommands;

import com.bobs.mount.GuideRate;
import com.bobs.mount.Mount;
import com.bobs.mount.TrackingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class SetGuideRate extends MountCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetGuideRate.class);
    private final byte targetBoard;
    private final GuideRate guideRate;

    /**
     * Constructor
     *
     * @param mount
     * @param targetBoard
     * @param guideRate
     */
    public SetGuideRate(Mount mount, byte targetBoard, GuideRate guideRate) {
        super(mount);
        this.guideRate = guideRate;
        this.targetBoard = targetBoard;
    }

    @Override
    public byte[] getCommand() {
        if (this.targetBoard != ALT_BOARD && this.targetBoard != AZM_BOARD) {
            throw new IllegalArgumentException("Cannot send this serial queueCommand to board " + this.targetBoard);
        }
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        if (this.guideRate == GuideRate.OFF) {
            result[1] = 0x04;
            result[4] = 0x00;
            result[5] = 0x00;
            result[6] = 0x00;
        } else if (this.guideRate == GuideRate.SIDEREAL) {
            result[1] = 0x03;
            result[4] = (byte) 0xff;
            result[5] = (byte) 0xff;
            result[6] = 0x00;
        } else if (this.guideRate == GuideRate.LUNAR) {
            result[1] = 0x03;
            result[4] = (byte) 0xff;
            result[5] = (byte) 0xfd;
            result[6] = 0x00;
        } else if (this.guideRate == GuideRate.SOLAR) {
            result[1] = 0x03;
            result[4] = (byte) 0xff;
            result[5] = (byte) 0xfe;
            result[6] = 0x00;
        }
        result[2] = this.targetBoard;
        result[3] = MC_SET_POS_GUIDERATE;
        result[7] = 0x01;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        if(OPERATION_COMPLETE==message[0]) {
            if (this.guideRate == GuideRate.OFF) {
                mount.setTrackingState(TrackingState.IDLE);
            } else {
                mount.setTrackingState(TrackingState.TRACKING);
            }
        }
    }
}
