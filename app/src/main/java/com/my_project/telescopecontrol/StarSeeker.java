package com.my_project.telescopecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

import static java.lang.Math.PI;

public class StarSeeker {

    double[][] brightest_stars_coords_east;
    double[][] brightest_stars_coords_west;
    int brightest_stars_counter_east;
    int brightest_stars_counter_west;
    private Context context;
    SharedPreferences.Editor editor;
    // String jsonString_names = "";
    SharedPreferences sharedPreferences;
    private ArrayList<star> starArrayList;
    private ArrayList<star> starArrayList_east;
    ArrayList<star> starArrayList_nearest = new ArrayList<>();

    private ArrayList<star> starArrayList_west;
    TelescopeCalcs tel_calcs = new TelescopeCalcs();
    private SharedViewModel viewModel;

    public StarSeeker(Context activityContext, SharedViewModel sharedviewModel) {
        brightest_stars_coords_east = new double[50][2];
        brightest_stars_coords_west = new double[50][2];
        brightest_stars_counter_east = 0;
        brightest_stars_counter_west = 0;
        starArrayList_east = new ArrayList<>();
        starArrayList_west = new ArrayList<>();

        viewModel = sharedviewModel;
        context = activityContext;
        SharedPreferences sharedPreferences2 = activityContext.getSharedPreferences(MainActivity.SHARED_PREFS, 0);
        sharedPreferences = sharedPreferences2;
        editor = sharedPreferences2.edit();
    }

    public void find_brightest_stars() {
        String current_side_of_meridian = viewModel.getSide_of_meridian().getValue();
        if (viewModel.get_jsonString_names().getValue() == "") {
            InputStream is = context.getResources().openRawResource(R.raw.star_names);
            viewModel.set_jsonString_names(new Scanner(is).useDelimiter("\\A").next());
        }
        starArrayList = new Gson().fromJson(viewModel.get_jsonString_names().getValue(), new TypeToken<ArrayList<star>>() {
        }.getType());
        brightest_stars_counter_east = 0;
        brightest_stars_counter_west = 0;
        starArrayList_west.clear();
        starArrayList_east.clear();
        for (int i = 0; i < starArrayList.size(); i++) {
            if (starArrayList.get(i).getMmag() < 2.55d) {
                double[] coords_with_precession = tel_calcs.calculate_coords_with_precession(starArrayList.get(i).getRaj2000(), starArrayList.get(i).getDecj2000(), viewModel);
                double star_ha_degrees = coords_with_precession[0] * 15.0d;
                double star_dec_degrees = coords_with_precession[1];
                double latitude_degrees = viewModel.getLatitute().getValue().doubleValue();
                double altitude = (Math.asin((Math.sin((latitude_degrees * PI) / 180.0d) * Math.sin((star_dec_degrees * PI) / 180.0d)) + ((Math.cos((latitude_degrees * PI) / 180.0d) * Math.cos((star_dec_degrees * PI) / 180.0d)) * Math.cos((star_ha_degrees * PI) / 180.0d))) * 180.0d) / PI;
                if (star_ha_degrees > 0.0d && star_ha_degrees < 180.0d && altitude > 20.0d && altitude < 75.0d && !starArrayList.get(i).getName_ascii().equals("Polaris") && brightest_stars_counter_west < 50) {
                    starArrayList_west.add(starArrayList.get(i));

                    brightest_stars_coords_west[brightest_stars_counter_west][0] = star_ha_degrees;
                    brightest_stars_coords_west[brightest_stars_counter_west][1] = star_dec_degrees;
                    brightest_stars_counter_west = brightest_stars_counter_west + 1;
                }
                if (star_ha_degrees > 180.0d && star_ha_degrees < 360.0d && altitude > 20.0d && altitude < 75.0d && !starArrayList.get(i).getName_ascii().equals("Polaris") && brightest_stars_counter_east < 50) {
                    starArrayList_east.add(starArrayList.get(i));

                    brightest_stars_coords_east[brightest_stars_counter_east][0] = star_ha_degrees;
                    brightest_stars_coords_east[brightest_stars_counter_east][1] = star_dec_degrees;
                    brightest_stars_counter_east = brightest_stars_counter_east + 1;
                }
            }
        }
        viewModel.setSide_of_meridian(current_side_of_meridian);
    }

