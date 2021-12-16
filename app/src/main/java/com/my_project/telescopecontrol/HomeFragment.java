package com.my_project.telescopecontrol;


import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.SHARED_PREFS;
import static java.lang.Math.abs;

public class HomeFragment extends Fragment {
    private Button button_GOTO;
    private MaterialButton button_improve_accuracy;
    private MaterialButton button_tracking;
    private HashMap<String, constellationObject> constellation_names;
    private double dec_to_goto;
    SharedPreferences.Editor editor;
    private InputStream is_con_names;
    private String jsonString_con_names = "";
    private ImageView logo_image;
    private TextView motors_status;
    private TextView text_developed_by;
    private double ra_to_goto;
    SharedPreferences sharedPreferences;
    SolarCalcs solar_ob = new SolarCalcs();
    private TelescopeCalcs tel = new TelescopeCalcs();
    private TextView text_con_name;
    private TextView text_constellation;
    private TextView text_coordinates;
    private TextView text_ra;
    private TextView text_dec;
    private TextView text_ha;
    private TextView text_magnitude;
    private TextView text_name;
    private TextView text_object_invisible;
    private TextView text_selected_object;
    private TextView text_welcome;
    private TextView text_welcome2;
    private SharedViewModel viewModel;
    private double current_HA = 0.0;
    Handler main_handler = new Handler();
    Thread newThread;
    private boolean keep_updating_ha = true;
    private double ra;
    private double dec;


