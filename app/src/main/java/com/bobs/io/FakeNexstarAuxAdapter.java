package com.bobs.io;

import com.bobs.serialcommands.*;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.bobs.serialcommands.MountCommand.*;

/**
 * A sumilator adapter for testing clients such as INDI
 */
@Component
public class FakeNexstarAuxAdapter implements NexstarAuxAdapter {

    Logger LOGGER = LoggerFactory.getLogger(FakeNexstarAuxAdapter.class);


    /**
     * Channels are defined to sequence messages and order the responses.
     */
    private BlockingQueue<MountCommand> inputChannel = new LinkedBlockingQueue<>(1000);

    private boolean connected;
    private String serialPortName;
    private int defaultFakeOperationTimerCounter;

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
        connected = true;
        while (connected) {
            MountCommand command = null;
            try {
                command = inputChannel.poll(100, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                LOGGER.error("error reading command", e);
            }
            LOGGER.info("Fake adapter processing {}", command.getClass());
            if (command instanceof PecQueryAtIndex) {
                fakeLongRunningOperation(command, 2);
            }
            if (command instanceof PecPlayback) {
                command.handleMessage(new byte[]{ACK});
            }
            if (command instanceof PecQueryRecordDone) {
                fakeLongRunningOperation(command, 10);
            }
            if (command instanceof QuerySlewDone) {
                fakeLongRunningOperation(command, 10);
            }
            if (command instanceof Move) {
                command.handleMessage(new byte[]{ACK});
            }
        }
    }

    private void fakeLongRunningOperation(MountCommand command, int callsTillDone) {
        if (defaultFakeOperationTimerCounter > callsTillDone) {
            command.handleMessage(new byte[]{OPERATION_COMPLETE});
            defaultFakeOperationTimerCounter = 0;
        } else {
            defaultFakeOperationTimerCounter++;
            command.handleMessage(new byte[]{OPERATION_PENDING});
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
    }

    @Override
    public void waitForQueueEmpty() {
        while (!this.inputChannel.isEmpty()) {
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
     *
     * @param serialPortName
     */
    @Override
    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
    }
}