    public void find_best_alignment_stars_east() {
        double[][] best_coords_for_alignment = (double[][]) Array.newInstance(double.class, new int[]{3, 2});
        char c = 0;
        if(viewModel.getLatitute().getValue()>=0){
        best_coords_for_alignment[0][0] = 276.3d;
        best_coords_for_alignment[0][1] = 46.95d;
        best_coords_for_alignment[1][0] = 320.4d;
        best_coords_for_alignment[1][1] = -11.38d;
        best_coords_for_alignment[2][0] = 332.85d;
        best_coords_for_alignment[2][1] = 33.23d;}

        if(viewModel.getLatitute().getValue()< 0){
            best_coords_for_alignment[0][0] = 312.0d;
            best_coords_for_alignment[0][1] = -6.2d;
            best_coords_for_alignment[1][0] = 271.4d;
            best_coords_for_alignment[1][1] = -53.3d;
            best_coords_for_alignment[2][0] = 325.5d;
            best_coords_for_alignment[2][1] = -41.2d;}

        int j = 0;
        while (j < 3) {
            int min_index = 0;
            double min_distance = 1000.0d;
            double best_ha = best_coords_for_alignment[j][c];
            double best_dec = best_coords_for_alignment[j][1];
            for (int i = 0; i <= starArrayList_east.size(); i++) {

                double bright_star_ha = brightest_stars_coords_east[i][c];
                double bright_star_dec = brightest_stars_coords_east[i][1];
                if (!(bright_star_ha == 0.0d || bright_star_dec == 0.0d)) {
                    double distance = Math.acos((Math.sin((best_dec * PI) / 180.0d) * Math.sin((bright_star_dec * PI) / 180.0d)) + (Math.cos((best_dec * PI) / 180.0d) * Math.cos((bright_star_dec * PI) / 180.0d) * Math.cos(((best_ha - bright_star_ha) * PI) / 180.0d)));
                    if (distance < min_distance) {
                        min_distance = distance;
                        min_index = i;
                    }
                }
            }
            if (!starArrayList_east.get(min_index).getName_ascii().contains("❂ ")) {
                starArrayList_east.get(min_index).setName_ascii("❂   " + starArrayList_east.get(min_index).getName_ascii());
            }
            Log.e("ContentValues", "nearest index is  : \n" + min_index);
            Log.e("ContentValues", "nearest star is  : \n" + starArrayList_east.get(min_index).getName_ascii());
            Log.e("ContentValues", "distance  : \n" + ((180.0d * min_distance) / PI) + "\n");
            j++;
            c = 0;
        }
    }

    public void find_best_alignment_stars_west() {
        double[][] best_coords_for_alignment = (double[][]) Array.newInstance(double.class, new int[]{3, 2});
        if(viewModel.getLatitute().getValue()>=0){
        best_coords_for_alignment[0][0] = 75.0d;
        best_coords_for_alignment[0][1] = 40.5d;
        best_coords_for_alignment[1][0] = 35.0d;
        best_coords_for_alignment[1][1] = -14.5d;
        best_coords_for_alignment[2][0] = 18.15d;
        best_coords_for_alignment[2][1] = 34.66d;}

        if(viewModel.getLatitute().getValue()< 0){
            best_coords_for_alignment[0][0] = 48.0d;
            best_coords_for_alignment[0][1] = -3.2d;
            best_coords_for_alignment[1][0] = 90.0d;
            best_coords_for_alignment[1][1] = -50.3d;
            best_coords_for_alignment[2][0] = 33.5d;
            best_coords_for_alignment[2][1] = -39.2d;}

        int j = 0;
        while (j < 3) {
            int min_index = 0;
            double min_distance = 1000.0d;
            double best_ha = best_coords_for_alignment[j][0];
            double best_dec = best_coords_for_alignment[j][1];
            for (int i = 0; i <= starArrayList_west.size(); i++) {
                double bright_star_ha = brightest_stars_coords_west[i][0];
                double bright_star_dec = brightest_stars_coords_west[i][1];
                if (!(bright_star_ha == 0.0d || bright_star_dec == 0.0d)) {
                    double distance = Math.acos((Math.sin((best_dec * PI) / 180.0d) * Math.sin((bright_star_dec * PI) / 180.0d)) + (Math.cos((best_dec * PI) / 180.0d) * Math.cos((bright_star_dec * PI) / 180.0d) * Math.cos(((best_ha - bright_star_ha) * PI) / 180.0d)));
                    if (distance < min_distance) {
                        min_distance = distance;
                        min_index = i;
                    }
                }
            }
            if (!starArrayList_west.get(min_index).getName_ascii().contains("❂ ")) {
                starArrayList_west.get(min_index).setName_ascii("❂  " + starArrayList_west.get(min_index).getName_ascii());
            }
            Log.e("ContentValues", "nearest index is  : \n" + min_index);
            Log.e("ContentValues", "distance  : \n" + ((180.0d * min_distance) / PI) + "\n");
            j++;

        }
    }

