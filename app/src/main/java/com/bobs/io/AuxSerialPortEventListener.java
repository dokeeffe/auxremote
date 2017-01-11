package com.bobs.io;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;

/**
 * Responsible for handling messages that come from the mount over the serial interface.
 * Messages that end in 0x23 are considered complete and added to the Queue for processing by the {@link NexstarAuxSerialAdapter}
 */
public class AuxSerialPortEventListener implements SerialPortEventListener {

    private static final byte MESSAGE_END = 0x23;
    private final SerialPort serialPort;
    private final Queue queue;
    private final ByteArrayOutputStream os = new ByteArrayOutputStream();
    private static final Logger LOGGER = LoggerFactory.getLogger(AuxSerialPortEventListener.class);


    /**
     * Constructor passed the SerialPort and the Queue to place complete messages on.
     *
     * @param serialPort
     * @param queue
     */
    public AuxSerialPortEventListener(SerialPort serialPort, Queue queue) {
        this.serialPort = serialPort;
        this.queue = queue;
    }

    /**
     * Handle a serial event. The Event may not contain the complete message. However if it ends in 0x23 it is considered complete
     *
     * @param event
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR() && event.getEventValue() > 0) {
            try {
                byte[] eventVal = serialPort.readBytes(event.getEventValue());
                os.write(eventVal);
                if (eventVal[eventVal.length - 1] == MESSAGE_END) {
                    os.flush();
                    byte[] data = os.toByteArray();
                    queue.add(Arrays.copyOf(data, data.length - 1)); //remove the last 0x23 char
                    LOGGER.debug("Received response: " + DatatypeConverter.printHexBinary(data));
                    os.reset();
                }
            } catch (SerialPortException ex) {
                LOGGER.error("Serial Exception receiving string from port: ", ex);
            } catch (IOException e) {
                LOGGER.error("Error in receiving string from serial: ", e);
            }
        }
    }

}