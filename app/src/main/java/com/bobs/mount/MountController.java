package com.bobs.mount;

import com.bobs.coord.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * A Web controller responsible for exposing an HTTP api and delegating to the {@link MountService}
 */
@RestController
@RequestMapping("/api/mount")
public class MountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MountController.class);

    /**
     * Service bean to handle the mounts state.
     */
    @Autowired
    private MountService mountService;

    /**
     * GET the {@link Mount}s current state.
     * FIXME: on not connected return somethnig other than 500
     * @return
     */
    @GetMapping("")
    public Mount mount() {
        return mountService.getMount();
    }

    @PostMapping("")
    public Mount updateMount(@RequestBody Mount mount) {
        LOGGER.info("Updating Mount");
        return mountService.updateMount(mount);
    }


    /**
     * Handle POST to connect. This will delegate to the service to connect to the mount over serial and query state.
     *
     * @return
     */
    @PostMapping("/connect")
    public boolean connect() {
        LOGGER.info("CONNECTING");
        return mountService.connect();
    }

    /**
     * Handle the POST for a Target.
     * There are 6 types of Target. Delegate to the appropriate service method.
     * NOTE: Some of these are blocking and some non-blocking.
     *
     * @param target
     */
    @PostMapping("/target")
    public void target(@RequestBody Target target) {
        LOGGER.info("target request");
        if ("park".equals(target.getType())) {
            mountService.park(target);
        } else if ("unpark".equals(target.getType())) {
            mountService.unpark(target);
        } else if ("move".equals(target.getType())) {
            mountService.moveAxis(target);
        } else if ("sync".equals(target.getType())) {
            mountService.sync(target);
        } else if ("slew".equals(target.getType())) {
            mountService.slew(target, false);
        } else if ("guide".equals(target.getType())) {
            mountService.guide(target);
        }
    }

    /**
     * Set the mount service.
     *
     * @param mountService
     */
    public void setMountService(MountService mountService) {
        this.mountService = mountService;
    }
}
