package com.bobs.mount;

import com.bobs.coord.CalendarProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Basic bean responsible for storing the state of the actual physical mount.
 * This is a Singleton bean, there is only one instance in the application.
 * This bean is managed by the {@link MountService}
 * The bean is serialised when the application shuts down and de-serialised on startup.
 * <p>
 * TODO: cleanup mess of objects and primitives used
 */
@Component
public class Mount {

    public static final String PERSISTANCE_STORE = "auxremote-mount.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(Mount.class);
    private static final long ONE_HOUR = 1000 * 60 * 60;
    private static final Double DEFAULT_ALT_SLEW_LIMIT = -30.0;
    private String version;
    private TrackingState trackingState;
    private Double raHours = 0.0;
    private Double decDegrees = 90.0;
    private TrackingMode trackingMode;
    private Double latitude;
    private Double longitude;
    private boolean gpsConnected;
    private Date gpsUpdateTime;
    private String gpsReceiverStatus;
    private Double slewLimitAlt = DEFAULT_ALT_SLEW_LIMIT;
    private Double slewLimitAz;
    private boolean locationSet = false;
    private boolean altSlewInProgress = false;
    private boolean azSlewInProgress = false;
    private boolean aligned = false;
    private boolean cordWrapEnabled;
    private String serialPort;
    private PecMode pecMode;
    private boolean pecIndexFound = false;
    private Boolean error;
    private GuideRate guideRate;
    private double cordWrapPosition;
    private String statusMessage;
    private Double alt;
    private Double az;

    @Autowired
    private CalendarProvider calendarProvider;


    /**
     * On app startup, set defaults, and if possible load a previously persisted state.
     */
    @PostConstruct
    public void loadState() {
        setDefaults();
        loadPersistedState(new File(System.getProperty("user.home"), PERSISTANCE_STORE));
    }

    /**
     * Load previously persisted mount state. Only a subset of properties are loaded
     *
     * @param json
     */
    protected void loadPersistedState(File json) {
        ObjectMapper om = new ObjectMapper();
        try {
            Mount persisted = om.readValue(json, Mount.class);
            this.latitude = persisted.getLatitude();
            this.longitude = persisted.getLongitude();
            this.locationSet = persisted.isLocationSet();
            this.serialPort = persisted.getSerialPort();
            this.trackingState = persisted.getTrackingState();
            if (persisted.getTrackingState() == TrackingState.TRACKING) {
                LOGGER.info("restoring persisted RA DEC posistion from mount. This may not be the actual mount position");
                this.raHours = persisted.getRaHours();
                this.decDegrees = persisted.getDecDegrees();
                this.aligned = persisted.isAligned();
            }
        } catch (Exception e) {
            LOGGER.warn("Could not load saved state, using defaults", e);
        }

    }

    /**
     * Set some defaults
     */
    private void setDefaults() {
        this.trackingState = TrackingState.IDLE;
        this.guideRate = GuideRate.SIDEREAL;
        this.trackingMode = TrackingMode.EQ_NORTH; //currently the only one supported. Once others are developed then this will be loaded from persisted
        this.setPecMode(PecMode.IDLE);
    }

    @PreDestroy
    public void saveState() {
        ObjectMapper om = new ObjectMapper();
        try {
            om.writeValue(new FileOutputStream(new File(System.getProperty("user.home"), PERSISTANCE_STORE)), this);
        } catch (IOException e) {
            LOGGER.warn("error saving mount state", e);
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isGpsConnected() {
        return gpsConnected;
    }

    public void setGpsConnected(boolean gpsConnected) {
        this.gpsConnected = gpsConnected;
    }

    public Date getGpsUpdateTime() {
        return gpsUpdateTime;
    }

    public void setGpsUpdateTime(Date gpsUpdateTime) {
        this.gpsUpdateTime = gpsUpdateTime;
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

    public boolean isCordWrapEnabled() {
        return cordWrapEnabled;
    }

    public void setCordWrapEnabled(boolean cordWrapEnabled) {
        this.cordWrapEnabled = cordWrapEnabled;
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

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public GuideRate getGuideRate() {
        return guideRate;
    }

    public void setGuideRate(GuideRate guideRate) {
        this.guideRate = guideRate;
    }

    public double getCordWrapPosition() {
        return cordWrapPosition;
    }

    public void setCordWrapPosition(double cordWrapPosition) {
        this.cordWrapPosition = cordWrapPosition;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * Returns true if the GPS info is more than 1 hour old
     */
    @JsonIgnore
    public boolean isGpsInfoOld() {
        return this.gpsUpdateTime == null || new Date().getTime() - this.getGpsUpdateTime().getTime() > ONE_HOUR;
    }

    @JsonIgnore
    public boolean isSlewing() {
        return isAltSlewInProgress() || isAzSlewInProgress();
    }

    @JsonIgnore
    public CalendarProvider getCalendarProvider() {
        return calendarProvider;
    }

    public void setCalendarProvider(CalendarProvider calendarProvider) {
        this.calendarProvider = calendarProvider;
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        this.alt = alt;
    }

    public Double getAz() {
        return az;
    }

    public void setAz(Double az) {
        this.az = az;
    }
}
