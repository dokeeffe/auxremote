package com.bobs.io;

import jssc.SerialPort;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by dokeeffe on 1/10/17.
 */
@Component
public class SerialPortBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerialPortBuilder.class);

    public SerialPort buildSerialPortForHandset(String serialPortName) {
        SerialPort serialPort = new SerialPort(serialPortName);
        try {
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        } catch (SerialPortException ex) {
            LOGGER.error("failure connecting to port", ex);
            //TODO: Throw an exception here and update the indi driver (client) to get notified on connection failure
        }
        return serialPort;
    }
}
