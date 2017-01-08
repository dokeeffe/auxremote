package com.bobs.io;

import com.bobs.serialcommands.MountCommand;
import jssc.SerialPortException;

import javax.annotation.PreDestroy;

/**
 * Created by dokeeffe on 1/5/17.
 */
public interface NexstarAuxAdapter extends Runnable {
    void queueCommand(MountCommand command);

    void start();

    void stop() throws SerialPortException;

    void waitForQueueEmpty();

    boolean isConnected();

    void setSerialPortName(String serialPortName);
}