    public void onActivityCreated(Bundle savedInstanceState) {
        star selected_star;
        super.onActivityCreated(savedInstanceState);
        if (viewModel.get_home_fragment_restore().getValue() && (!sharedPreferences.getString("selected_star", "").equals(".."))) {
            selected_star = new Gson().fromJson(sharedPreferences.getString("selected_star", ""), star.class);
            viewModel.setStar_object(selected_star);
            viewModel.setTracking_status(Boolean.valueOf(sharedPreferences.getBoolean("tracking_is_on", false)));
            viewModel.setMoving_status("motors stopped.");
        }
        if (!viewModel.get_home_fragment_restore().getValue()) {
            editor.putString("selected_star", "..");
            editor.apply();
        }
        viewModel.set_home_fragment_restore(false);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        BottomNavigationView bottomNav = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(VISIBLE);
        bottomNav.getMenu().getItem(0).setChecked(true);
        viewModel = (SharedViewModel) new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        final View v = inflater.inflate(R.layout.home_screen, container, false);
        if (jsonString_con_names.equals("")) {
            constructHashmap();
        }
        logo_image = v.findViewById(R.id.imageView);
        text_welcome = v.findViewById(R.id.text_welcome);
        text_welcome2 = v.findViewById(R.id.text_welcome2);
        text_selected_object = v.findViewById(R.id.text_selected);
        text_name = v.findViewById(R.id.text_name);
        text_ra = v.findViewById(R.id.text_RA);
        text_dec = v.findViewById(R.id.text_DEC);
        text_ha = v.findViewById(R.id.text_HA);
        text_magnitude = v.findViewById(R.id.text_MAG);
        text_constellation = v.findViewById(R.id.text_constellation);
        text_con_name = v.findViewById(R.id.text_constellation_name);
        text_coordinates = v.findViewById(R.id.text_coordinates);
        text_object_invisible = v.findViewById(R.id.text_object_invisible);
        motors_status = v.findViewById(R.id.text_moving_status);
        ProgressBar progressBar = v.findViewById(R.id.progressBar);
        progressBar.setVisibility(GONE);
        motors_status.setVisibility(GONE);
        button_GOTO = v.findViewById(R.id.button_GOTO);
        button_tracking = v.findViewById(R.id.button_tracking);
        button_improve_accuracy = v.findViewById(R.id.button_improve_accuracy);
        text_developed_by = v.findViewById(R.id.text_developed_by);

        if (viewModel.getTracking_status().getValue()) {
            button_tracking.setText("Tracking is Enabled");
            button_tracking.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0C7839")));
        }
        if (!viewModel.getTracking_status().getValue()) {
            button_tracking.setText("Tracking is Disabled");
            button_tracking.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2337A8")));
        }

        viewModel.getStar_object().observe(getViewLifecycleOwner(), new Observer<star>() {
            String con_name;
            // double dec;
            double mag;
            String name;
            //double ra;

            public void onChanged(star returned_star) {
                if (viewModel.get_alignment_done().getValue()) {
                    button_improve_accuracy.setVisibility(VISIBLE);
                }
                if (!viewModel.get_alignment_done().getValue()) {
                    button_improve_accuracy.setVisibility(GONE);
                }
                logo_image.setVisibility(GONE);
                text_developed_by.setVisibility(GONE);
                text_welcome.setVisibility(GONE);
                text_welcome2.setVisibility(GONE);
                text_selected_object.setVisibility(VISIBLE);
                text_name.setVisibility(VISIBLE);
                text_ra.setVisibility(VISIBLE);
                text_dec.setVisibility(VISIBLE);
                text_ha.setVisibility(VISIBLE);
                text_magnitude.setVisibility(VISIBLE);
                text_constellation.setVisibility(VISIBLE);
                text_con_name.setVisibility(VISIBLE);
                text_coordinates.setVisibility(VISIBLE);
                motors_status.setVisibility(VISIBLE);
                button_GOTO.setVisibility(VISIBLE);
                button_tracking.setVisibility(VISIBLE);
                editor.putString("selected_star", new Gson().toJson((Object) viewModel.getStar_object().getValue()));
                editor.apply();
                name = returned_star.getName_ascii();
                ra = returned_star.getRaj2000();
                dec = returned_star.getDecj2000();
                mag = returned_star.getMmag();
                con_name = returned_star.getCon();

                double[] precessed_ha_and_dec = tel.calculate_coords_with_precession(ra, dec, viewModel);
                current_HA = precessed_ha_and_dec[0];
                double precessed_dec = precessed_ha_and_dec[1];
                //  double test_Ra =viewModel.getRA_decimal().getValue();
                text_ha.setText(decimal_to_dms(current_HA, "ha"));
                //  if(timer_is_running)
                //  {tim.cancel();tim=null;}
                //  tim =new Timer();
                // tim.schedule(timerTask,0,1000);
                //refreshCurrentDateTime();

                if (newThread != null) {
                    newThread.interrupt();
                }
                if (newThread == null) {
                    newThread = new Thread(runnable_update_ha);
                    newThread.start();
                }


                Log.e(TAG, "star name in observer is  :" + name + " ra " + ra + " dec " + dec);
                if (name.equals("Moon") || name.equals("Mercury") || name.equals("Mars") || name.equals("Neptune") || name.equals("Venus") || name.equals("Jupiter") || name.equals("Saturn") || name.equals("Uranus") || name.equals("Pluto")) {
                    text_constellation.setVisibility(INVISIBLE);
                    text_magnitude.setVisibility(INVISIBLE);
                    text_con_name.setVisibility(INVISIBLE);

                }
                text_name.setText(returned_star.getName_ascii());
                NumberFormat formatter = new DecimalFormat("###.####");

                String mag_str = formatter.format(mag);

                text_magnitude.setText("Magnitude :  " + mag_str);
                if (mag_str.equals("1000")) {
                    text_magnitude.setText("Magnitude :  - ");
                }
                text_con_name.setText((constellation_names.get(con_name)).getname() + " / " + (constellation_names.get(con_name)).geten());
                text_ra.setText(decimal_to_dms(viewModel.getRA_decimal().getValue(), "ra"));
                text_dec.setText(decimal_to_dms(precessed_dec, "dec"));
                if (constellation_names.get(con_name).getname().equals("unavailable")) {
                    text_con_name.setText(" - ");
                }

                button_improve_accuracy.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                        MainActivity.handler.obtainMessage(11, -1, -1).sendToTarget();
                    }
                });

                button_GOTO.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                        progressBar.setVisibility(VISIBLE);
                        motors_status.setText("Moving to object ");
                        motors_status.setVisibility(VISIBLE);
                        button_tracking.setText("Tracking is Disabled");
                        viewModel.setTracking_status(false);
                        editor.putBoolean("tracking_is_on", false);
                        editor.apply();
                        button_tracking.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2337A8")));

                        if (text_ra != null && text_dec != null) {
                            if (name.equals("Mercury") || name.equals("Mars") || name.equals("Neptune") || name.equals("Venus") || name.equals("Jupiter") || name.equals("Saturn") || name.equals("Uranus") || name.equals("Pluto")) {
                                double[] Planet_coordinates = solar_ob.GetPlanetCoords(name, viewModel, 0);
                                ra = Planet_coordinates[0];
                                dec = Planet_coordinates[1];
                                //Toast.makeText(getContext(), "RA: " + ra + "\nDEC: " + dec, Toast.LENGTH_SHORT).show();
                                viewModel.getStar_object().getValue().setRaj2000(ra);
                                viewModel.getStar_object().getValue().setDecj2000(dec);
                                viewModel.setStar_object(viewModel.getStar_object().getValue());
                            }
                            if (name.equals("Moon")) {
                                double[] Moon_coordinates = solar_ob.GetMoonCoords(viewModel, 0);
                                ra = Moon_coordinates[0];
                                dec = Moon_coordinates[1];
                                Toast.makeText(getContext(), "Moon's position recalculated", Toast.LENGTH_SHORT).show();
                                viewModel.getStar_object().getValue().setRaj2000(ra);
                                viewModel.getStar_object().getValue().setDecj2000(dec);
                                viewModel.setStar_object(viewModel.getStar_object().getValue());
                            }
                            viewModel.set_first_goto(true);
                            ra_to_goto = ra;
                            dec_to_goto = dec;
                            viewModel.setRA_to_goto(ra_to_goto);
                            viewModel.setDEC_to_goto(dec_to_goto);
                            tel.Goto(ra_to_goto, dec_to_goto, viewModel);
                            if (viewModel.get_alignment_done().getValue() && viewModel.getAlignmentpoints().getValue() != null && !viewModel.get_use_only_basic_transformation().getValue()) {

                                Toast.makeText(getContext(), "nearest alignment star :\n " + viewModel.getAlignmentpoints().getValue().get(viewModel.get_current_nearest_star_index().getValue()), Toast.LENGTH_SHORT).show();
                            }
                            if (viewModel.get_object_invisible().getValue()) {
                                text_object_invisible.setVisibility(VISIBLE);
                                progressBar.setVisibility(GONE);
                                motors_status.setText("Move Cancelled. Object not visible.");
                            }
                        }
                    }
                });
                button_tracking.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (viewModel.getMoving_status().getValue().equals("motors stopped.")) {
                            progressBar.setVisibility(GONE);
                            motors_status.setVisibility(GONE);
                            v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);


                            if (button_tracking.getText().equals("Tracking is Disabled")) {
                                if (name.equals("Mercury") || name.equals("Mars") || name.equals("Neptune") || name.equals("Venus")
                                        || name.equals("Jupiter") || name.equals("Saturn") || name.equals("Uranus") || name.equals("Pluto")) {

                                    double[] tracking_rates = solar_ob.GetPlanetTrackingRates(name, viewModel);
                                    int ra_horizon_tracking_limit=viewModel.get_ra_steps_at_horizon().getValue();
                                    Toast.makeText(getActivity().getBaseContext(), "Tracking rates: ra : " + tracking_rates[0] + " dec : " + tracking_rates[1], Toast.LENGTH_SHORT).show();
                                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<track:" + tracking_rates[0] + ":" + tracking_rates[1] + ":" + ra_horizon_tracking_limit + ":>\n").sendToTarget();

                                }
                                if (name.equals("Moon")) {

                                    double[] tracking_rates_moon = solar_ob.GetMoonTrackingRates(viewModel);
                                    Toast.makeText(getContext(), "Tracking rates: ra : " + tracking_rates_moon[0] + " dec : " + tracking_rates_moon[1], Toast.LENGTH_SHORT).show();
                                    int ra_horizon_tracking_limit=viewModel.get_ra_steps_at_horizon().getValue();
                                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<track:" + tracking_rates_moon[0] + ":" + tracking_rates_moon[1] + ":" + ra_horizon_tracking_limit + ":>\n").sendToTarget();

                                }

                                if (!name.equals("Moon") && !name.equals("Mercury") && !name.equals("Mars") && !name.equals("Neptune") && !name.equals("Venus")
                                        && !name.equals("Jupiter") && !name.equals("Saturn") && !name.equals("Uranus") && !name.equals("Pluto")) {
                                    int ra_horizon_tracking_limit=viewModel.get_ra_steps_at_horizon().getValue();
                                    if(viewModel.getLatitute().getValue()<0){MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<track:" + viewModel.get_sidereal_rate().getValue() + ":" + 0 + ":" + ra_horizon_tracking_limit + ":>\n").sendToTarget();}
                                    if(viewModel.getLatitute().getValue()>=0){MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<track:" + viewModel.get_sidereal_rate().getValue()*(-1) + ":" + 0 + ":" + ra_horizon_tracking_limit + ":>\n").sendToTarget();}
                                    //MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<track:" + viewModel.get_sidereal_rate().getValue()*(-1) + ":" + 0 + ":" + ra_horizon_tracking_limit + ":>\n").sendToTarget();
                                }
                                button_tracking.setText("Tracking is Enabled");
                                viewModel.setTracking_status(true);
                                editor.putBoolean("tracking_is_on", true);
                                editor.apply();
                                button_tracking.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0C7839")));
                            } else if (button_tracking.getText().equals("Tracking is Enabled")) {
                                MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<stop:1:>\n").sendToTarget();
                                button_tracking.setText("Tracking is Disabled");
                                button_tracking.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2337A8")));
                                viewModel.setTracking_status(false);
                                editor.putBoolean("tracking_is_on", false);
                                editor.apply();
                            }
                        }
                    }
                });

                viewModel.getMoving_status().observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String received_status) {

                        if (received_status.equals("motors stopped.")) {
                            progressBar.setVisibility(GONE);
                            motors_status.setText("Move completed");}
                    }
                });

                viewModel.getTracking_status().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean tracking_status) {

                        if (!tracking_status) {
                            button_tracking.setText("Tracking is Disabled");
                            button_tracking.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2337A8")));
                        }
                        if (tracking_status) {
                            button_tracking.setText("Tracking is Enabled");
                            editor.putBoolean("tracking_is_on", true);
                            editor.apply();
                            button_tracking.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0C7839")));
                        }
                    }
                });

            }


        });
        return v;
    }

    double one_sec_to_hours = 1.0 / 3600;
    int moon_recalculate_counter = 0;
    int planet_recalculate_counter = 0;
    Runnable runnable_update_ha = new Runnable() {
        @Override
        public void run() {
            while (keep_updating_ha) {


                current_HA = mod((one_sec_to_hours + current_HA), 24);

                String ha_Str = decimal_to_dms(current_HA, "ha");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                main_handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (moon_recalculate_counter == 10) {
                            moon_recalculate_counter = 0;
                            if (viewModel.getStar_object().getValue().getName_ascii().equals("Moon")) {
                                double[] Moon_coordinates = solar_ob.GetMoonCoords(viewModel, 0);
                                ra = Moon_coordinates[0] / 15;
                                dec = Moon_coordinates[1];
                                double[] precessed_ha_and_dec = tel.calculate_coords_with_precession(ra, dec, viewModel);
                                current_HA = precessed_ha_and_dec[0];
                                double precessed_dec = precessed_ha_and_dec[1];
                                text_ra.setText(decimal_to_dms(viewModel.getRA_decimal().getValue(), "ra"));
                                text_dec.setText(decimal_to_dms(precessed_dec, "dec"));
                            }
                        }
                        if (planet_recalculate_counter == 30) {
                            planet_recalculate_counter = 0;
                            String name = viewModel.getStar_object().getValue().getName_ascii();
                            if (name.equals("Mercury") || name.equals("Mars") || name.equals("Neptune") || name.equals("Venus")
                                    || name.equals("Jupiter") || name.equals("Saturn") || name.equals("Uranus") || name.equals("Pluto")) {
                                double[] Planet_coordinates = solar_ob.GetPlanetCoords(name, viewModel, 0);
                                ra = Planet_coordinates[0] / 15;
                                dec = Planet_coordinates[1];
                                double[] precessed_ha_and_dec = tel.calculate_coords_with_precession(ra, dec, viewModel);
                                current_HA = precessed_ha_and_dec[0];
                                double precessed_dec = precessed_ha_and_dec[1];
                                text_ra.setText(decimal_to_dms(viewModel.getRA_decimal().getValue(), "ra"));
                                text_dec.setText(decimal_to_dms(precessed_dec, "dec"));

                            }

                        }

                        moon_recalculate_counter++;
                        planet_recalculate_counter++;
                        text_ha.setText(ha_Str);
                    }
                });
            }

        }


    };


    public void constructHashmap() {

        is_con_names = getResources().openRawResource(R.raw.constellations);
        jsonString_con_names = new Scanner(is_con_names).useDelimiter("\\A").next();
        Type listType_con_names = new TypeToken<HashMap<String, constellationObject>>() {
        }.getType();
        constellation_names = new Gson().fromJson(jsonString_con_names, listType_con_names);
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
