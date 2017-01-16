package com.bobs.io;

import jssc.SerialPort;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class to create an instance of a SerialPort. Manily to ease testing.
 */
@Component
public class SerialPortBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerialPortBuilder.class);

    /**
     * Create a SerialPort instance for use when communicating with a nexstar hansdet over serial.
     * @param serialPortName
     * @return
     */
    public SerialPort buildSerialPortForHandset(String serialPortName) {
        SerialPort serialPort = new SerialPort(serialPortName);
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            LOGGER.info("Serial port opened");
        } catch (SerialPortException ex) {
            LOGGER.error("failure connecting to / opening port", ex);
            //TODO: Throw an exception here and update the indi driver (client) to get notified on connection failure
        }
        return serialPort;
    }
}
