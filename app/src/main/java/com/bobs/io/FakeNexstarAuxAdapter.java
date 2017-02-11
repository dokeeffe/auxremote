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
 * A simulator adapter for testing clients such as INDI
 */
//@Component
public class FakeNexstarAuxAdapter implements NexstarAuxAdapter {

    /**
     * manage state of long running operations. When called n times then fake operation deemed complete
     */
    private static final int DEFAULT_LONG_RUNNING_OP_CYCLES = 5;
    Logger LOGGER = LoggerFactory.getLogger(FakeNexstarAuxAdapter.class);
    private int longRunningOperationCycles = DEFAULT_LONG_RUNNING_OP_CYCLES;
    private int fakeOperationTimerCounter;

    /**
     * Channels are defined to sequence messages and order the responses.
     */
    private BlockingQueue<MountCommand> inputChannel = new LinkedBlockingQueue<>(1000);

    private boolean connected;
    private String serialPortName;
    private byte[] altMcPosition;
    private byte[] azMcPosition;
    private byte[] goToAltMcPosition;
    private byte[] goToAzMcPosition;

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
                fakeLongRunningOperation(command, longRunningOperationCycles);
            }
            if (command instanceof PecPlayback) {
                command.handleMessage(new byte[]{ACK});
            }
            if (command instanceof PecQueryRecordDone) {
                fakeLongRunningOperation(command, longRunningOperationCycles);
            }
            if (command instanceof QuerySlewDone) {
                fakeLongRunningOperation(command, longRunningOperationCycles);
            }
            if (command instanceof Move) {
                command.handleMessage(new byte[]{ACK});
            }
            if (command instanceof SetAltMcPosition) {
                handleSetAltMcPosition(((SetAltMcPosition)command));
            }
            if (command instanceof SetAzMcPosition) {
                handleSetAzMcPosition(((SetAzMcPosition)command));
            }
            if (command instanceof QueryAzMcPosition) {
                command.handleMessage(this.azMcPosition);
            }
            if (command instanceof QueryAltMcPosition) {
                command.handleMessage(this.altMcPosition);
            }
            if (command instanceof Goto) {
                handleGoto(((Goto)command));
            }

        }
    }

    private void handleGoto(Goto command) {
        byte[] goToCommand = command.getCommand();
        byte[] ticks = new byte[3];
        ticks[0] = goToCommand[4];
        ticks[1] = goToCommand[5];
        ticks[2] = goToCommand[6];
        if (goToCommand[2] == ALT_BOARD) {
            this.goToAltMcPosition = ticks;
        } else {
            this.goToAzMcPosition = ticks;
        }

    }

    private void handleSetAzMcPosition(SetAzMcPosition command) {
        byte setAltPosCommand[] = command.getCommand();
        byte[] ticks = new byte[3];
        ticks[0] = setAltPosCommand[4];
        ticks[1] = setAltPosCommand[5];
        ticks[2] = setAltPosCommand[6];
        this.azMcPosition = ticks;
    }

    private void handleSetAltMcPosition(SetAltMcPosition command) {
        byte setAltPosCommand[] = command.getCommand();
        byte[] ticks = new byte[3];
        ticks[0] = setAltPosCommand[4];
        ticks[1] = setAltPosCommand[5];
        ticks[2] = setAltPosCommand[6];
        this.altMcPosition = ticks;
    }

    private void fakeLongRunningOperation(MountCommand command, int callsTillDone) {
        fakeOperationTimerCounter++;
        if (fakeOperationTimerCounter >= callsTillDone) {
            command.handleMessage(new byte[]{OPERATION_COMPLETE});
            if(this.goToAltMcPosition!=null && this. goToAzMcPosition!=null) {
                LOGGER.info("Finalizing goto operation");
                this.altMcPosition = goToAltMcPosition;
                this.azMcPosition = goToAzMcPosition;
                this.goToAltMcPosition = null;
                this.goToAzMcPosition = null;
            }
            fakeOperationTimerCounter = 0;
        } else {
            LOGGER.info("Faking long running operation, not done yet.");
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
