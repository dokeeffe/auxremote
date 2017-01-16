package com.bobs.coord;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 12/21/16.
 */
public class AltAzTest {

    public static final Double CORK_LAT = 51.896892;
    public static final Double CORK_LON = -8.486316;
    public static final Double HOME_LAT = 52.25323273260789;
    public static final Double HOME_LON = 351.63910339111703;
    public static final Double BETELGEUSE_RA = 88.79166667;
    public static final Double BETELGEUSE_DEC = 7.40694444;
    public static final Double DUBHE_RA = 165.92916667;
    public static final Double DUBHE_DEC = 61.75111111;
    public static final Double ALBRERIO_RA = 19.55; //19 31 24
    public static final Double ALBRERIO_DEC = 28.0; //27 59 57

    AltAz sut = new AltAz();

    @Test
    public void testBuildFromRaDec() throws Exception {
        double delta = 0.1;
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal2.set(Calendar.YEAR, 2016);
        cal2.set(Calendar.MONTH, Calendar.DECEMBER);
        cal2.set(Calendar.DAY_OF_MONTH, 21);
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 10);

        Target result = sut.buildFromRaDec(cal2, CORK_LAT, CORK_LON, BETELGEUSE_RA, BETELGEUSE_DEC);

        assertEquals(42.8, result.getAlt(), delta);
        assertEquals(154.0, result.getAz(), delta);
        result = sut.buildFromRaDec(cal2, CORK_LAT, CORK_LON, DUBHE_RA, DUBHE_DEC);
        assertEquals(41.5, result.getAlt(), delta);
        assertEquals(39.0, result.getAz(), delta);
    }

    @Test
    public void testBuildFromNexstarEqNorthForBetelgeuse() {
        double delta = 0.1;
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal2.set(Calendar.YEAR, 2016);
        cal2.set(Calendar.MONTH, Calendar.DECEMBER);
        cal2.set(Calendar.DAY_OF_MONTH, 27);
        cal2.set(Calendar.HOUR_OF_DAY, 00);
        cal2.set(Calendar.MINUTE, 02);
        double valFromNexstarAzMcBoard = 176.91956859347633;
        double valFromNexstarAltMcBoard = 7.407446349110982;

        Target result = sut.buildFromNexstarEqNorth(cal2, HOME_LON, valFromNexstarAzMcBoard, valFromNexstarAltMcBoard);

        System.out.println("RA diff " + (BETELGEUSE_RA - result.getRaDeg()));
        System.out.println("DEC diff " + (BETELGEUSE_DEC - result.getDec()));
        System.out.println("hours " + sut.convertRaDegToHours(result.getRaDeg()));
        assertEquals(BETELGEUSE_DEC, result.getDec(), delta);
        assertEquals(6.0, result.getRaHours(), delta);
    }

    @Test
    public void testBuildFromNexstarEqNorthForAlbriero() {
        double delta = 0.1;
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal2.set(Calendar.YEAR, 2016);
        cal2.set(Calendar.MONTH, Calendar.DECEMBER);
        cal2.set(Calendar.DAY_OF_MONTH, 27);
        cal2.set(Calendar.HOUR_OF_DAY, 16);
        cal2.set(Calendar.MINUTE, 12);
        double valFromNexstarAzMcBoard = 218.484905867869;
        double valFromNexstarAltMcBoard = 28.0;

        Target result = sut.buildFromNexstarEqNorth(cal2, HOME_LON, valFromNexstarAzMcBoard, valFromNexstarAltMcBoard);

        System.out.println("RA diff " + (ALBRERIO_RA - result.getRaDeg()));
        System.out.println("DEC diff " + (ALBRERIO_DEC - result.getDec()));
        System.out.println("hours " + result.getRaHours());
        assertEquals(ALBRERIO_RA, result.getRaHours(), delta);
        assertEquals(ALBRERIO_DEC, result.getDec(), delta);
    }

    @Test
    public void testLocalSiderealTime() {
        double result = sut.localSiderealTime(Calendar.getInstance(), HOME_LON);
        System.out.println("LST: " + result);
        System.out.println("LST: " + sut.convertRaDegToHours(result));
    }

    @Test
    public void testConvertRaDegToHours() {
        double delta = 0.02;
        double result = sut.convertRaDegToHours(BETELGEUSE_RA);
        assertEquals(5.919, result, delta);
    }

    @Test
    public void testConverHoursToDeg() {
        double delta = 0.02;
        double result = sut.convertRaHoursToDeg(5.919);
        assertEquals(BETELGEUSE_RA, result, delta);
    }

    @Test
    public void testConvertRaFromDegToNexstarTicks_ForAlbriero() {
        double delta = 0.1;
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal2.set(Calendar.YEAR, 2016);
        cal2.set(Calendar.MONTH, Calendar.DECEMBER);
        cal2.set(Calendar.DAY_OF_MONTH, 27);
        cal2.set(Calendar.HOUR_OF_DAY, 16);
        cal2.set(Calendar.MINUTE, 12);

        double result = sut.convertRaFromDegToNexstarTicks(cal2, HOME_LON, sut.convertRaHoursToDeg(ALBRERIO_RA));

        assertEquals(218, result, delta);
    }

    @Test
    public void testConvertToNexstarTicks() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 27);
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 12);

        double ticks = sut.convertRaFromDegToNexstarTicks(cal, HOME_LON, 196.66813);

        System.out.println(ticks);
//        assertEquals("8BDA57", result)

//        2016-12-29 23:41:59.767 DEBUG 24548 --- [cTaskExecutor-1] com.bobs.coord.AltAz                     : LON 351.639317967851
//        2016-12-29 23:41:59.767 DEBUG 24548 --- [cTaskExecutor-1] com.bobs.coord.AltAz                     : Local sidereal time deg: 85.74006541467588 hours: 5.716004360978392
//        2016-12-29 23:41:59.767 DEBUG 24548 --- [cTaskExecutor-1] com.bobs.coord.AltAz                     : RA from nexstar mount az azis=196.66813830543387

    }

    @Test
    public void testConvertPositionAngleToDecForEqNorth() {
        assertEquals(0, sut.convertPositionAngleToDecForEqNorth(0.0), 0);
        assertEquals(45, sut.convertPositionAngleToDecForEqNorth(45.0), 0);
        assertEquals(90, sut.convertPositionAngleToDecForEqNorth(90.0), 0);
        assertEquals(89, sut.convertPositionAngleToDecForEqNorth(91.0), 0);
        assertEquals(-10, sut.convertPositionAngleToDecForEqNorth(350.0), 0);
        assertEquals(-45, sut.convertPositionAngleToDecForEqNorth(360 - 45), 0);
        assertEquals(-90, sut.convertPositionAngleToDecForEqNorth(360 - 90), 0);
        assertEquals(-89, sut.convertPositionAngleToDecForEqNorth(360 - 91), 0);
        assertEquals(-20, sut.convertPositionAngleToDecForEqNorth(200.0), 0);
    }

    @Test
    public void testConvertDecToPositionAngleForEqNorth() {
        assertEquals(350, sut.convertDecToPositionAngleForEqNorth(-10), 0);
    }
}