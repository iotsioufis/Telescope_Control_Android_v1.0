package com.my_project.telescopecontrol;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import java.time.LocalDateTime;
import java.time.LocalTime;

import java.time.ZoneId;

import static android.content.Context.MODE_PRIVATE;
import static com.my_project.telescopecontrol.MainActivity.SHARED_PREFS;
import static java.lang.Math.*;

import Jama.Matrix;

import static android.content.ContentValues.TAG;

import static java.lang.Math.floor;

public class SolarCalcs {


    double d;
    double Ls; //Sun's  mean longitude
    double Ms;   //Sun's  mean anomaly:

    public double[] SunPosition(int hours_in_future) {


        double J2000 = 2451543.5;

        //Calculations of current Julian day:
        LocalDateTime UTCtime = LocalDateTime.now(ZoneId.of("GMT"));


        int year = UTCtime.getYear();
        int month = UTCtime.getMonthValue();
        int day = UTCtime.getDayOfMonth();
        int hours = UTCtime.getHour() + hours_in_future;
        int min = UTCtime.getMinute();
        int sec = UTCtime.getSecond();
        double JD = julianday(year, month, day, hours, min, sec);
        //double JD = julianday(2022, 8, 14, 5, 34, 0);
        d = JD - J2000;


        Log.e(TAG, "d is: " + d);
        // d = -3543;

        double w = 282.9404 + 4.70935E-5 * d;  // (longitude of perihelion)
        double a = 1.000000;                         //   (mean distance, a.u.)
        double e = 0.016709 - 1.151E-9 * d;  //(eccentricity)
        double M = 356.0470 + 0.9856002585 * d;  // (mean anomaly)



        /*We also need the obliquity of the ecliptic, oblecl:*/

        double oblecl = 23.4393 - 3.563E-7 * d;

        /*and the Sun's mean longitude, L:*/

        double L = w + M;
        /*All angular elements should be normalized to within 0-360 degrees:*/
        L = mod(L, 360);
        w = mod(w, 360);
        a = mod(a, 360);
        e = mod(e, 360);
        M = mod(M, 360);
        Ls = L;
       /* computing an auxiliary angle, the eccentric anomaly. Since the eccentricity of the Sun's (i.e. the Earth's) orbit is so small, 0.017,
       a first approximation of E will be accurate enough. Below E and M are in degrees:*/

        double E = M + (180.0 / PI) * e * sin(toRadians(M)) * (1 + e * cos(toRadians(M)));
        /* Now we compute the Sun's rectangular coordinates in the plane of the ecliptic, where the X axis points towards the perihelion:
         */

        double x = cos(toRadians(E)) - e;
        double y = sin(toRadians(E)) * sqrt(1 - e * e);

        double r = sqrt(x * x + y * y);
        double v = atan2(toRadians(y), toRadians(x));
        v = toDegrees(v);
        double lon = v + w;
        lon = mod(lon, 360);

        x = r * cos(toRadians(lon));
        y = r * sin(toRadians(lon));
        double z = 0.0;

        double xequat_sun = x;
        double yequat_sun = y * cos(toRadians(23.4406)) - 0.0 * sin(toRadians(23.4406));
        double zequat_sun = y * sin(toRadians(23.4406)) + 0.0 * cos(toRadians(23.4406));

        /* Convert to RA and Decl:*/

        r = sqrt(xequat_sun * xequat_sun + yequat_sun * yequat_sun + zequat_sun * zequat_sun);
        double RA_sun = atan2(yequat_sun, xequat_sun);
        RA_sun = v = toDegrees(RA_sun / 15);
        double Decl_sun = atan2(zequat_sun, sqrt(xequat_sun * xequat_sun + yequat_sun * yequat_sun));
        Decl_sun = toDegrees(Decl_sun);


        Ms = M;


        double[] sun_rectangular_coords = {x, y, z};
        return sun_rectangular_coords;

    }


