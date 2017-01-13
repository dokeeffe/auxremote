package com.bobs.io;

import com.bobs.serialcommands.MountCommand;
import jssc.SerialPort;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

/**
 * Created by dokeeffe on 1/10/17.
 */
public class NexstarAuxSerialAdapterTest {

    private NexstarAuxSerialAdapter sut;
    private SerialPort serialPort;
    private AuxSerialPortEventListener auxSerialPortEventListener;
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
        sut = new NexstarAuxSerialAdapter();
        sut.setSerialPortName("/dev/ttyUSB0");
        sut.setSerialPortBuilder(serialPortBuilder);
        outputChannel = (Queue) ReflectionTestUtils.getField(sut, "outputChannel");
        inputChannel = (Queue) ReflectionTestUtils.getField(sut, "inputChannel");
        auxSerialPortEventListener = new AuxSerialPortEventListener(serialPort, outputChannel);
    }

    @Test
    public void queueCommand() throws Exception {
        sut.queueCommand(testCommand);
        assertEquals(1, inputChannel.size());
    }

    @Test
    public void start_verifyQueuedCommandGetsUsed() throws Exception {
        new Thread(sut).start();
        sut.queueCommand(testCommand);
        outputChannel.add(new byte[]{0x21, 0x22});
        sut.waitForQueueEmpty();

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