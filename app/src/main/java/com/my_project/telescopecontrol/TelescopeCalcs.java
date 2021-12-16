package com.my_project.telescopecontrol;

import static android.content.ContentValues.TAG;

import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static java.lang.Math.*;

import Jama.Matrix;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;


public class TelescopeCalcs {
    int DEC_microstep_mode = 4;
    double RA_with_precession;
    int RA_microstep_mode = 4;
    boolean alignment_done = false;
    CoordinatesTransformations coordsTransformations_object = new CoordinatesTransformations();
    double latitude_degrees = 0;
    double longitude_degrees = 0;


    public void Goto(double RA, double DEC, SharedViewModel viewModel) {
        int steps_RA;
        int steps_DEC;
       // SharedViewModel sharedViewModel = viewModel;
        this.alignment_done = viewModel.get_alignment_done().getValue();
        int RA_gear_teeth = viewModel.getRA_gear_teeth().getValue();
        int DEC_gear_teeth = viewModel.getDEC_gear_teeth().getValue();
        double RA_motor_ratio = viewModel.getRA_mount_pulley().getValue() / (double) viewModel.getRA_motor_pulley().getValue();
        double DEC_motor_ratio = viewModel.getDEC_mount_pulley().getValue() / (double) viewModel.getDEC_motor_pulley().getValue();
        int RA_motor_steps = viewModel.getRA_motor_steps().getValue();
        int DEC_motor_steps = viewModel.getDEC_motor_steps().getValue();

        Log.e(TAG, "RA_gear_teeth: " + RA_gear_teeth);
        Log.e(TAG, "DEC_gear_teeth: " + DEC_gear_teeth);
        Log.e(TAG, "RA_motor_ratio: " + RA_motor_ratio);
        Log.e(TAG, "DEC_motor_ratio: " + DEC_motor_ratio);
        Log.e(TAG, "RA_motor_steps: " + RA_motor_steps);
        Log.e(TAG, "DEC_motor_steps: " + DEC_motor_steps);


        //micromicrosteps needed for a full 360 rotation of the RA MOUNT's axis:
        double RA_micro_360 = RA_gear_teeth * RA_motor_ratio * RA_motor_steps * RA_microstep_mode;

        //microsteps needed for a full 360 rotation of the DEC MOUNT's axis:
        double DEC_micro_360 = DEC_gear_teeth * DEC_motor_ratio * DEC_motor_steps * DEC_microstep_mode;

        //microsteps needed for ONE degree rotation  of the RA MOUNT's axis:
        double RA_micro_1 = RA_micro_360 / 360.0;

        // %microsteps needed for ONE degree rotation  of the DEC MOUNT's axis:
        double DEC_micro_1 = DEC_micro_360 / 360.0;

        double[] coords_with_precession = calculate_coords_with_precession(RA, DEC, viewModel);
        double HA_decimal = coords_with_precession[0];

        DEC = coords_with_precession[1];

        viewModel.setRA_decimal(this.RA_with_precession);
        viewModel.setDEC_decimal(DEC);

        double HA_degrees = HA_decimal * 15;
        double DEC_degrees = DEC;

        if (viewModel.get_alignment_ongoing().getValue() && HA_degrees >= 180 && HA_degrees <= 360) {


            HA_degrees = HA_degrees + viewModel.get_ra_initial_offset_east().getValue() / RA_micro_1;
            DEC_degrees = DEC_degrees + viewModel.get_dec_initial_offset_east().getValue() / DEC_micro_1;

            Log.e(TAG, "alignment started , using East ra offset : " + viewModel.get_ra_initial_offset_east().getValue() + " dec offset: " + viewModel.get_dec_initial_offset_east().getValue() + "\n");
        }
        if (viewModel.get_alignment_ongoing().getValue() && HA_degrees > 0 && HA_degrees < 180) {
            HA_degrees = HA_degrees + viewModel.get_ra_initial_offset_west().getValue() / RA_micro_1;
            DEC_degrees = DEC_degrees + viewModel.get_dec_initial_offset_west().getValue() / DEC_micro_1;
            Log.e(TAG, "alignment started , using West ra offset : " + viewModel.get_ra_initial_offset_west().getValue() + " dec offset: " + viewModel.get_dec_initial_offset_west().getValue() + "\n");
        }



        if (alignment_done) {
            if (HA_degrees < 0) {
                HA_degrees = HA_degrees + 360;
            }

            LocalTime ltime = LocalTime.now();
            double local_time = ((double) ltime.getHour()) + (((double) ltime.getMinute()) / 60) + (((double) ltime.getSecond()) / 3600);
            double t_dif = local_time - viewModel.getInitial_time().getValue();

            if (t_dif < 0) {
                t_dif = t_dif + 24;
            }

            double days_since_last_alignment = 0;
            if (viewModel.getAlignment_julianday() != null) {
                LocalDateTime localtimenow = LocalDateTime.now();


                days_since_last_alignment = floor(julianday(localtimenow.getYear(), localtimenow.getMonthValue(), localtimenow.getDayOfMonth(), localtimenow.getHour(), localtimenow.getMinute(), localtimenow.getSecond()) - viewModel.getAlignment_julianday().getValue());
            }

            if (days_since_last_alignment < 0) {
                days_since_last_alignment = 0;
            }
            t_dif = t_dif + floor(days_since_last_alignment * 24);
            if (t_dif < 0) {
                t_dif = t_dif + 24;
            }

            double[] coord_vector = {((this.RA_with_precession * 15) * PI) / 180, (DEC * PI) / 180, ((t_dif * 15) * PI) / 180};

            Matrix transformationmatrix_east = viewModel.getTransformation_matrix_east().getValue();
            Matrix transformationmatrix_west = viewModel.getTransformation_matrix_west().getValue();

            if (viewModel.getAlignmentpoints().getValue() != null) {
                double[] new_coords = {0.0, 0.0};
                if (viewModel.get_use_only_basic_transformation().getValue()) {

                    if (viewModel.getSide_of_meridian().getValue().equals("east")) {
                        new_coords = coordsTransformations_object.get_transformed_coordinates(transformationmatrix_east, coord_vector);
                    }
                    if (viewModel.getSide_of_meridian().getValue().equals("west")) {
                        new_coords = coordsTransformations_object.get_transformed_coordinates(transformationmatrix_west, coord_vector);
                    }

                    HA_degrees = new_coords[0];
                    DEC_degrees = new_coords[1];

                    Log.e(TAG, "----->new coords using initial alignment only .\n HA :... " + HA_degrees + " dec :" + DEC_degrees);

                }


                if (!viewModel.get_use_only_basic_transformation().getValue()) {


                    int alignment_points_num = viewModel.getAlignmentpoints().getValue().size();
                    double[] distances = new double[alignment_points_num];


                    for (int i = 0; i < alignment_points_num; i++) {

                        double saved_point_ha = (viewModel.getAlignmentpoints().getValue().get(i)).get_corrected_ha();
                        double saved_point_dec = (viewModel.getAlignmentpoints().getValue().get(i)).get_non_transformed_dec();


                        distances[i] = acos((sin((DEC * PI) / 180) * sin(saved_point_dec)) + (cos((DEC * PI) / 180) * cos(saved_point_dec) * cos(((HA_degrees * PI) / 180) + saved_point_ha)));

                        if (!viewModel.getAlignmentpoints().getValue().get(i).get_side_of_meridian().equals(viewModel.getSide_of_meridian().getValue())) {
                            distances[i] = distances[i] + 1000;
                        }

                    }

                    OptionalDouble minimum = DoubleStream.of(distances).min();
                    int index_of_nearest = (DoubleStream.of(distances).boxed().collect(Collectors.toList())).indexOf(minimum.getAsDouble());
                    viewModel.set_current_nearest_star_index(index_of_nearest);

                    double[] new_coords2 = coordsTransformations_object.get_transformed_coordinates(viewModel.getAlignmentpoints().getValue().get(index_of_nearest).associated_transformation_matrix, coord_vector);

                    HA_degrees = new_coords2[0];
                    DEC_degrees = new_coords2[1];

                    Log.e(TAG, "----->new coords using associated transformation matrix of the nearest point  .\n HA :... " + HA_degrees + " dec :" + DEC_degrees);
                    Log.e(TAG, decimal_to_dms(HA_degrees/15,"ha"));
                    Log.e(TAG, decimal_to_dms(DEC_degrees,"dec"));

                }

            }

        }


        double h = (sin((this.latitude_degrees * PI) / 180) * sin((DEC * PI) / 180)) + (cos((this.latitude_degrees * PI) / 180) * cos((DEC * PI) / 180) * cos((HA_degrees * PI) / 180));
        double altitude = (asin(h) * 180) / PI;
        Log.e(TAG, "Altitude :\n " + altitude);

       int RA_steps_at_zero_altitude= get_RA_steps_at_horizon(DEC,viewModel.getLatitute().getValue(),viewModel.getRA_micro_1().getValue());
       viewModel.set_ra_steps_at_horizon(RA_steps_at_zero_altitude);

        if (altitude < 0) {
            MainActivity.handler.obtainMessage(MESSAGE_WRITE, "\nObject is not visible as it is below the horizon !\n").sendToTarget();
            viewModel.set_object_invisible(true);
        } else if (altitude >= 0) {
            viewModel.set_object_invisible(false);

            double HA_from_home = 0;
            double DEC_from_home = 0;

            if (HA_degrees >= 0 && HA_degrees < 180) {
                Log.e(TAG, "target is WEST of the local meridian at this moment\n");
                viewModel.setSide_of_meridian("west");

                if (viewModel.getLatitute().getValue() >= 0) {
                    HA_from_home = 90 - HA_degrees;
                    DEC_from_home = (-90) + DEC_degrees;

                } else if (viewModel.getLatitute().getValue() < 0) {
                    HA_from_home = (-90) + HA_degrees;
                    DEC_from_home = 90 + DEC_degrees;
                }

            }

            if (HA_degrees >= 180 && HA_degrees <= 360) {
                Log.e(TAG, "target is EAST of the local meridian at this moment\n");
                viewModel.setSide_of_meridian("east");

                if (viewModel.getLatitute().getValue() >= 0) {
                    HA_from_home = 270 - HA_degrees;
                    DEC_from_home = 90 - DEC_degrees;
                }
                if (viewModel.getLatitute().getValue() < 0) {

                    HA_from_home = (-270) + HA_degrees;
                    DEC_from_home = (-90) - DEC_degrees;
                }

            }

            int [] backlash_fixes=get_nearest_backlash_fixes(HA_degrees,DEC_degrees,viewModel);
            viewModel.set_ra_backlash_fix( backlash_fixes[0]);
            viewModel.set_dec_backlash_fix( backlash_fixes[1]);

        /*calculation of how many steps the stepper motors should move
        from the home position to the target with its HA and DEC at that time: */

            steps_RA = (int) (RA_micro_1 * HA_from_home);
            steps_DEC = (int) (DEC_micro_1 * DEC_from_home);


            Log.e(TAG, "DEC_micro_1 is :\n " + DEC_micro_1);
            Log.e(TAG, "RA_micro_1 is :\n " + RA_micro_1);
            Log.e(TAG, "HA_from_home is :\n " + HA_from_home);
            Log.e(TAG, "DEC_from_home is :\n " + DEC_from_home);


            String is_second_goto_str = "";
            if (viewModel.get_is_first_goto().getValue()) {
                is_second_goto_str = "0";
            } else if (!viewModel.get_is_first_goto().getValue()) {
                is_second_goto_str = "1";
            }

            String command_str = "<move:RA:" + steps_RA + ";DEC:" + steps_DEC + ":" + is_second_goto_str + ":" +  ":"+backlash_fixes[0] +":"+backlash_fixes[1]+";>\n";

            Log.e(TAG, command_str);
            MainActivity.handler.obtainMessage(MESSAGE_WRITE, command_str).sendToTarget();
            viewModel.setRA_micro_1(RA_micro_1);
            viewModel.setDEC_micro_1(DEC_micro_1);
            viewModel.setDEC_decimal_transformed(DEC_degrees);
            viewModel.setHA_degrees(HA_degrees);
            Log.e(TAG, "ra_backlash_fix : " + backlash_fixes[0]+ "   dec_backlash_fix : "+ backlash_fixes[1]);


        }
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

   /* private double[] to_alt_az_coords(double ha, double dec) {

        double azimuth = (atan2(sin(ha), (cos(ha) * sin((this.latitude_degrees * PI) / 180)) - (tan(dec) * cos((this.latitude_degrees * PI) / 180))) * 180 / PI) + 180;
        double altitude = asin((sin((this.latitude_degrees * PI) / 180) * sin(dec)) + ((cos((this.latitude_degrees * PI) / 180) * cos(dec)) * cos(ha))) * 180 / PI;
        double[] alt_az_coords = {azimuth, altitude};
        return alt_az_coords;
    }*/

    public double[] calculate_coords_with_precession(double RA, double DEC, SharedViewModel viewModel) {


        // calculations for Local Sidereal Time(LST), needed for the Hour Angle(HA) of an object:
        double J2000 = 2451545.0;
        //Calculations of current Julian day:

        LocalDateTime UTCtime = LocalDateTime.now(ZoneId.of("GMT"));
        Log.e(TAG, "UTCtime: " + UTCtime);
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
        //local_time is the local to the user time


        // LocalTime ltime = LocalTime.now();
        //double hour = ((double) ltime.getHour()) + (((double) ltime.getMinute()) / 60) + (((double) ltime.getSecond()) / 3600);
        //Conversion of Universal Time (UT) to Greenwich mean sidereal time(GST)
        double S = julianday(year, month, day, 0, 0, 0) - J2000;

        double T = S / 36525.0;

        double T0 = 6.697374558 + 2400.051336 * T + (0.000025862 * T * T);
        T0 = T0 % 24.0;
        UT = UT * 1.002737909;
        double GST_decimal = (T0 + UT) % 24.0;

        //latidute of the user .should be defined at location.
        latitude_degrees = viewModel.getLatitute().getValue();
        // longitude of the user .should be defined at location.
        longitude_degrees = viewModel.getLongitude().getValue();
//Convert longitude difference in degrees to
        //difference in time by dividing by 15.
        double longitude_in_time = longitude_degrees / 15.0;

        /*calculation of Local Sidereal Time (LST)*/

        double GST_plus_longitude = GST_decimal + longitude_in_time;
        if (GST_plus_longitude > 24) {
            GST_plus_longitude = GST_plus_longitude - 24;
        }

        if (GST_plus_longitude < 0) {
            GST_plus_longitude = GST_plus_longitude + 24;
        }

        double LST_decimal = GST_plus_longitude;

        /*TODO next line only for testing :*/
//LST_decimal=22.513337;

        Log.e(TAG, "LST is : " + LST_decimal);

         /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%        PART 3            %%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%%%%%%%%%%%%%%%%%%%%%% Calculation of precession  of the RA and DEC coordinates%%%%%%%%%%%%%%
    %%%%%% using the rigorous method%%%%%%%%%%%%%%*/
        double JD1 = 2451545.5;
        T = (JD1 - 2451545) / 36525;
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
        Matrix v = new Matrix(v_array, 1);
        v = v.transpose();
        Matrix s = P_t.times(v);
        // Log.e(TAG, "s is :\n " + strung(s));


        //Second part of the precession calculation :J0(Julian day 0) to J2(current Julian day)
        //P = [CX*CT*CZ-SX*SZ -SX*CT*CZ-CX*SZ -ST*CZ; CX*CT*SZ+SX*CZ -SX*CT*SZ+CX*CZ -ST*SZ;CX*ST -SX*ST CT]
        // JD=2459223.52527914;
        double JD2 = JD;
        T = (JD2 - 2451545) / 36525;
        zeta = 0.6406161 * T + 0.0000839 * T * T + 0.0000050 * T * T * T;
        z = 0.6406161 * T + 0.0003041 * T * T + 0.0000051 * T * T * T;
        theta = 0.5567530 * T - 0.0001184 * T * T - 0.0000116 * T * T * T;
        //convert to radians
        zeta = zeta * PI / 180;
        z = z * PI / 180;
        theta = theta * PI / 180;

        double[][] P_array = {
                {cos(zeta) * cos(theta) * cos(z) - sin(zeta) * sin(z), -sin(zeta) * cos(theta) * cos(z) - cos(zeta) * sin(z), -sin(theta) * cos(z)},
                {cos(zeta) * cos(theta) * sin(z) + sin(zeta) * cos(z), -sin(zeta) * cos(theta) * sin(z) + cos(zeta) * cos(z), -sin(theta) * sin(z)},
                {cos(zeta) * sin(theta), -sin(zeta) * sin(theta), cos(theta)},
        };
        Matrix P = new Matrix(P_array);
        Matrix w = P.times(s);
        //  Log.e(TAG, "w is :\n " + strung(w));

        double a2 = atan2(w.get(1, 0), w.get(0, 0));
        Log.e(TAG, "a2 is :\n " + a2);
        double d2 = asin(w.get(2, 0));

        //convert to degrees
        a2 = a2 * 180 / PI;
        d2 = d2 * 180 / PI;
        if (w.get(1, 0) < 0 && w.get(0, 0) > 0) {
            a2 = a2 + 180;
        }


        if (w.get(1, 0) < 0 && w.get(0, 0) < 0) {
            a2 = a2 + 360;
        }


        if (w.get(0, 0) > 0 && (w.get(1, 0) < 0)) {
            a2 = a2 + 180;
        }
        //convert a2 from degrees to hours
        a2 = a2 / 15;

        Log.e(TAG, "a2 is :\n " + a2);
        Log.e(TAG, "d2 is :\n " + d2);

        this.RA_with_precession = a2;
        viewModel.setRA_decimal(this.RA_with_precession);
        DEC = d2;


   /* HA=LST-RA. Calculation of the Hour Angle(HA) of a target object, based on its Right Ascension (RA) coordinate and the
    Local Sidereal Time (LST).*/

        double HA_decimal = LST_decimal - RA_with_precession;
        if (HA_decimal < 0) {

            HA_decimal = HA_decimal + 24;
        }


        Log.e(TAG, "HA (in hours) and DEC after pressesion is calculated :\n " + HA_decimal + " decimal hours\n" + DEC + " decimal degrees\n");
        Log.e(TAG, "HA (in degrees) and DEC after pressesion is calculated :\n " + HA_decimal * 15 + " decimal hours\n" + DEC + " decimal degrees\n");
        Log.e(TAG, decimal_to_dms(HA_decimal,"ha"));
        Log.e(TAG, decimal_to_dms(DEC,"dec"));



        double[] coords_with_precession = {HA_decimal, DEC};
        if (HA_decimal * 15 > 180 && HA_decimal * 15 < 360) {
            viewModel.setSide_of_meridian("east");
        }
        if (HA_decimal * 15 > 0 && HA_decimal * 15 < 180) {
            viewModel.setSide_of_meridian("west");
        }
        return coords_with_precession;
    }


    private int[] get_nearest_backlash_fixes(double HA_degrees,double DEC, SharedViewModel viewModel) {
        int[] nearest_backlash_fixes={0,0};
        if (viewModel.getBacklashfixes().getValue() != null) {

            int backlash_points_num = viewModel.getBacklashfixes().getValue().size();
            if (backlash_points_num > 0) {
                double[] distances = new double[backlash_points_num];


                for (int i = 0; i < backlash_points_num; i++) {

                    double saved_point_ha = viewModel.getBacklashfixes().getValue().get(i).get_ha()*PI/180;
                    double saved_point_dec = viewModel.getBacklashfixes().getValue().get(i).get_dec()*PI/180;
                    distances[i] = acos(sin((DEC * PI) / 180) * sin(saved_point_dec) + cos((DEC * PI) / 180) * cos(saved_point_dec) * cos((HA_degrees * PI / 180) - saved_point_ha));
                    distances[i]=toDegrees(distances[i]);
                    if (!viewModel.getBacklashfixes().getValue().get(i).get_side_of_meridian().equals(viewModel.getSide_of_meridian().getValue())) {
                        distances[i] = distances[i] + 1000;
                    }


                }

                OptionalDouble minimum = DoubleStream.of(distances).min();
                int index_of_nearest = (DoubleStream.of(distances).boxed().collect(Collectors.toList())).indexOf(minimum.getAsDouble());
                nearest_backlash_fixes[0] = viewModel.getBacklashfixes().getValue().get(index_of_nearest).get_ra_backlash_fix();
                nearest_backlash_fixes[1] = viewModel.getBacklashfixes().getValue().get(index_of_nearest).get_dec_backlash_fix();

            }
        }
        return nearest_backlash_fixes;
    }


    public String decimal_to_dms(double decimal_coord, String coord_id) {
        String dms_str = "";
        String integer_part_str = "";


        Integer degrees_int_part = (int) decimal_coord;  // Truncate the decimals
        double decimal_part = abs((decimal_coord - degrees_int_part) * 60);
        Integer mm = (int) decimal_part;
        Double ss = (decimal_part - mm) * 60;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat dfmm = new DecimalFormat("00", symbols);

        integer_part_str = degrees_int_part.toString();
        if (decimal_coord < 0 && degrees_int_part == 0 && mm == 0.0) {
            integer_part_str = "- " + integer_part_str;
        }
        if (decimal_coord < 0 && degrees_int_part == 0 && !integer_part_str.contains("-")) {
            integer_part_str = "- " + integer_part_str;
        }
        String mm_str = dfmm.format(mm);
        DecimalFormat dfss = new DecimalFormat("00.0", symbols);
        String ss_str = dfss.format(ss);
        // String ss_str = String.format(Locale.ENGLISH, "%.1f", ss);

        if (coord_id.equals("ra")) {
            dms_str = "RA   :  " + integer_part_str + "h " + mm_str + "m " + ss_str + "s";

        }
        if (coord_id.equals("dec")) {
            dms_str = "DEC :  " + integer_part_str + "° " + mm_str + "' " + ss_str + "\"";
        }
        if (coord_id.equals("ha")) {
            dms_str = "HA   :  " + integer_part_str + "h " + mm_str + "m " + ss_str + "s";

        }

        return dms_str;
    }

    private int get_RA_steps_at_horizon(double dec,double latitude_degrees,double RA_micro_1){
        double HA_from_home=0.0;
        double cosHA_0=-tan(toRadians(latitude_degrees)) * tan(toRadians(dec));
        double  HA_at_zero_altitude=acos(cosHA_0);
        HA_at_zero_altitude=toDegrees(HA_at_zero_altitude)/15;
        if (Double.isNaN(HA_at_zero_altitude)) {
            HA_at_zero_altitude = 11.999; // Nan is produced for the stars near polaris that do not set

        }

        Log.e(TAG," HA_at_zero_altitude : "+ HA_at_zero_altitude+"\n");
        HA_at_zero_altitude=HA_at_zero_altitude*15;
        if (HA_at_zero_altitude >= 0 && HA_at_zero_altitude < 180) {
            if (latitude_degrees >= 0) { HA_from_home = 90 - HA_at_zero_altitude; }
            if (latitude_degrees < 0) {HA_from_home = (-90) + HA_at_zero_altitude; }
        }

        if (HA_at_zero_altitude >= 180 && HA_at_zero_altitude <= 360) {
            if (latitude_degrees >= 0) {HA_from_home = 270 - HA_at_zero_altitude; }
            if (latitude_degrees < 0) {HA_from_home = (-270) + HA_at_zero_altitude; }
        }
// add 2 degrees (2/15=0.1333 hours ) so the tracking will continue for (60*(2/15))=8 minutes after the limit has reached .
        if (latitude_degrees >= 0) {HA_from_home=HA_from_home-0.1333 ;}
        if (latitude_degrees < 0) {HA_from_home=HA_from_home + 0.1333 ;}
       int RA_STEPS_at_zero_altitude = (int) (RA_micro_1 * HA_from_home);
       // int RA_STEPS_at_zero_altitude = (int) (RA_micro_1 *  HA_at_zero_altitude );
        Log.e(TAG," RA_STEPS_at_zero_altitude : "+ RA_STEPS_at_zero_altitude+"\n");
        return RA_STEPS_at_zero_altitude;

    }




}