    public double[] GetPlanetCoords(String name, SharedViewModel viewModel, int hours_in_future) {

        //viewModel.setStar_object(viewModel.getStar_object().getValue());
        double N = 0.0;
        double i = 0.0;
        double w = 0.0;
        double a = 0.0;
        double e = 0.0;
        double M = 0.0;
        double Mj = 0.0;
        double Ms = 0.0;
        double Mu = 0.0;
        double J2000 = 2451543.5;

        //Calculations of current Julian day:
        LocalDateTime UTCtime = LocalDateTime.now(ZoneId.of("GMT"));

        int year = UTCtime.getYear();
        int month = UTCtime.getMonthValue();
        Log.e(TAG, "month is: " + month);
        int day = UTCtime.getDayOfMonth();
        int hours = UTCtime.getHour();
        int min = UTCtime.getMinute();
        int sec = UTCtime.getSecond();
        double JD = julianday(year, month, day, hours + hours_in_future, min, sec);
        double d = JD - J2000;
        int counter = 1;

        if (name.equals("Mars")) {
            N = 49.5574 + 2.11081E-5 * d;
            i = 1.8497 - 1.78E-8 * d;
            w = 286.5016 + 2.92961E-5 * d;
            a = 1.523688;
            e = 0.093405 + 2.516E-9 * d;
            M = 18.6021 + 0.5240207766 * d;

        }
        if (name.equals("Mercury")) {
            N = 48.3313 + 3.24587E-5 * d;
            i = 7.0047 + 5.00E-8 * d;
            w = 29.1241 + 1.01444E-5 * d;
            a = 0.387098;
            e = 0.205635 + 5.59E-10 * d;
            M = 168.6562 + 4.0923344368 * d;

        }


        if (name.equals("Neptune")) {
            N = 131.7806 + 3.0173E-5 * d;
            i = 1.7700 - 2.55E-7 * d;
            w = 272.8461 - 6.027E-6 * d;
            a = 30.05826 + 3.313E-8 * d;//  (AU)
            e = 0.008606 + 2.15E-9 * d;
            M = 260.2471 + 0.005995147 * d;

        }

        if (name.equals("Venus")) {
            N = 76.6799 + 2.46590E-5 * d;
            i = 3.3946 + 2.75E-8 * d;
            w = 54.8910 + 1.38374E-5 * d;
            a = 0.723330;
            e = 0.006773 - 1.302E-9 * d;
            M = 48.0052 + 1.6021302244 * d;

        }
        if (name.equals("Jupiter")) {
            N = 100.4542 + 2.76854E-5 * d;
            i = 1.3030 - 1.557E-7 * d;
            w = 273.8777 + 1.64505E-5 * d;
            a = 5.20256;// (AU)
            e = 0.048498 + 4.469E-9 * d;
            M = 19.8950 + 0.0830853001 * d;
            Mj = mod(M, 360);
            Ms = mod(316.9670 + 0.0334442282 * d, 360);
        }

        if (name.equals("Saturn")) {
            N = 113.6634 + 2.38980E-5 * d;
            i = 2.4886 - 1.081E-7 * d;
            w = 339.3939 + 2.97661E-5 * d;
            a = 9.55475;          //  (AU)
            e = 0.055546 - 9.499E-9 * d;
            M = 316.9670 + 0.0334442282 * d;
            Ms = mod(M, 360);
            Mj = mod(19.8950 + 0.0830853001 * d, 360);


        }

        if (name.equals("Uranus")) {
            N = 74.0005 + 1.3978E-5 * d;
            i = 0.7733 + 1.9E-8 * d;
            w = 96.6612 + 3.0565E-5 * d;
            a = 19.18171 - 1.55E-8 * d;    //  (AU)
            e = 0.047318 + 7.45E-9 * d;
            M = 142.5905 + 0.011725806 * d;
            Mu = mod(M, 360);
            Ms = mod(316.9670 + 0.0334442282 * d, 360);
            Mj = mod(19.8950 + 0.0830853001 * d, 360);

        }










        /*All angular elements should be normalized to within 0-360 degrees:*/
        N = mod(N, 360);
        i = mod(i, 360);
        w = mod(w, 360);
        a = mod(a, 360);
        e = mod(e, 360);
        M = mod(M, 360);
        double E0 = M + (180 / PI) * e * sin(toRadians(M)) * (1 + e * cos(toRadians(M)));
        double E1 = E0 - (E0 - (180 / PI) * e * sin(toRadians(E0)) - M) / (1 - e * cos(toRadians(E0)));


        /*calculate new E1 until the difference with E0 is small enough or until 10 iterations to avoid infinite loop */
        while (abs(E1 - E0) > 0.001 || counter > 10) {

            E0 = E1;


            E1 = E0 - (E0 - (180 / PI) * e * sin(toRadians(E0)) - M) / (1 - e * cos(toRadians(E0)));
            counter++;

        }
        double E = E0;

         /*  Now we've computed E - the next step is to compute the Planet's distance and true anomaly.
          First we compute rectangular (x,y) coordinates in the plane of the Planet orbit:
          */

        double x = a * (cos(toRadians(E)) - e);
        double y = a * sqrt(1 - e * e) * sin(toRadians(E));

        /*Then we convert this to distance and true anonaly:*/

        double r = sqrt(x * x + y * y);
        double v = atan2(y, x);
        v = toDegrees(v);
        /* Now we know the Planet's position in the plane of the planet orbit. To compute the Planet's position in ecliptic coordinates, we apply these formulae:*/

        double xeclip = r * (cos(toRadians(N)) * cos(toRadians(v + w)) - sin(toRadians(N)) * sin(toRadians(v + w)) * cos(toRadians(i)));
        double yeclip = r * (sin(toRadians(N)) * cos(toRadians(v + w)) + cos(toRadians(N)) * sin(toRadians(v + w)) * cos(toRadians(i)));
        double zeclip = r * sin(toRadians(v + w)) * sin(toRadians(i));


        if (name.equals("Jupiter")) {
            double longitude_pert = -0.332 * sin(toRadians(2 * Mj - 5 * Ms - 67.6))
                    - 0.056 * sin(toRadians(2 * Mj - 2 * Ms + 21))
                    + 0.042 * sin(toRadians(3 * Mj - 5 * Ms + 21))
                    - 0.036 * sin(toRadians(Mj - 2 * Ms))
                    + 0.022 * cos(toRadians(Mj - Ms))
                    + 0.023 * sin(toRadians(2 * Mj - 3 * Ms + 52))
                    - 0.016 * sin(toRadians(Mj - 5 * Ms - 69));


            double eclip_long = atan2(yeclip, xeclip);
            double eclip_lat = atan2(zeclip, sqrt(xeclip * xeclip + yeclip * yeclip));
            r = sqrt(xeclip * xeclip + yeclip * yeclip + zeclip * zeclip);
            eclip_long = toDegrees(eclip_long);
            //  eclip_long=mod(eclip_long,360);
            eclip_lat = toDegrees(eclip_lat);
            // eclip_lat=mod(eclip_lat,360);
            eclip_long = eclip_long + longitude_pert;
            xeclip = r * cos(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
            yeclip = r * sin(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
            zeclip = r * sin(toRadians(eclip_lat));
        }


        if (name.equals("Saturn")) {
            double longitude_pert = +0.812 * sin(toRadians(2 * Mj - 5 * Ms - 67.6))
                    - 0.229 * cos(toRadians(2 * Mj - 4 * Ms - 2))
                    + 0.119 * sin(toRadians(Mj - 2 * Ms - 3))
                    + 0.046 * sin(toRadians(2 * Mj - 6 * Ms - 69))
                    + 0.014 * sin(toRadians(Mj - 3 * Ms + 32));

            double latitude_pert = -0.020 * cos(toRadians(2 * Mj - 4 * Ms - 2))
                    + 0.018 * sin(toRadians(2 * Mj - 6 * Ms - 49));

            double eclip_long = atan2(yeclip, xeclip);
            double eclip_lat = atan2(zeclip, sqrt(xeclip * xeclip + yeclip * yeclip));
            r = sqrt(xeclip * xeclip + yeclip * yeclip + zeclip * zeclip);
            eclip_long = toDegrees(eclip_long);
            //  eclip_long=mod(eclip_long,360);
            eclip_lat = toDegrees(eclip_lat);
            // eclip_lat=mod(eclip_lat,360);
            eclip_long = eclip_long + longitude_pert;
            eclip_lat = eclip_lat + latitude_pert;
            xeclip = r * cos(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
            yeclip = r * sin(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
            zeclip = r * sin(toRadians(eclip_lat));
        }

        if (name.equals("Uranus")) {
            double longitude_pert = +0.040 * sin(toRadians(Ms - 2 * Mu + 6))
                    + 0.035 * sin(toRadians(Ms - 3 * Mu + 33))
                    - 0.015 * sin(toRadians(Mj - Mu + 20));

            double eclip_long = atan2(yeclip, xeclip);
            double eclip_lat = atan2(zeclip, sqrt(xeclip * xeclip + yeclip * yeclip));
            r = sqrt(xeclip * xeclip + yeclip * yeclip + zeclip * zeclip);
            eclip_long = toDegrees(eclip_long);
            //  eclip_long=mod(eclip_long,360);
            eclip_lat = toDegrees(eclip_lat);
            // eclip_lat=mod(eclip_lat,360);
            eclip_long = eclip_long + longitude_pert;
            xeclip = r * cos(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
            yeclip = r * sin(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
            zeclip = r * sin(toRadians(eclip_lat));
        }


        if (name.equals("Pluto")) {
            double S = toRadians(50.03 + 0.033459652 * d);
            double P = toRadians(238.95 + 0.003968789 * d);


            double eclip_long = 238.9508 + 0.00400703 * d
                    - 19.799 * sin(P) + 19.848 * cos(P)
                    + 0.897 * sin(2 * P) - 4.956 * cos(2 * P)
                    + 0.610 * sin(3 * P) + 1.211 * cos(3 * P)
                    - 0.341 * sin(4 * P) - 0.190 * cos(4 * P)
                    + 0.128 * sin(5 * P) - 0.034 * cos(5 * P)
                    - 0.038 * sin(6 * P) + 0.031 * cos(6 * P)
                    + 0.020 * sin(S - P) - 0.010 * cos(S - P);

            double eclip_lat = -3.9082
                    - 5.453 * sin(P) - 14.975 * cos(P)
                    + 3.527 * sin(2 * P) + 1.673 * cos(2 * P)
                    - 1.051 * sin(3 * P) + 0.328 * cos(3 * P)
                    + 0.179 * sin(4 * P) - 0.292 * cos(4 * P)
                    + 0.019 * sin(5 * P) + 0.100 * cos(5 * P)
                    - 0.031 * sin(6 * P) - 0.026 * cos(6 * P)
                    + 0.011 * cos(S - P);

            r = 40.72
                    + 6.68 * sin(P) + 6.90 * cos(P)
                    - 1.18 * sin(2 * P) - 0.03 * cos(2 * P)
                    + 0.15 * sin(3 * P) - 0.14 * cos(3 * P);

            xeclip = r * cos(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
            yeclip = r * sin(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
            zeclip = r * sin(toRadians(eclip_lat));


        }



        /*To convert the planets' heliocentric positions to geocentric positions, we add the Sun's rectangular (x,y,z) coordinates to the rectangular (x,y,z) heliocentric coordinates of the planet:*/

        double[] sun_rectangular_coords = SunPosition(0);
        double xgeoc = xeclip + sun_rectangular_coords[0];
        double ygeoc = yeclip + sun_rectangular_coords[1];
        double zgeoc = zeclip + sun_rectangular_coords[2];

        /*After having calculated the rectangular ecliptic coordinates of the Planet we and rotate them to get rectangular equatorial coordinates:*/

        double oblecl = 23.4393 - 3.563E-7 * d;

        double xequat_planet = xgeoc;
        double yequat_planet = ygeoc * cos(toRadians(oblecl)) - zgeoc * sin(toRadians(oblecl));
        double zequat_planet = ygeoc * sin(toRadians(oblecl)) + zgeoc * cos(toRadians(oblecl));

        /* Convert to RA and Decl:*/

        r = sqrt(xequat_planet * xequat_planet + yequat_planet * yequat_planet + zequat_planet * zequat_planet);
        double RA = atan2(yequat_planet, xequat_planet);

        RA = toDegrees(RA);
        RA = mod(RA, 360);
        double Decl = atan2(zequat_planet, sqrt(xequat_planet * xequat_planet + yequat_planet * yequat_planet));
        Decl = toDegrees(Decl);
        //  Decl=mod(Decl,360);
        double Planet_coords[] = calculate_preccesion(RA, Decl);


        // double Planet_coords[]={RA,Decl};


        return Planet_coords;


    }


    public double[] GetMoonCoords(SharedViewModel viewModel, int hours_in_future) {
        /*  The orbital elements of the Moon are:*/
        // d = -3543;
        SunPosition(hours_in_future);
        double N = 125.1228 - 0.0529538083 * d;  // (Long asc. node)
        double i = 5.1454;                        //  (Inclination)
        double w = 318.0634 + 0.1643573223 * d;  // (Arg. of perigee)
        double a = 60.2666;                              // (Mean distance)
        double e = 0.054900;                              // (Eccentricity)
        double M = 115.3654 + 13.0649929509 * d;  // (Mean anomaly)
        /*All angular elements should be normalized to within 0-360 degrees:*/
        N = mod(N, 360);
        i = mod(i, 360);
        w = mod(w, 360);
        a = mod(a, 360);
        e = mod(e, 360);
        M = mod(M, 360);


        double E0 = M + (180 / PI) * e * sin(toRadians(M)) * (1 + e * cos(toRadians(M)));
        double E1 = E0 - (E0 - (180 / PI) * e * sin(toRadians(E0)) - M) / (1 - e * cos(toRadians(E0)));
        int counter = 0;
        Log.e(TAG, "counter is: " + counter);
        /*calculate new E1 until the difference with E0 is small enough or until 10 iterations to avoid infinite loop */
        while (abs(E1 - E0) > 0.001 || counter > 10) {

            E0 = E1;


            E1 = E0 - (E0 - (180 / PI) * e * sin(toRadians(E0)) - M) / (1 - e * cos(toRadians(E0)));
            counter++;

        }
        double E = E1;

         /*  Now we've computed E - the next step is to compute the Planet's distance and true anomaly.
          First we compute rectangular (x,y) coordinates in the plane of the Planet orbit:
          */

        double x = a * (cos(E * PI / 180) - e);
        double y = a * sqrt(1 - e * e) * sin(toRadians(E));

        /*Then we convert this to distance and true anonaly:*/

        double r = sqrt(x * x + y * y);
        double v = atan2(y, x);
        v = toDegrees(v);
        v = mod(v, 360);
        /* Now we know the Planet's position in the plane of the planet orbit. To compute the Planet's position in ecliptic coordinates, we apply these formulae:*/

        double xeclip = r * (cos(toRadians(N)) * cos(toRadians(v + w)) - sin(toRadians(N)) * sin(toRadians(v + w)) * cos(toRadians(i)));
        double yeclip = r * (sin(toRadians(N)) * cos(toRadians(v + w)) + cos(toRadians(N)) * sin(toRadians(v + w)) * cos(toRadians(i)));
        double zeclip = r * sin(toRadians(v + w)) * sin(toRadians(i));

        double eclip_long = atan2(yeclip, xeclip);
        double eclip_lat = atan2(zeclip, sqrt(xeclip * xeclip + yeclip * yeclip));
        r = sqrt(xeclip * xeclip + yeclip * yeclip + zeclip * zeclip);
        eclip_long = toDegrees(eclip_long);
        //  eclip_long=mod(eclip_long,360);
        eclip_lat = toDegrees(eclip_lat);
        // eclip_lat=mod(eclip_lat,360);

        /* Calculations for higher accuracy :*/
        double[] sun_rectangular_coords = SunPosition(hours_in_future);
        double xgeoc = xeclip + sun_rectangular_coords[0];
        double ygeoc = yeclip + sun_rectangular_coords[1];
        double zgeoc = zeclip + sun_rectangular_coords[2];


        // double      Ls    ;  // Sun's  mean longitude   (already computed)
        double Lm = N + w + M;  //Moon's mean longitude: (  M w,M, for the Moon)
        Lm = mod(Lm, 360);
        // double   Ms  ;  //Sun's  mean anomaly:        (already computed)
        double Mm = M;   // Moon's mean anomaly:   (already computed)
        Mm = mod(Mm, 360);
        double D = Lm - Ls; // Moon's mean elongation:
        D = mod(D, 360);
        double F = Lm - N; //  Moon's argument of latitude:
        F = mod(F, 360);



        /* compute and add up the 12 largest perturbation terms in longitude, the 5 largest in latitude, and the 2 largest in distance.
         These are all the perturbation terms with an amplitude larger than 0.01_deg in longitude resp latitude.
        In the lunar distance, only the perturbation terms larger than 0.1 Earth radii has been included:*/

        /*Perturbations in longitude (degrees):*/

        double longitude_pert = (-1.274 * sin(toRadians(Mm - 2 * D))      //Evection
                + 0.658 * sin(toRadians(2 * D))               //Variation
                - 0.186 * sin(toRadians(Ms))               //Yearly equation
                - 0.059 * sin(toRadians(2 * Mm - 2 * D))
                - 0.057 * sin(toRadians(Mm - 2 * D + Ms))
                + 0.053 * sin(toRadians(Mm + 2 * D))
                + 0.046 * sin(toRadians(2 * D - Ms))
                + 0.041 * sin(toRadians(Mm - Ms))
                - 0.035 * sin(toRadians(D))                //Parallactic equation
                - 0.031 * sin(toRadians(Mm + Ms))
                - 0.015 * sin(toRadians(2 * F - 2 * D))
                + 0.011 * sin(toRadians(Mm - 4 * D)));

        /*Perturbations in latitude (degrees):*/

        double latitude_pert = (-0.173 * sin(toRadians(F - 2 * D))
                - 0.055 * sin(toRadians(Mm - F - 2 * D))
                - 0.046 * sin(toRadians(Mm + F - 2 * D))
                + 0.033 * sin(toRadians(F + 2 * D))
                + 0.017 * sin(toRadians(2 * Mm + F)));


        /*Perturbations in lunar distance (Earth radii):*/

        double distance_pert = (-0.58 * cos(toRadians(Mm - 2 * D))
                - 0.46 * cos(toRadians(2 * D)));

        /* Add the Perturbations to the ecliptic positions already computed:*/

        eclip_long = eclip_long + longitude_pert;
        eclip_lat = eclip_lat + latitude_pert;
        r = r + distance_pert;



         /*convert these ecliptic coordinates to Right Ascension and Declination.
          first convert the ecliptic longitude/latitude to rectangular (x,y,z) coordinates
           then rotate this x,y,z, system through an angle corresponding to the obliquity of the ecliptic
            then convert back to spherical coordinates.
             The Moon's distance doesn't matter here, and one can therefore set r=1.0.
          */


        double oblecl = 23.4406; //the obliquity of the ecliptic
        double previous_r = r;
        r = 1.0;
        xeclip = r * cos(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
        yeclip = r * sin(toRadians(eclip_long)) * cos(toRadians(eclip_lat));
        zeclip = r * sin(toRadians(eclip_lat));

        double xequat_moon = xeclip;
        double yequat_moon = yeclip * cos(toRadians(oblecl)) - zeclip * sin(toRadians(oblecl));
        double zequat_moon = yeclip * sin(toRadians(oblecl)) + zeclip * cos(toRadians(oblecl));

        double RA_moon = atan2(yequat_moon, xequat_moon);
        RA_moon = toDegrees(RA_moon);
        RA_moon = mod(RA_moon, 360);
        double Decl_moon = atan2(zequat_moon, sqrt(xequat_moon * xequat_moon + yequat_moon * yequat_moon));
        Decl_moon = toDegrees(Decl_moon);
        //   Decl_moon=mod(Decl_moon,360);


//double lst =GetLST(viewModel)*15;

        /*To compute the Moon's topocentric position,we need to know the Moon's geocentric Right Ascension and Declination (RA, Decl),
        the Local Sidereal Time (LST), and our latitude (lat):*/

        /* compute the Moon's parallax, i.e. the apparent size of the (equatorial) radius of the Earth, as seen from the Moon:*/
        r = previous_r;
        double mpar = toDegrees(asin(1 / r));



        /*convert observer's latitude to geocentric latitude accounting for the flattening of the Earth:*/
        double gclat = viewModel.getLatitute().getValue() - 0.1924 * sin(toRadians(2 * viewModel.getLatitute().getValue()));
        double rho = 0.99833 + 0.00167 * cos(toRadians(2 * viewModel.getLatitute().getValue()));

        /*Next we compute the Moon's geocentric Hour Angle (HA):

        HA = LST - RA*/

        double HA = (GetLST(viewModel, hours_in_future)) * 15 - RA_moon;

        //mpar=0.9243;

        /* We also need an auxiliary angle, g:*/


        double g_temp = tan(toRadians(gclat) / cos(toRadians(HA)));
        double g = (atan(g_temp));
        g = toDegrees(g);
        g = mod(g, 360);


        /*convert the geocentric Right Ascention and Declination (RA, Decl) to their topocentric values (topRA, topDecl):*/
        double test_value = mpar * rho * cos(toRadians(gclat)) * sin(toRadians(HA)) / cos(toRadians(Decl_moon));

        double topRA = RA_moon - mpar * rho * cos(toRadians(gclat)) * (sin(toRadians(HA)) / cos(toRadians(Decl_moon)));
        double topDecl = Decl_moon - mpar * rho * sin(toRadians(gclat)) * sin(toRadians(g - Decl_moon)) / sin(toRadians(g));

//topRA=mod(topRA,360);
//topDecl=mod(topDecl,360);


        double MoonCoords[] = calculate_preccesion(topRA, topDecl);

        //double[] MoonCoords={topRA,topDecl};
        return MoonCoords;
    }


    public double[] GetPlanetTrackingRates(String name, SharedViewModel viewModel) {

        double[] planet_coords = GetPlanetCoords(name, viewModel, 0);
        double[] planet_future_coords = GetPlanetCoords(name, viewModel, 1);
        double dif_in_RA = planet_future_coords[0] - planet_coords[0];
        double dif_in_DEC = planet_future_coords[1] - planet_coords[1];
        double RA_tracking_rate = viewModel.get_sidereal_rate().getValue() - (dif_in_RA * viewModel.getRA_micro_1().getValue() * 4 / 3600);
          /* if(viewModel.getLatitute().getValue()<0){
               RA_tracking_rate = -viewModel.get_sidereal_rate().getValue() + (dif_in_RA * viewModel.getRA_micro_1().getValue() * 4 / 3600);
                            }*/
        double DEC_tracking_rate = dif_in_DEC * viewModel.getDEC_micro_1().getValue() * 4 / 3600;
        if (viewModel.getSide_of_meridian().getValue().equals("east")) {
            DEC_tracking_rate = -DEC_tracking_rate;
        }
        double[] tracking_rates = {-RA_tracking_rate, DEC_tracking_rate};

          if(viewModel.getLatitute().getValue()<0){
              tracking_rates[0] = RA_tracking_rate;
              tracking_rates[1] = - DEC_tracking_rate;
                            }
        Log.e(TAG, "planet_coords are : ra : " + planet_coords[0] + " dec : " + planet_coords[1]);
        Log.e(TAG, "planet_future_coords are : ra : " + planet_future_coords[0] + " dec : " + planet_future_coords[1]);
        Log.e(TAG, "diferences are : ra : " + dif_in_RA + " dec : " + dif_in_DEC);


        Log.e(TAG, "Tracking rates: ra : " + tracking_rates[0] + " dec : " + tracking_rates[1]);

        // Log.e(TAG, "Tracking rates: ra : "+dif_in_RA+" dec : "+viewModel.getLatitute().getValue());
        return tracking_rates;
    }

    public double[] GetMoonTrackingRates(SharedViewModel viewModel) {

        double[] moon_coords = GetMoonCoords(viewModel, 0);
        double[] moon_future_coords = GetMoonCoords(viewModel, 1);
        double dif_in_RA = moon_future_coords[0] - moon_coords[0];
        double dif_in_DEC = moon_future_coords[1] - moon_coords[1];

        Log.e(TAG, "moon_coords are : ra : " + moon_coords[0] + " dec : " + moon_coords[1]);
        Log.e(TAG, "moon_future_coords are : ra : " + moon_future_coords[0] + " dec : " + moon_future_coords[1]);
        Log.e(TAG, "diferences are : ra : " + dif_in_RA + " dec : " + dif_in_DEC);
        double RA_tracking_rate = viewModel.get_sidereal_rate().getValue() - (dif_in_RA * viewModel.getRA_micro_1().getValue() * 4 / 3600);
        double DEC_tracking_rate = dif_in_DEC * viewModel.getDEC_micro_1().getValue() * 4 / 3600;
        double[] tracking_rates = {-RA_tracking_rate, DEC_tracking_rate};

        if(viewModel.getLatitute().getValue()<0){
            tracking_rates[0] = RA_tracking_rate;
            tracking_rates[1] = -DEC_tracking_rate;
        }
        Log.e(TAG, "Tracking rates: ra : " + tracking_rates[0] + " dec : " + tracking_rates[1]);

        // Log.e(TAG, "Tracking rates: ra : "+dif_in_RA+" dec : "+viewModel.getLatitute().getValue());
        return tracking_rates;
    }


    private double julianday(int year, int month, int day, int hour, int minute, int second) {
        if (month <= 2) { // January & February
            year = year - 1;
            month = month + 12;
        }
        double dayFraction = (hour + minute / 60.0 + second / 3600.0) / 24.0;
        double day2 = floor(365.25 * (year + 4716.0)) + floor(30.6001 * (month + 1.0)) + 2.0 - floor(year / 100.0) + floor(floor(year / 100.0) / 4.0) + day - 1524.5;
        double JD = dayFraction + day2;
        return JD;
    }


    private double GetLST(SharedViewModel viewModel, int hours_in_future) {
        // calculations for Local Sidereal Time(LST), needed for the Hour Angle(HA) of an object:
        double J2000 = 2451543.5;

//Calculations of current Julian day:
        LocalDateTime UTCtime = LocalDateTime.now(ZoneId.of("GMT"));

        int year = UTCtime.getYear();
        int month = UTCtime.getMonthValue();
        int day = UTCtime.getDayOfMonth();
        int hours = UTCtime.getHour() + hours_in_future;
        int min = UTCtime.getMinute();
        int sec = UTCtime.getSecond();

        //double JD = julianday(2022, 7, 24, 3, 39, 0);


        //UT is the Universal Time at this moment
        double UT = hours + min / 60.0 + sec / 3600.0;


//local_time is the local to the user time

        //LocalDateTime ltime =  LocalDateTime.of(2021,1,7,6,30,15);
        // LocalTime ltime = LocalTime.now();


//Conversion of Universal Time (UT) to Greenwich mean sidereal time(GST)
        double S = julianday(year, month, day, 0, 0, 0) - J2000;

        double T = S / 36525.0;

        double T0 = 6.697374558 + 2400.051336 * T + (0.000025862 * T * T);
        T0 = T0 % 24.0;
        UT = UT * 1.002737909;
        double GST_decimal = (T0 + UT) % 24.0;
        double GST = GST_decimal;


        //latidute of the user .should be defined at location.
        double latitude_degrees = viewModel.getLatitute().getValue();
        // longitude of the user .should be defined at location.
        double longitude_degrees = viewModel.getLongitude().getValue();
//Convert longitude difference in degrees to
        //difference in time by dividing by 15.
        double longitude_in_time = longitude_degrees / 15.0;

        /*calculation of Local Sidereal Time (LST)*/

        double GST_minus_longitude = GST_decimal + longitude_in_time;
        if (longitude_in_time > 24) {
            GST_minus_longitude = GST_minus_longitude - 24;
        }

        if (longitude_in_time < 0) {
            GST_minus_longitude = GST_minus_longitude + 24;
        }

        double LST_decimal = GST_minus_longitude;


        return LST_decimal;

    }

    double[] calculate_preccesion(double RA, double DEC) {

        RA = RA / 15;
        LocalDateTime UTCtime = LocalDateTime.now(ZoneId.of("GMT"));
        //LocalDateTime UTCtime = LocalDateTime.of(2021,1,7,4,30,15);
        Log.e(TAG, "UTCtime2: " + UTCtime);
        // LocalDateTime UTCtime = LocalDateTime.now(ZoneId.of("UTC"));
        //double decimal_time2=UTCtime.getHour()+UTCtime.getMinute()/60.0+UTCtime.getSecond()/3600.0;
        int year = UTCtime.getYear();
        int month = UTCtime.getMonthValue();
        int day = UTCtime.getDayOfMonth();
        int hours = UTCtime.getHour();
        int min = UTCtime.getMinute();
        int sec = UTCtime.getSecond();
        double JD = julianday(year, month, day, hours, min, sec);


        Log.e(TAG, "Current Julian Day: " + JD);

        //UT is the Universal Time at this moment
        double UT = hours + min / 60.0 + sec / 3600.0;


        double JD1 = 2451545.5;
        double T = (JD - 2451545) / 36525;
        double zeta = 0.6406161 * T + 0.0000839 * T * T + 0.0000050 * T * T * T;
        double z = 0.6406161 * T + 0.0003041 * T * T + 0.0000051 * T * T * T;
        double theta = 0.5567530 * T - 0.0001184 * T * T - 0.0000116 * T * T * T;
        //convert to radians
        zeta = zeta * PI / 180;
        z = z * PI / 180;
        theta = theta * PI / 180;

        //matrix P_t converts the coordinates from epoch 1(epoch 1=date the database was created) to epoch0(J2000).

        double[][] P_t_array = {
                {cos(zeta) * cos(theta) * cos(z) - sin(zeta) * sin(z), cos(zeta) * cos(theta) * sin(z) + sin(zeta) * cos(z), cos(zeta) * sin(theta)},
                {-sin(zeta) * cos(theta) * cos(z) - cos(zeta) * sin(z), -sin(zeta) * cos(theta) * sin(z) + cos(zeta) * cos(z), -sin(zeta) * sin(theta)},
                {-sin(theta) * cos(z), -sin(theta) * sin(z), cos(theta)},
        };


        // convert RA from decimal hours to radian degrees
        double RA_rad = RA * PI / 180;
        double a1 = RA_rad * 15;
        //convert DEC from decimal degrees to radian degrees(already in degrees so no need to multiply*15)
        double DEC_rad = DEC * PI / 180;
        double d1 = DEC_rad;
//vector v corresponds to the coordinates at epoch 1, a1(RA) and d1(DEC)
        double[] v_array = {cos(a1) * cos(d1), sin(a1) * cos(d1), sin(d1)};

        //vectol s corresponds to the coordinates at epoch 0(J2000)
        //double s=P_t * v;
        Matrix P_t = new Matrix(P_t_array);
        //  Log.e(TAG, "P_t :\n " + strung(P_t));
        Matrix v = new Matrix(v_array, 1);

        v = v.transpose();
        Matrix w = P_t.times(v);

        //  Log.e(TAG, "w is :\n " + strung(w));

        double a2 = atan2(w.get(1, 0), w.get(0, 0));
        Log.e(TAG, "a2 is :\n " + a2);
        double d2 = asin(w.get(2, 0));

        //convert to degrees
        a2 = a2 * 180 / PI;
        d2 = d2 * 180 / PI;
        a2 = mod(a2, 360);




   /* HA=LST-RA. Calculation of the Hour Angle(HA) of a target object, based on its Right Ascension (RA) coordinate and the
    Local Sidereal Time (LST).*/
        //  double RA_difference=a2-RA;
        RA = a2;
        DEC = d2;


        return new double[]{RA, DEC};


    }

    private double mod(double number, int divider) {
        //modulo for positive and negative numbers:
        double modulo = 0;
        if (number > 0) {
            modulo = number % divider;
        }
        if (number < 0) {
            modulo = (number % divider + divider) % divider;
        }
        return modulo;
    }

}
