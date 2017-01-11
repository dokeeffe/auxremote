package com.bobs.mount;

import com.bobs.coord.Target;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by dokeeffe on 1/8/17.
 */
public class MountControllerTest {

    private MountController sut;
    private MountService mountService;

    @Before
    public void setup() {
        sut = new MountController();
        mountService = mock(MountService.class);
        sut.setMountService(mountService);
    }

    @Test
    public void mount() throws Exception {
        sut.mount();
        verify(mountService).getMount();
    }

    @Test
    public void updateMount() throws Exception {
        Mount updated = new Mount();
        sut.updateMount(updated);
        verify(mountService).updateMount(updated);
    }

    @Test
    public void connect() throws Exception {
        sut.connect();
        verify(mountService).connect();
    }

    @Test
    public void target_park() throws Exception {
        Target target = new Target();
        target.setType("park");
        sut.target(target);
        verify(mountService).park(target);
    }

    @Test
    public void target_unpark() throws Exception {
        Target target = new Target();
        target.setType("unpark");
        sut.target(target);
        verify(mountService).unpark(target);
    }

    @Test
    public void target_move() throws Exception {
        Target target = new Target();
        target.setType("move");
        sut.target(target);
        verify(mountService).moveAxis(target);
    }

    @Test
    public void target_sync() throws Exception {
        Target target = new Target();
        target.setType("sync");
        sut.target(target);
        verify(mountService).sync(target);
    }

    @Test
    public void target_slew() throws Exception {
        Target target = new Target();
        target.setType("slew");
        sut.target(target);
        verify(mountService).slew(target);
    }

    @Test
    public void target_guide() throws Exception {
        Target target = new Target();
        target.setType("guide");
        sut.target(target);
        verify(mountService).guide(target);
    }

}