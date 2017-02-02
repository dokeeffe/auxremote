package com.bobs.io;

import org.junit.Test;

public class SerialPortBuilderTest {

    @Test(expected = RuntimeException.class)
    public void buildSerialPortForHandset_when_noMountPhysicallyConnected_then_exceptionThrown() throws Exception {
        SerialPortBuilder serialPortBuilder = new SerialPortBuilder();
        serialPortBuilder.buildSerialPortForHandset("/dev/null");
    }

}