package com.bobs.coord;

import static java.lang.Math.PI;
import static java.lang.Math.asin;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bobs.mount.Mount;

/**
 * Responsible for all sorts of transformations between coordinate systems such as the usual RA-DEC celestial coordinates, ALT-AZ coordinates and specific angular coordinates for the mount motor controllers.
 */
public class CoordTransformer {

    public static final double ONE_RA_HOUR_IN_DEGREES = 15.0;
    public static final double ONE_DEG_IN_HOURS = 24.0 / 360;
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordTransformer.class);
    private static final Calendar j2000;

    static {
        j2000 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        j2000.set(Calendar.YEAR, 2000);
        j2000.set(Calendar.MONTH, Calendar.JANUARY);
        j2000.set(Calendar.DAY_OF_MONTH, 1);
        j2000.set(Calendar.HOUR_OF_DAY, 12);
        j2000.set(Calendar.MINUTE, 0);
    }

    /**
     * Populate the alt-az values for a given RA/DEC contained in the passed target for the passed mount's location.
     * This esentially transforms RA DEC coordinates to ALT AZ for a location and time.
     * Based on http://www.stargazing.net/kepler/altaz.html#twig02a
     *
     * @param mount
     */
    public void populateAltAzFromRaDec(Mount mount) {
        Calendar cal = mount.getCalendarProvider().provide();
        Calendar utc = convertCalendarToUtcCalendar(cal);
        double raDegrees = convertRaHoursToDeg(mount.getRaHours());
        double decDegrees = mount.getDecDegrees();
        double lst = localSiderealTime(utc, mount.getLongitude());
        double hourAngle = ((lst - raDegrees) + 360) % 360;
        double x = Math.cos(hourAngle * (PI / 180)) * Math.cos(decDegrees * (PI / 180));
        double y = Math.sin(hourAngle * (PI / 180)) * Math.cos(decDegrees * (PI / 180));
        double z = Math.sin(decDegrees * (PI / 180));

        double xhor = x * Math.cos((90 - mount.getLatitude()) * (PI / 180)) - z * Math.sin((90 - mount.getLatitude()) * (PI / 180));
        double yhor = y;
        double zhor = x * Math.sin((90 - mount.getLatitude()) * (PI / 180)) + z * Math.cos((90 - mount.getLatitude()) * (PI / 180));

        double az = Math.atan2(yhor, xhor) * (180 / PI) + 180;
        double alt = asin(zhor) * (180 / PI);

        mount.setAlt(alt);
        mount.setAz(az);
    }

    /**
     * Build a {@link Target} from nexstar alt-az angles when in EQ-north mode.
     * Esentially this is when the mount is being used on a wedge/polar aligned.
     * The actual RA is calculated based on the local sidereal time and how far the mount is from the meridian.
     * The actual DEC is taken as is. Assuming the mount is polar aligned the altitude angle will correspond to DEC
     * <p>
     *
     * @param calendar
     * @param lon
     * @param nexstarAzimuth The angle from the azimuth axis. This is basically the RA axis. 180deg on this axis is the meridian.
     * @param nexstarAlt     The angle from the nexstar alt axis. This should correspond to DEC when in eq-north mode.
     * @return
     */
    public Target buildTargetFromNexstarEqNorth(Calendar calendar, double lon, double nexstarAzimuth, double nexstarAlt) {
        Calendar utc = convertCalendarToUtcCalendar(calendar);
        double lst = localSiderealTime(utc, lon);
        double meridianOffset = (nexstarAzimuth - 180) * -1;
        LOGGER.debug("RA from nexstar mount az azis={}, meridianOffset={}", nexstarAzimuth, meridianOffset);
        Target target = new Target((((lst + meridianOffset) + 360) % 360), nexstarAlt, 0.0, 0.0);
        LOGGER.debug("radeg {}", target.getRaDeg());
        target.setRaHours(convertRaDegToHours(target.getRaDeg()));
        return target;
    }

    /**
     * Return the local sidereal time for the Calender and location longitude passed
     *
     * @param utc
     * @param longitude
     * @return
     */
    public double localSiderealTime(Calendar utc, double longitude) {
        LOGGER.debug("LON {} ", longitude);
        double dayOffset = (utc.getTimeInMillis() - j2000.getTimeInMillis()) / 1000.0 / 60.0 / 60.0 / 24.0;
        double time = (utc.get(Calendar.HOUR_OF_DAY) + utc.get(Calendar.MINUTE) / 60d);
        double lst = ((100.46 + 0.985647 * dayOffset + longitude + 15 * time) + 360) % 360;
        LOGGER.debug("Local sidereal time deg: {} hours: {}", lst, convertRaDegToHours(lst));
        return lst;
    }

    /**
     * Convert right ascention from UOM hours to deg.
     *
     * @param raHours
     * @return
     */
    public double convertRaHoursToDeg(Double raHours) {
        return raHours * ONE_RA_HOUR_IN_DEGREES;
    }

    /**
     * Convert an RA angle in degrees (not hours) to an azimuth axis angle for the mount. This angle needs the current time and earth location (longitude)
     *
     * @param calendar
     * @param lon
     * @param raDeg right ascention measured in degrees.
     * @return
     */
    public double convertRaFromDegToNexstarAzimuthAngle(Calendar calendar, double lon, double raDeg) {
        Calendar utc = convertCalendarToUtcCalendar(calendar);
        double lst = localSiderealTime(utc, lon);
        double meridianOffset = (raDeg - lst) * -1;
        return 180 + meridianOffset;
    }

    /**
     * For EQ north mounted telescopes. 0 deg altitude corresponds to the celestial equator. 90 the pole.
     * Similar to latitude on earth. Anything past 90deg is beyond the pole and down the other side...
     * Convert all angles outside of the +-90 range to be between +-90deg
     * Example 355deg would be -5deg. 91deg would be 89.
     *
     * @param positionAngle the angle reported from the motor controller
     * @return the declinataion angle
     */
    public double convertAltPositionAngleToDecForEqNorth(double positionAngle) {
        if (positionAngle > 180) {
            positionAngle = 180 - (positionAngle - 180);
            positionAngle = positionAngle * -1;
        }
        if (positionAngle > 90) {
            positionAngle = 90 - (positionAngle - 90);
        }
        if (positionAngle < -90) {
            positionAngle = -90 - (positionAngle + 90);
        }
        return positionAngle;
    }

    /**
     * For EQ north mounted telescopes. 0 deg altitude corresponds to the celestial equator. 90 the pole.
     * Similar to latitude on earth. Anything past 90deg is beyond the pole and down the other side...
     * This will convert an RA to the telescope ALT angle when in EQ north. For example, DEC -5deg = 355deg
     *
     * @param dec the declination angle in degrees
     * @return the converted declinationAngle
     */
    public double convertDecToPositionAngleForEqNorth(double dec) {
        if (dec < 0) {
            dec = 360 + dec;
        }
        return dec;
    }

    /**
     * Convert a Calendar to UTC
     * @param calendar
     * @return
     */
    private static Calendar convertCalendarToUtcCalendar(Calendar calendar) {
        final Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(calendar.getTimeInMillis());
        return utc;
    }

    /**
     * Convert right ascention from UOM degrees to hours.
     *
     * @param ra
     * @return
     */
    private double convertRaDegToHours(Double ra) {
        return ra / ONE_RA_HOUR_IN_DEGREES;
    }

}
