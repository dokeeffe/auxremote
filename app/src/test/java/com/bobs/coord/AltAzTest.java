package com.bobs.coord;

import com.bobs.mount.Mount;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by dokeeffe on 12/21/16.
 */
public class AltAzTest {

    public static final Double HOME_LAT = 52.2;
    public static final Double HOME_LON = 351.6;
    public static final Double BETELGEUSE_RA = 88.79166667;
    public static final Double BETELGEUSE_DEC = 7.40694444;
    public static final Double ALBRERIO_RA = 19.55; //19 31 24
    public static final Double ALBRERIO_DEC = 28.0; //27 59 57

    AltAz sut = new AltAz();

    @Test
    public void testpopulateAltAzFromRaDec_when_BelowHorizion_then_corectAltCalculated() {
        //RA 19.187847689497158, DEC -38.42831125428148 These coords were reported during field testing when the mount crashed into the pier pointing straight down.
        Mount mount = new Mount();
        Target target = new Target();
        target.setRaHours(19.187847689497158);
        target.setDec(-38.42831125428148);
        mount.setLongitude(HOME_LON);
        mount.setLatitude(HOME_LAT);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 12);
        cal.set(Calendar.SECOND, 18);

        sut.populateAltAzFromRaDec(cal, target, mount);

        assertEquals(-60.9, target.getAlt(), 0.1);
        assertEquals(283.4, target.getAz(), 0.1);
    }

    /**
     * Compare conversion against a csv dataset generated using astropy
     *
     * @throws FileNotFoundException
     */
    @Test
    public void conversionOfRaDecToAltAz_when_LargeSetOfPreCalculatedData_then_valuesMatchPreCalculated() throws FileNotFoundException {
        Mount mount = new Mount();
        Target target = new Target();
        Scanner scanner = new Scanner(new File("src/test/resources/radec-altaz.csv"));
        List<Double> altError = new ArrayList<>();
        List<Double> azError = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split(",");
            target.setRaHours(Double.parseDouble(line[0]));
            target.setDec(Double.parseDouble(line[1]));
            mount.setLongitude(HOME_LON);
            mount.setLatitude(HOME_LAT);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.set(Calendar.YEAR, 2017);
            cal.set(Calendar.MONTH, Calendar.FEBRUARY);
            cal.set(Calendar.DAY_OF_MONTH, 5);
            cal.set(Calendar.HOUR_OF_DAY, 20);
            cal.set(Calendar.MINUTE, 12);
            cal.set(Calendar.SECOND, 18);

            sut.populateAltAzFromRaDec(cal, target, mount);

            double expectedAlt = Double.parseDouble(line[2]);
            double expectedAz = Double.parseDouble(line[3]);
            System.out.println("ALT Calc=" + target.getAlt() + " Expected=" + expectedAlt);
            System.out.println("AZ  Calc=" + target.getAz() + " Expected=" + expectedAz);
            altError.add(target.getAlt() - expectedAlt);
            azError.add(target.getAz() - expectedAz);

            assertEquals(target.getAlt(), expectedAlt, 1.5);
            assertEquals(target.getAz(), expectedAz, 1.5);
        }
        System.out.println(altError.stream().mapToDouble(a -> a).summaryStatistics());
        System.out.println(azError.stream().mapToDouble(a -> a).summaryStatistics());


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
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 11);
        cal.set(Calendar.SECOND, 04);

        double ticks = sut.convertRaFromDegToNexstarTicks(cal, HOME_LON, 70.450425);

        System.out.println(ticks);
        assertEquals(180, ticks,0.1);
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