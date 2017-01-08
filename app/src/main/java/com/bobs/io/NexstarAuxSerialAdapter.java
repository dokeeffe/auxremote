package com.bobs.io;

import com.bobs.serialcommands.MountCommand;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.xml.bind.DatatypeConverter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Low level IO adapter class responsible for serial communication with the mount by means of {@link MountCommand} objects.
 * Since serial communication is a non blocking IO, Java {@link BlockingQueue}s are used as message channels to ensure the ordering of the messages being sent to the mount and response messages being read from the mount.
 */
public class NexstarAuxSerialAdapter implements NexstarAuxAdapter {

    /**
     * Max number of seconds to wait for a response message after sending a command message to the mount.
     */
    private static final int RESPONSE_TIMEOUT_SEC = 10;
    Logger LOGGER = LoggerFactory.getLogger(NexstarAuxSerialAdapter.class);

    /**
     * The SerialPort used to send and recieve bytes to/from.
     */
    private SerialPort serialPort;

    /**
     * Channels are defined to sequence messages and order the responses.
     */
    private BlockingQueue<MountCommand> inputChannel = new LinkedBlockingQueue<>(10);
    private BlockingQueue<byte[]> outputChannel = new LinkedBlockingQueue<>(10);
    private boolean connected;
    private String serialPortName;

    public NexstarAuxSerialAdapter(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    /**
     * Queue a serial command. Serial commands are only issued in series (one after another)
     * The {@link MountCommand} passed is also responsible for handling the logic related to the serial message that gets returned from the mount.
     *
     * @param command
     */
    @Override
    public void queueCommand(MountCommand command) {
        this.inputChannel.add(command);
    }


    /**
     * Start this adapter. This normally needs to start in a new thread.
     */
    @Override
    public void start() {
        LOGGER.debug("Starting adapter");
        this.serialPort = new SerialPort(serialPortName);
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            serialPort.addEventListener(new AuxSerialPortEventListener(serialPort, outputChannel));
            connected = true;
            LOGGER.debug("port opened");
        } catch (SerialPortException ex) {
            LOGGER.error("failure connecting to port", ex);
        }
        while (connected) {
            try {
                MountCommand command = inputChannel.poll(100, TimeUnit.DAYS);
                byte[] cmd = command.getCommand();
                LOGGER.debug("Sending {} message to mount {}", command.getClass().getName(), DatatypeConverter.printHexBinary(cmd));
                serialPort.writeBytes(cmd);
                byte[] response1 = outputChannel.poll(RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS);
                if (response1 == null || response1.length == 0) {
                    LOGGER.error("Comms error, invalid response from mount {}", response1 );
                } else {
                    try {
                        LOGGER.debug("Processing response from mount");
                        command.handleMessage(response1);
                    } catch (Exception ex) {
                        //FIXME: better error handling.. if an exception gets thrown from here then it kills the serial processor thread esentially killing the entire app
                        LOGGER.error("Fatal error processing response",ex);
                    }
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cleanup on finish. Close the port and set to not connected.
     *
     * @throws SerialPortException
     */
    @Override
    @PreDestroy
    public void stop() throws SerialPortException {
        connected = false;
        serialPort.closePort();
    }

    @Override
    public void waitForQueueEmpty() {
        while(!this.inputChannel.isEmpty()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void run() {
        this.start();
    }

    /**
     * Set the name of the port to use.
     * @param serialPortName
     */
    @Override
    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
    }
}
