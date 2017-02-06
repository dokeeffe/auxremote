package com.bobs.mount;

import com.bobs.io.NexstarAuxAdapter;
import com.bobs.serialcommands.*;
import jssc.SerialPortException;

import java.util.ArrayList;
import java.util.List;

import static com.bobs.serialcommands.MountCommand.ALT_BOARD;
import static com.bobs.serialcommands.MountCommand.AZM_BOARD;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class FakeAuxAdapter implements NexstarAuxAdapter {

    private List<MountCommand> commands = new ArrayList();
    private String serialPortName;
    private Mount mount;
    private boolean connected = true;
    private byte[] azBoardPosition = new byte[3];
    private byte[] altBoardPosition = new byte[3];

    public void setMount(Mount mount) {
        this.mount = mount;
    }

    @Override
    public void queueCommand(MountCommand command) {
        commands.add(command);
        if (command instanceof QuerySlewDone) {
            if (command.getCommand()[2] == AZM_BOARD) {
                mount.setAzSlewInProgress(false);
            } else {
                mount.setAltSlewInProgress(false);
            }
        }
        if (command instanceof PecQueryAtIndex) {
            mount.setPecIndexFound(true);
        }
        if (command instanceof Goto) {
            byte[] gotoCmd = command.getCommand();
            if (gotoCmd[2] == ALT_BOARD) {
                altBoardPosition[0] = gotoCmd[4];
                altBoardPosition[1] = gotoCmd[5];
                altBoardPosition[2] = gotoCmd[6];
            }
            if (gotoCmd[2] == AZM_BOARD) {
                azBoardPosition[0] = gotoCmd[4];
                azBoardPosition[1] = gotoCmd[5];
                azBoardPosition[2] = gotoCmd[6];
            }
        }
        if (command instanceof QueryAltMcPosition) {
            command.handleMessage(altBoardPosition);
        }
        if (command instanceof QueryAzMcPosition) {
            command.handleMessage(azBoardPosition);
        }
        if (command instanceof PecQueryRecordDone) {
            mount.setPecMode(PecMode.IDLE);
        }
        if (command instanceof PecPlayback) {
            mount.setPecMode(PecMode.PLAYING);
        }
        if (command instanceof GpsLinked) {
            mount.setGpsConnected(true);
        }
        if (command instanceof Move) {
            Move moveCmd = (Move) command;
            if (moveCmd.getCommand()[4] == 0) {
                mount.setTrackingState(TrackingState.TRACKING);
            } else {
                mount.setTrackingState(TrackingState.SLEWING);
            }
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() throws SerialPortException {

    }

    @Override
    public void waitForQueueEmpty() {

    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
    }

    @Override
    public void run() {

    }

    public List<MountCommand> getQueuedCommands() {
        return this.commands;
    }
}
