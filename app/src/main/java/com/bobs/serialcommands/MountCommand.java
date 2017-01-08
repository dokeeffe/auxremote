package com.bobs.serialcommands;

import com.bobs.mount.Mount;

/**
 * Base class for all serial commands that update an instance of a {@link Mount}
 */
public abstract class MountCommand {

    public static final byte ACK = 0x01;
    public static final byte OPERATION_PENDING = 0x00;
    public static final byte OPERATION_COMPLETE = (byte)0xff;

    public static final byte MC_HC_AUX_COMMAND_PREFIX = 0x50;
    public static final byte MC_GET_POSITION = 0x01;
    public static final byte MC_GOTO_FAST = 0x02;
    public static final byte MC_SET_POSITION = 0x04;
    public static final byte MC_SET_POS_GUIDERATE = 0x06;
    public static final byte MC_SET_NEG_GUIDERATE = 0x07;
    public static final byte MC_LEVEL_START = 0x0b;
    public static final byte MC_SLEW_DONE = 0x13;
    public static final byte MC_GOTO_SLOW = 0x17;
    public static final byte MC_PULSE_GUIDE = 0x26; //TODO: this is not documented : { 0x50, 0x04, 0x11, 0x26, rate, duration_csec, 0x00, 0x00};
    public static final byte MC_SEEK_INDEX = 0x19;
    public static final byte MC_MOVE_POS = 0x24;
    public static final byte MC_MOVE_NEG = 0x25;
    public static final byte MC_VER = (byte) 0xfe;
    public static final byte MC_ENABLE_CORDWRAP = (byte) 0x38;
    public static final byte MC_DISABLE_CORDWRAP = (byte) 0x39;
    public static final byte MC_SET_CORDWRAP_POS = (byte) 0x3a;
    public static final byte MC_PEC_REC_START = (byte) 0x0c;
    public static final byte MC_PEC_PLAYBACK = (byte) 0x0d;
    public static final byte MC_PEC_RECORD_DONE = (byte) 0x15;
    public static final byte MC_PEC_RECORD_STOP = (byte) 0x16;
    public static final byte MC_PEC_AT_INDEX = (byte) 0x18;
    public static final byte MC_PEC_SEEK_INDEX = (byte) 0x19;
    public static final byte MC_POLL_CORDWRAP = (byte) 0x3b;
    public static final byte MC_GET_CORDWRAP_POS = (byte) 0x3c;
    public static final byte GPS_GET_LAT = 0x01;
    public static final byte GPS_GET_LON = 0x02;
    public static final byte GPS_GET_DATE = 0x03;
    public static final byte GPS_GET_YEAR = 0x04;
    public static final byte GPS_GET_TIME = 0x33;
    public static final byte GPS_TIME_VALID = 0x36;
    public static final byte GPS_REC_STATUS = 0x08;
    public static final byte GPS_LINKED = 0x37;
    public static final byte MB_BOARD = 0x01;
    public static final byte HC_BOARD = 0x04;
    public static final byte AZM_BOARD = 0x10;
    public static final byte ALT_BOARD = 0x11;
    public static final byte GPS = (byte) 0xb0;

    /**
     * Aux commands report alt-az positions as fractions of a full rotation which is a 3byte integer
     */
    protected static final int FULL_ROTATION = Integer.parseInt("FFFFFF", 16);

    /**
     * The mount to be updated
     */
    protected final Mount mount;

    /**
     * Constructor
     *
     * @param mount
     */
    public MountCommand(Mount mount) {
        this.mount = mount;
    }

    /**
     * Subclasses should implement this to generate the relevant queueCommand to send over serial to the mount
     *
     * @return
     */
    public abstract byte[] getCommand();

    /**
     * Subclasses should implement this to perform the relevant actions on recieving the response from the mount
     *
     * @param message The response bytes from the mount
     */
    public abstract void handleMessage(byte[] message);

    /**
     * Convert a 3byte hex string as a fraction of a full rotation to degrees.
     *
     * @param hex
     * @return
     */
    protected double bytesToDegrees(String hex) {
        int rotation = Integer.parseInt(hex, 16);
        return (double) rotation / FULL_ROTATION * 360.0;
    }

    /**
     * Convert degrees to a 24 bit byte representation where 'ffffff' is 360 deg
     *
     * @param deg
     * @return
     */
    protected byte[] degreesToBytes(double deg) {
        byte[] result = new byte[3];
        Double fractionOfFull = (deg / 360) * FULL_ROTATION;
        int intTicks = fractionOfFull.intValue();
        result[2] = (byte) (intTicks & 0xFF);
        result[1] = (byte) ((intTicks >> 8) & 0xFF);
        result[0] = (byte) ((intTicks >> 16) & 0xFF);
        return result;
    }
}