    public ArrayList<star> get_eastern_stars() {
        find_best_alignment_stars_east();
        Collections.sort(starArrayList_east, new Comparator<star>() {
            public int compare(star o1, star o2) {
                return Double.valueOf(o1.getMmag()).compareTo(Double.valueOf(o2.getMmag()));
            }
        });
        return starArrayList_east;
    }

    public ArrayList<star> get_western_stars() {
        find_best_alignment_stars_west();
        Collections.sort(starArrayList_west, new Comparator<star>() {
            public int compare(star o1, star o2) {
                return Double.valueOf(o1.getMmag()).compareTo(Double.valueOf(o2.getMmag()));
            }
        });
        return starArrayList_west;
    }

    public ArrayList<star> get_nearest_star() {
        String current_side_of_meridian;
        Type listType;
        String current_side_of_meridian2 = viewModel.getSide_of_meridian().getValue();
        starArrayList_nearest.clear();
        if (viewModel.get_jsonString_names().getValue() == "") {
            InputStream is = context.getResources().openRawResource(R.raw.star_names);
            viewModel.set_jsonString_names(new Scanner(is).useDelimiter("\\A").next());
        }
        Type listType2 = new TypeToken<ArrayList<star>>() {
        }.getType();
        starArrayList = new Gson().fromJson(viewModel.get_jsonString_names().getValue(), listType2);
        double d = 15.0d;
        double target_ra = viewModel.getStar_object().getValue().getRaj2000() * 15.0d;
        double target_dec = viewModel.getStar_object().getValue().getDecj2000();
        tel_calcs.calculate_coords_with_precession(viewModel.getStar_object().getValue().getRaj2000(), viewModel.getStar_object().getValue().getDecj2000(), viewModel);
        String target_side_of_meridian = viewModel.getSide_of_meridian().getValue();
        int i = 1;
        while (i < viewModel.get_HipHashMap().getValue().size()) {
            if (viewModel.get_HipHashMap().getValue().get(String.valueOf(i)) == null) {
                current_side_of_meridian = current_side_of_meridian2;
                listType = listType2;
            } else if (viewModel.get_HipHashMap().getValue().get(String.valueOf(i)).getMmag() <= 3.91d) {
                double catalogue_ra = viewModel.get_HipHashMap().getValue().get(String.valueOf(i)).getRaj2000() * d;
                double catalogue_dec = viewModel.get_HipHashMap().getValue().get(String.valueOf(i)).getDecj2000();
                Double distance = Double.valueOf(Math.acos((Math.sin((target_dec * PI) / 180.0d) * Math.sin((catalogue_dec * PI) / 180.0d)) + (Math.cos((target_dec * PI) / 180.0d) * Math.cos((catalogue_dec * PI) / 180.0d) * Math.cos(((target_ra - catalogue_ra) * PI) / 180.0d))));
                if ((distance.doubleValue() * 180.0d) / PI < 1.0E-5d) {
                    distance = Double.valueOf(0.0d);
                }
                if ((distance.doubleValue() * 180.0d) / PI < 20.0d) {
                    listType = listType2;
                    current_side_of_meridian = current_side_of_meridian2;
                    tel_calcs.calculate_coords_with_precession(((hipObject) viewModel.get_HipHashMap().getValue().get(String.valueOf(i))).getRaj2000(), catalogue_dec, viewModel);
                    String catalogue_side_of_meridian = viewModel.getSide_of_meridian().getValue();
                    if (target_side_of_meridian.equals(catalogue_side_of_meridian)) {
                        ArrayList<star> arrayList = starArrayList_nearest;
                        String str = catalogue_side_of_meridian;
                        arrayList.add(new star("hip:" + i, "", "", "", "", "unavailable", "", 0, ((hipObject) viewModel.get_HipHashMap().getValue().get(String.valueOf(i))).getMmag(), "", "0", 0, catalogue_ra, catalogue_dec, "", ""));

                        ArrayList<star> arrayList2 = starArrayList_nearest;
                        arrayList2.get(arrayList2.size() + -1).setDistance_from_nearest_selected(Double.valueOf((distance.doubleValue() * 180.0d) / PI));
                    }
                } else {
                    current_side_of_meridian = current_side_of_meridian2;
                    listType = listType2;
                }
            } else {
                current_side_of_meridian = current_side_of_meridian2;
                listType = listType2;
            }
            i++;
            listType2 = listType;
            current_side_of_meridian2 = current_side_of_meridian;
            d = 15.0d;
        }
        String current_side_of_meridian3 = current_side_of_meridian2;

        for (int j = 0; j < starArrayList.size(); j++) {
            if (starArrayList.get(j).getHip() != null) {
                for (int z = 0; z < starArrayList_nearest.size(); z++) {
                    if (("hip:" + starArrayList.get(j).getHip()).equals(starArrayList_nearest.get(z).getName_ascii())) {
                        Double distance2 = Double.valueOf(Math.acos((Math.sin((target_dec * PI) / 180.0d) * Math.sin((starArrayList.get(j).getDecj2000() * PI) / 180.0d)) + (Math.cos((target_dec * PI) / 180.0d) * Math.cos((starArrayList.get(j).getDecj2000() * PI) / 180.0d) * Math.cos(((target_ra - (starArrayList.get(j).getRaj2000() * 15.0d)) * PI) / 180.0d))));
                        if (Double.isNaN(distance2.doubleValue())) {
                            distance2 = Double.valueOf(0.0d);
                        }
                        starArrayList.get(j).setDistance_from_nearest_selected(Double.valueOf((distance2.doubleValue() * 180.0d) / PI));
                        starArrayList_nearest.set(z, starArrayList.get(j));
                    }
                }
            }
        }
        Collections.sort(starArrayList_nearest, new Comparator<star>() {
            public int compare(star o1, star o2) {
                return o1.getDistance_from_nearest_Selected().compareTo(o2.getDistance_from_nearest_Selected());
            }
        });
        DecimalFormat df_distance = new DecimalFormat("00.00");
        DecimalFormat df_mag = new DecimalFormat("0.00");
        for (int i2 = 0; i2 < starArrayList_nearest.size(); i2++) {
            String distance_str = df_distance.format(starArrayList_nearest.get(i2).getDistance_from_nearest_Selected());
            starArrayList_nearest.get(i2).setName_ascii(starArrayList_nearest.get(i2).getName_ascii() + "\ndistance :      " + distance_str + "° \nmagnitude :    " + df_mag.format(starArrayList_nearest.get(i2).getMmag()));
        }
        viewModel.setSide_of_meridian(current_side_of_meridian3);
        return starArrayList_nearest;
    }

    private double mod(double number, int divider) {
        double modulo = 0.0d;
        if (number > 0.0d) {
            modulo = number % ((double) divider);
        }
        if (number < 0.0d) {
            return ((number % ((double) divider)) + ((double) divider)) % ((double) divider);
        }
        return modulo;
    }
}
