package com.bobs.coord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Based on http://www.stargazing.net/kepler/altaz.html#twig02a
 * //FIXME: rename/refactor this sesspit
 */
public class AltAz {

    private static final Logger LOGGER = LoggerFactory.getLogger(AltAz.class);

    public static final double ONE_RA_HOUR_IN_DEGREES = 15.0; //360.00 / 24.0
    public static final double ONE_DEG_IN_HOURS = 24.0 / 360;

    public static Calendar j2000;

    static {
        j2000 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        j2000.set(Calendar.YEAR, 2000);
        j2000.set(Calendar.MONTH, Calendar.JANUARY);
        j2000.set(Calendar.DAY_OF_MONTH, 1);
        j2000.set(Calendar.HOUR_OF_DAY, 12);
        j2000.set(Calendar.MINUTE, 0);
    }

    private static Calendar convertCalendarToUtcCalendar(Calendar calendar) {
        final Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(calendar.getTimeInMillis());
        return utc;
    }


    public Target buildFromRaDec(Calendar calendar, double lat, double lon, double ra, double dec) {
        Calendar utc = convertCalendarToUtcCalendar(calendar);
        double lst = localSiderealTime(utc, lon);
        double hourAngle = ((lst - ra) + 360) % 360;
        double x = Math.cos(hourAngle * (Math.PI / 180)) * Math.cos(dec * (Math.PI / 180));
        double y = Math.sin(hourAngle * (Math.PI / 180)) * Math.cos(dec * (Math.PI / 180));
        double z = Math.sin(dec * (Math.PI / 180));

        double xhor = x * Math.cos((90 - lat) * (Math.PI / 180)) - z * Math.sin((90 - lat) * (Math.PI / 180));
        double yhor = y;
        double zhor = x * Math.sin((90 - lat) * (Math.PI / 180)) + z * Math.cos((90 - lat) * (Math.PI / 180));

        double az = Math.atan2(yhor, xhor) * (180 / Math.PI) + 180;
        double alt = Math.asin(zhor) * (180 / Math.PI);
        return new Target(ra, dec, alt, az);
    }

    /**
     * Build a {@link Target} from nexstar alt-az angles when in EQ-north mode.
     * Esentially this is when the mount is being used on a wedge/polar aligned.
     * The actual RA is calculated based on the local sidereal time and how far the mount is from the meridian.
     * The actual DEC is taken as is. Assuming the mount is polar aligned the altitude angle will correspond to DEC
     * <p>
     * FIXME: Test for parts of the sky under the pole. The dec angle should be max 90
     *
     * @param calendar
     * @param lon
     * @param nexstarAzimuth The angle from the azimuth axis. This is basically the RA axis. 180deg on this axis is the meridian.
     * @param nexstarAlt     The angle from the nexstar alt axis. This should correspond to DEC when in eq-north mode.
     * @return
     */
    public Target buildFromNexstarEqNorth(Calendar calendar, double lon, double nexstarAzimuth, double nexstarAlt) {
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
     * Convert right ascention from UOM degrees to hours.
     *
     * @param ra
     * @return
     */
    public double convertRaDegToHours(Double ra) {
        return ra / ONE_RA_HOUR_IN_DEGREES;
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

    public double convertRaFromDegToNexstarTicks(Calendar calendar, double lon, double raDeg) {
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
     * @param positionAngle
     * @return
     */
    public double convertPositionAngleToDecForEqNorth(double positionAngle) {
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
     * @param dec
     * @return
     */
    public double convertDecToPositionAngleForEqNorth(double dec) {
        if (dec < 0) {
            dec = 360 + dec;
        }
        return dec;
    }
}
