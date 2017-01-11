package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

/**
 * Created by dokeeffe on 25/12/16.
 */
public class QueryCordWrapPos extends MountCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryCordWrapPos.class);
    public static final byte MESSAGE_LENGTH = 0x01;
    public static final byte RESPONSE_LENGTH = 0x03;

    public QueryCordWrapPos(Mount mount) {
        super(mount);
    }

    @Override
    public byte[] getCommand() {
        byte result[] = new byte[8];
        result[0] = MC_HC_AUX_COMMAND_PREFIX;
        result[1] = MESSAGE_LENGTH;
        result[2] = AZM_BOARD;
        result[3] = MC_GET_CORDWRAP_POS;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = 0x00;
        result[7] = RESPONSE_LENGTH;
        return result;
    }

    @Override
    public void handleMessage(byte[] message) {
        String hex = DatatypeConverter.printHexBinary(message);
        LOGGER.debug("MC_GET_CORDWRAP_POS resp data = {}", hex);
        double cordwrapPos = bytesToDegrees(hex);
        LOGGER.debug("MC_GET_CORDWRAP_POS AZ angle = {}", cordwrapPos);
        mount.setCordWrapPosition(cordwrapPos);
    }
}
