package com.bobs.mount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Basic bean responsible for storing the state of the actual physical mount.
 * This is a Singleton bean, there is only one instance in the application.
 * This bean is managed by the {@link MountService}
 * The bean is serialised when the application shuts down and de-serialised on startup.
 * <p>
 * TODO: cleanup mess of objects and primitives used
 */
@Component
public class Mount implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mount.class);

    private String version;
    private TrackingState trackingState = TrackingState.IDLE;
    private Double raHours = 0.0;
    private Double decDegrees = 0.0;
    private TrackingMode trackingMode; //eq north will be the only tested one.
    private Double gpsLat;
    private Double gpsLon;
    private boolean gpsConnected;
    private String gpsReceiverStatus;
    private Double slewLimitAlt;
    private Double slewLimitAz;
    private boolean locationSet = false;
    private boolean altSlewInProgress = false;
    private boolean azSlewInProgress = false;
    private boolean aligned = false;
    private boolean cordWrapEnabled;
    private String serialPort;
    private PecMode pecMode = PecMode.IDLE;
    private boolean pecIndexFound = false;
    private String error;
    private GuideRate guideRate = GuideRate.SIDEREAL;
    private double cordWrapPosition;


    @PostConstruct
    public void loadState() {
        try {
            File persistanceStore = new File(System.getProperty("user.home"), "auxremote-mount.ser");
            byte[] mountState = FileCopyUtils.copyToByteArray(persistanceStore);
            Mount persisted = (Mount) SerializationUtils.deserialize(mountState);
            BeanUtils.copyProperties(persisted, this);
            this.pecIndexFound = false;
            this.pecMode = PecMode.IDLE;
            this.gpsConnected = false;
            this.aligned = false;
        } catch (Exception e) {
            LOGGER.error("Error restoring mount state from persistance store. Not a big problem. Using defaults.", e);
        }
    }

    @PreDestroy
    public void saveState() {
        byte[] mountState = SerializationUtils.serialize(this);
        File persistanceStore = new File(System.getProperty("user.home"), "auxremote-mount.ser");
        try {
            FileCopyUtils.copy(mountState, persistanceStore);
        } catch (IOException e) {
            LOGGER.warn("Error saving mount state", e);
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Double getRaHours() {
        return raHours;
    }

    public void setRaHours(Double raHours) {
        this.raHours = raHours;
    }

    public Double getDecDegrees() {
        return decDegrees;
    }

    public void setDecDegrees(Double decDegrees) {
        this.decDegrees = decDegrees;
    }

    public TrackingMode getTrackingMode() {
        return trackingMode;
    }

    public void setTrackingMode(TrackingMode trackingMode) {
        this.trackingMode = trackingMode;
    }

    public Double getGpsLat() {
        return gpsLat;
    }

    public void setGpsLat(Double gpsLat) {
        this.gpsLat = gpsLat;
    }

    public Double getGpsLon() {
        return gpsLon;
    }

    public void setGpsLon(Double gpsLon) {
        this.gpsLon = gpsLon;
    }

    public boolean isGpsConnected() {
        return gpsConnected;
    }

    public void setGpsConnected(boolean gpsConnected) {
        this.gpsConnected = gpsConnected;
    }

    public String getGpsReceiverStatus() {
        return gpsReceiverStatus;
    }

    public void setGpsReceiverStatus(String gpsReceiverStatus) {
        this.gpsReceiverStatus = gpsReceiverStatus;
    }

    public Double getSlewLimitAlt() {
        return slewLimitAlt;
    }

    public void setSlewLimitAlt(Double slewLimitAlt) {
        this.slewLimitAlt = slewLimitAlt;
    }

    public Double getSlewLimitAz() {
        return slewLimitAz;
    }

    public void setSlewLimitAz(Double slewLimitAz) {
        this.slewLimitAz = slewLimitAz;
    }

    public boolean isLocationSet() {
        return locationSet;
    }

    public void setLocationSet(boolean locationSet) {
        this.locationSet = locationSet;
    }

    public boolean isAltSlewInProgress() {
        return altSlewInProgress;
    }

    public void setAltSlewInProgress(boolean altSlewInProgress) {
        this.altSlewInProgress = altSlewInProgress;
    }

    public boolean isAzSlewInProgress() {
        return azSlewInProgress;
    }

    public void setAzSlewInProgress(boolean azSlewInProgress) {
        this.azSlewInProgress = azSlewInProgress;
    }

    public TrackingState getTrackingState() {
        return trackingState;
    }

    public void setTrackingState(TrackingState trackingState) {
        this.trackingState = trackingState;
    }

    public boolean isAligned() {
        return aligned;
    }

    public void setAligned(boolean aligned) {
        this.aligned = aligned;
    }

    public void setCordWrapEnabled(boolean cordWrapEnabled) {
        this.cordWrapEnabled = cordWrapEnabled;
    }

    public boolean isCordWrapEnabled() {
        return cordWrapEnabled;
    }

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public PecMode getPecMode() {
        return pecMode;
    }

    public void setPecMode(PecMode pecMode) {
        this.pecMode = pecMode;
    }

    public boolean isPecIndexFound() {
        return pecIndexFound;
    }

    public void setPecIndexFound(boolean pecIndexFound) {
        this.pecIndexFound = pecIndexFound;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public GuideRate getGuideRate() {
        return guideRate;
    }

    public void setGuideRate(GuideRate guideRate) {
        this.guideRate = guideRate;
    }

    public void setCordWrapPosition(double cordWrapPosition) {
        this.cordWrapPosition = cordWrapPosition;
    }

    public double getCordWrapPosition() {
        return cordWrapPosition;
    }
}
