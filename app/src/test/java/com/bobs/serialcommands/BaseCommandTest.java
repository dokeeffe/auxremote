package com.bobs.serialcommands;

import com.bobs.mount.Mount;
import com.bobs.mount.TrackingMode;

import static org.mockito.Mockito.mock;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class BaseCommandTest {

    protected Mount mount = new Mount();
    protected Mount mockMount = mock(Mount.class);
    protected byte[] ACK = {0x01};
    protected byte[] PENDING = {0x00};
    protected byte[] COMPLETE = {(byte) 0xff};

    public void setup() {
        mount.setTrackingMode(TrackingMode.EQ_NORTH);
        mount.setLongitude(351.6391);
    }
}
