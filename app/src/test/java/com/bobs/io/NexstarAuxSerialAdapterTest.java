package com.bobs.io;

import com.bobs.serialcommands.MountCommand;
import jssc.SerialPort;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

/**
 * Created by dokeeffe on 1/10/17.
 */
public class NexstarAuxSerialAdapterTest {

    private NexstarAuxSerialAdapter sut;
    private SerialPort serialPort;
    private Queue outputChannel;
    private Queue inputChannel;
    private MountCommand testCommand;

    @Before
    public void setUp() throws Exception {
        testCommand = mock(MountCommand.class);
        when(testCommand.getCommand()).thenReturn(new byte[]{0x01, 0x02});
        serialPort = mock(SerialPort.class);
        SerialPortBuilder serialPortBuilder = mock(SerialPortBuilder.class);
        when(serialPortBuilder.buildSerialPortForHandset("/dev/ttyUSB0")).thenReturn(serialPort);
        when(serialPort.isOpened()).thenReturn(Boolean.TRUE);
        sut = new NexstarAuxSerialAdapter();
        sut.setSerialPortName("/dev/ttyUSB0");
        sut.setSerialPortBuilder(serialPortBuilder);
        outputChannel = (Queue) ReflectionTestUtils.getField(sut, "outputChannel");
        inputChannel = (Queue) ReflectionTestUtils.getField(sut, "inputChannel");
        new Thread(sut).start();
        Thread.sleep(100);
    }

    @Test(expected = IllegalStateException.class)
    public void queueCommand_when_notconnected_throwsException() throws Exception {
        when(serialPort.isOpened()).thenReturn(Boolean.FALSE);
        sut.queueCommand(testCommand);
    }

    @Test
    public void start_verifyQueuedCommandGetsUsed() throws Exception {
        sut.queueCommand(testCommand);
        outputChannel.add(new byte[]{0x21, 0x22});
        sut.waitForQueueEmpty();
        Thread.sleep(200);

        verify(serialPort).writeBytes(new byte[]{0x01, 0x02});
        verify(testCommand).handleMessage(new byte[]{0x21, 0x22});
    }


    @Test
    public void stop() throws Exception {
        ReflectionTestUtils.setField(sut, "connected", Boolean.TRUE);
        ReflectionTestUtils.setField(sut, "serialPort", serialPort);
        sut.stop();
        verify(serialPort).closePort();
        assertFalse(sut.isConnected());
    }

}