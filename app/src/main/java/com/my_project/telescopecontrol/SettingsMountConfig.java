package com.my_project.telescopecontrol;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.SHARED_PREFS;

public class SettingsMountConfig extends Fragment {
    MaterialButton button_reset;
    MaterialButton button_save;

    private CheckBox check_invert_dec;
    private CheckBox check_invert_ra;
    private EditText dec_gear_teeth;
    private EditText dec_motor_pulley;
    private EditText dec_motor_steps;
    private EditText dec_mount_pulley;
    private EditText dec_speed;
    private EditText ra_gear_teeth;
    private EditText ra_motor_pulley;
    private EditText ra_motor_steps;
    private EditText ra_mount_pulley;
    private EditText ra_speed;
    View v;
    private SharedViewModel viewModel;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        v = inflater.inflate(R.layout.settings_mount_config, container, false);
        View myActivityView = (RelativeLayout) getActivity().findViewById(R.id.relative);
        BottomNavigationView bottomNav = (BottomNavigationView) myActivityView.findViewById(R.id.bottom_navigation);
        button_save = v.findViewById(R.id.button_save_config);
        button_reset = v.findViewById(R.id.button_reset);
        check_invert_ra = v.findViewById(R.id.checkBox_invert_ra);
        check_invert_dec = v.findViewById(R.id.checkBox_invert_dec);
        ra_motor_steps = v.findViewById(R.id.editText_ra_motor_steps);
        dec_motor_steps = v.findViewById(R.id.editText_dec_motor_steps);
        ra_gear_teeth = v.findViewById(R.id.editText_ra_gear_teeth);
        dec_gear_teeth = v.findViewById(R.id.editText_dec_gear_teeth);
        ra_mount_pulley = v.findViewById(R.id.editText_ra_mount_pulley);
        dec_mount_pulley = v.findViewById(R.id.editText_dec_mount_pulley);
        ra_motor_pulley = v.findViewById(R.id.editText_ra_motor_pulley);
        dec_motor_pulley = v.findViewById(R.id.editText_dec_motor_pulley);
        ra_speed = v.findViewById(R.id.editText_ra_motor_speed);
        dec_speed = v.findViewById(R.id.editText_dec_motor_speed);

        loadConfig();

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                saveConfig();

            }
        });

        button_reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                viewModel.setRA_motor_steps(200);
                viewModel.setDEC_motor_steps(200);
                viewModel.setRA_gear_teeth(130);
                viewModel.setDEC_gear_teeth(65);
                viewModel.setRA_mount_pulley(40);
                viewModel.setRA_motor_pulley(16);
                viewModel.setDEC_mount_pulley(40);
                viewModel.setDEC_motor_pulley(16);
                viewModel.setRASpeed(3500);
                viewModel.setDECSpeed(2500);
                viewModel.set_is_ra_motor_inverted(true);
                viewModel.set_is_dec_motor_inverted(false);
                update_current_config();
                saveConfig();
            }
        });

        check_invert_ra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check_invert_ra.isChecked()) {
                    viewModel.set_is_ra_motor_inverted(true);
                }
                if (!check_invert_ra.isChecked()) {
                    viewModel.set_is_ra_motor_inverted(false);
                }
            }
        });

        check_invert_dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check_invert_dec.isChecked()) {
                    viewModel.set_is_dec_motor_inverted(true);
                }
                if (!check_invert_dec.isChecked()) {
                    viewModel.set_is_dec_motor_inverted(false);
                }
            }
        });
        return v;
    }

    public void saveConfig() {
        /*Save Entered values to SharedViewmodel: */
        boolean is_valid = true;
        if (ra_motor_steps.getText().toString().equals("")) {
            Toast.makeText(getContext(), "INVALID RA motor steps number", Toast.LENGTH_SHORT).show();
            viewModel.setRA_motor_steps(0);
            is_valid = false;
        } else {
            viewModel.setRA_motor_steps(Integer.parseInt(ra_motor_steps.getText().toString()));
        }
        if (dec_motor_steps.getText().toString().equals("")) {
            Toast.makeText(getContext(), "INVALID DEC motor steps number", Toast.LENGTH_SHORT).show();
            viewModel.setDEC_motor_steps(0);
            is_valid = false;
        } else {
            viewModel.setDEC_motor_steps(Integer.parseInt(dec_motor_steps.getText().toString()));
        }
        if (ra_gear_teeth.getText().toString().equals("")) {
            Toast.makeText(getContext(), "INVALID RA gear teeth number", Toast.LENGTH_SHORT).show();
            viewModel.setRA_gear_teeth(0);
            is_valid = false;
        } else {
            viewModel.setRA_gear_teeth(Integer.parseInt(ra_gear_teeth.getText().toString()));
        }
        if (dec_gear_teeth.getText().toString().equals("")) {
            Toast.makeText(getContext(), "INVALID DEC gear teeth number", Toast.LENGTH_SHORT).show();
            viewModel.setDEC_gear_teeth(0);
            is_valid = false;
        } else {
            viewModel.setDEC_gear_teeth(Integer.parseInt(dec_gear_teeth.getText().toString()));
        }
        if (ra_mount_pulley.getText().toString().equals("")) {
            Toast.makeText(getContext(), "INVALID RA mount pulley teeth number", Toast.LENGTH_SHORT).show();
            viewModel.setRA_mount_pulley(0);
            is_valid = false;
        } else {
            viewModel.setRA_mount_pulley(Integer.parseInt(ra_mount_pulley.getText().toString()));
        }
        if (dec_mount_pulley.getText().toString().equals("")) {
            Toast.makeText(getContext(), "INVALID DEC mount pulley teeth number", Toast.LENGTH_SHORT).show();
            viewModel.setDEC_mount_pulley(0);
            is_valid = false;
        } else {
            viewModel.setDEC_mount_pulley(Integer.parseInt(dec_mount_pulley.getText().toString()));
        }
        if (ra_motor_pulley.getText().toString().equals("") || ra_motor_pulley.getText().toString().equals("0")) {
            Toast.makeText(getContext(), "INVALID RA motor pulley teeth number", Toast.LENGTH_SHORT).show();
            viewModel.setRA_motor_pulley(1);
            is_valid = false;
        } else {
            viewModel.setRA_motor_pulley(Integer.parseInt(ra_motor_pulley.getText().toString()));
        }
        if (dec_motor_pulley.getText().toString().equals("") || dec_motor_pulley.getText().toString().equals("0")) {
            Toast.makeText(getContext(), "INVALID DEC motor pulley teeth number", Toast.LENGTH_SHORT).show();
            viewModel.setDEC_motor_pulley(1);
            is_valid = false;
        } else {
            viewModel.setDEC_motor_pulley(Integer.parseInt(dec_motor_pulley.getText().toString()));
        }
        if (ra_speed.getText().toString().equals("") || ra_speed.getText().toString().equals("0")) {
            Toast.makeText(getContext(), "INVALID RA motor speed", Toast.LENGTH_SHORT).show();
            viewModel.setRASpeed(0);
            is_valid = false;
        } else {
            viewModel.setRASpeed(Integer.parseInt(ra_speed.getText().toString()));
            if (Integer.parseInt(ra_speed.getText().toString()) < 500 || Integer.parseInt(ra_speed.getText().toString()) > 4000) {
                ra_speed.setText("0");
                Toast.makeText(getContext(), "INVALID RA speed", Toast.LENGTH_SHORT).show();
                is_valid = false;
            }
        }
        if (dec_speed.getText().toString().equals("") || dec_speed.getText().toString().equals("0")) {
            Toast.makeText(getContext(), "INVALID DEC motor speed", Toast.LENGTH_SHORT).show();
            viewModel.setDECSpeed(0);
            is_valid = false;
        } else {
            viewModel.setDECSpeed(Integer.parseInt(dec_speed.getText().toString()));
            if (Integer.parseInt(dec_speed.getText().toString()) < 500 || Integer.parseInt(dec_speed.getText().toString()) > 4000) {
                dec_speed.setText("0");
                Toast.makeText(getContext(), "INVALID DEC speed", Toast.LENGTH_SHORT).show();
                is_valid = false;
            }
        }
        if (is_valid) {
            /*save to SharedPreferences: */
            SharedPreferences sharedPreferences3 = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor3 = sharedPreferences3.edit();
            editor3.putInt("ra_motor_steps", viewModel.getRA_motor_steps().getValue());
            editor3.putInt("dec_motor_steps", viewModel.getDEC_motor_steps().getValue());
            editor3.putInt("ra_gear_teeth", viewModel.getRA_gear_teeth().getValue());
            editor3.putInt("dec_gear_teeth", viewModel.getDEC_gear_teeth().getValue());
            editor3.putInt("ra_mount_pulley", viewModel.getRA_mount_pulley().getValue());
            editor3.putInt("dec_mount_pulley", viewModel.getDEC_mount_pulley().getValue());
            editor3.putInt("ra_motor_pulley", viewModel.getRA_motor_pulley().getValue());
            editor3.putInt("dec_motor_pulley", viewModel.getDEC_motor_pulley().getValue());
            editor3.putInt("ra_speed", viewModel.getRASpeed().getValue());
            editor3.putInt("dec_speed", viewModel.getDECSpeed().getValue());
            editor3.putBoolean("is_ra_motor_inverted", viewModel.get_is_ra_motor_inverted().getValue());
            editor3.putBoolean("is_dec_motor_inverted", viewModel.get_is_dec_motor_inverted().getValue());
            editor3.apply();
            build_config_command();
        } else if (!is_valid) {
            Toast.makeText(getContext(), "Invalid configuration NOT saved", Toast.LENGTH_LONG).show();
        }
    }

    public void loadConfig() {
        SharedPreferences sharedPreferences3 = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        /*Load from SharedPreferences to the SharedviewModel : */

        viewModel.setRA_motor_steps(sharedPreferences3.getInt("ra_motor_steps", 200));
        viewModel.setDEC_motor_steps(sharedPreferences3.getInt("dec_motor_steps", 200));
        viewModel.setRA_gear_teeth(sharedPreferences3.getInt("ra_gear_teeth", 130));
        viewModel.setDEC_gear_teeth(sharedPreferences3.getInt("dec_gear_teeth", 65));
        viewModel.setRA_mount_pulley(sharedPreferences3.getInt("ra_mount_pulley", 40));
        viewModel.setDEC_mount_pulley(sharedPreferences3.getInt("dec_mount_pulley", 40));
        viewModel.setRA_motor_pulley(sharedPreferences3.getInt("ra_motor_pulley", 16));
        viewModel.setDEC_motor_pulley(sharedPreferences3.getInt("dec_motor_pulley", 16));
        viewModel.setRASpeed(sharedPreferences3.getInt("ra_speed", 3500));
        viewModel.setDECSpeed(sharedPreferences3.getInt("dec_speed", 2500));
        viewModel.set_is_ra_motor_inverted(Boolean.valueOf(sharedPreferences3.getBoolean("is_ra_motor_inverted", true)));
        viewModel.set_is_dec_motor_inverted(Boolean.valueOf(sharedPreferences3.getBoolean("is_dec_motor_inverted", false)));
        update_current_config();
    }

    public void update_current_config() {
        ra_motor_steps.setText(viewModel.getRA_motor_steps().getValue().toString());
        dec_motor_steps.setText(viewModel.getDEC_motor_steps().getValue().toString());
        ra_gear_teeth.setText(viewModel.getRA_gear_teeth().getValue().toString());
        dec_gear_teeth.setText(viewModel.getDEC_gear_teeth().getValue().toString());
        ra_mount_pulley.setText(viewModel.getRA_mount_pulley().getValue().toString());
        dec_mount_pulley.setText(viewModel.getDEC_mount_pulley().getValue().toString());
        ra_motor_pulley.setText(viewModel.getRA_motor_pulley().getValue().toString());
        dec_motor_pulley.setText(viewModel.getDEC_motor_pulley().getValue().toString());
        ra_speed.setText(viewModel.getRASpeed().getValue().toString());
        dec_speed.setText(viewModel.getDECSpeed().getValue().toString());
        if (viewModel.get_is_ra_motor_inverted().getValue()) {
            check_invert_ra.setChecked(true);
        }
        if (viewModel.get_is_dec_motor_inverted().getValue()) {
            check_invert_dec.setChecked(true);
        }
    }

    public void build_config_command() {
        int RA_gear_teeth = viewModel.getRA_gear_teeth().getValue();
        int DEC_gear_teeth = viewModel.getDEC_gear_teeth().getValue();
        double RA_motor_ratio = viewModel.getRA_mount_pulley().getValue() / (double) viewModel.getRA_motor_pulley().getValue();
        double DEC_motor_ratio = viewModel.getDEC_mount_pulley().getValue() / (double) viewModel.getDEC_motor_pulley().getValue();
        int RA_motor_steps = viewModel.getRA_motor_steps().getValue();
        int DEC_motor_steps = viewModel.getDEC_motor_steps().getValue();

        int microstep_mode = 16;
        //microsteps needed for a full 360 rotation of the RA MOUNT's axis:
        double RA_micro_360 = RA_gear_teeth * RA_motor_ratio * RA_motor_steps * microstep_mode;
        // microsteps needed for a full 360 rotation of the DEC MOUNT's axis:
        double DEC_micro_360 = DEC_gear_teeth * DEC_motor_ratio * DEC_motor_steps * microstep_mode;
        //viewModel = MainActivity.viewModel;
        //microsteps needed for ONE degree rotation  of the RA MOUNT's axis:
        double RA_micro_1 = RA_micro_360 / 360.0;

        // microsteps needed for ONE degree rotation  of the DEC MOUNT's axis:
        double DEC_micro_1 = DEC_micro_360 / 360.0;
        //sidereal day( 24h - 56 minutes, 4.0905 in seconds), constant:
        double s_day_in_secs = 86164.0905;
        //arcseconds for a full Earth rotation,  used for determining sidereal and tracking rate in
        //microsteps .contant: 1296000
        int earth_arcsec_360 = 360 * 60 * 60;

        // %Earth's rotation rate in arcsecond per second:
        double Earth_rotation_rate = earth_arcsec_360 / s_day_in_secs;

        // %microstep/arcseconds of RA motor:

        double microsteps_per_arcsec = RA_micro_360 / (double) earth_arcsec_360;


        /* having the Earth's rotation rate in arcseconds per second and the
          RA motor's microsteps per arcsecond bellow is the tracking rate in
         microsteps per second:*/

        double sidereal_tracking_rate = Earth_rotation_rate * microsteps_per_arcsec;


        String ra_speed_str = viewModel.getRASpeed().getValue().toString();
        double d3 = Earth_rotation_rate;
        String dec_speed_str = viewModel.getDECSpeed().getValue().toString();
        double d4 = microsteps_per_arcsec;

        String sidereal_rate_str = String.format(Locale.ENGLISH, "%.7f", sidereal_tracking_rate);
        String RA_micro_1_X4_mode_str = String.format(Locale.ENGLISH, "%.3f", RA_micro_1 / 4);
        String DEC_micro_1_X4_mode_str = String.format(Locale.ENGLISH, "%.3f", DEC_micro_1 / 4);


        viewModel.set_sidereal_rate(sidereal_tracking_rate);
       // if(viewModel.getLatitute().getValue()<0){ viewModel.set_sidereal_rate(-sidereal_tracking_rate);}
        viewModel.setRA_micro_1(RA_micro_1 / 4.0d);
        viewModel.setDEC_micro_1(DEC_micro_1 / 4.0d);

        String is_ra_motor_inverted_str = "";
        String is_dec_motor_inverted_str = "";
        if (viewModel.get_is_ra_motor_inverted().getValue()) {
            is_ra_motor_inverted_str = "1";
        } else if (!viewModel.get_is_ra_motor_inverted().getValue()) {
            is_ra_motor_inverted_str = "0";
        }
        if (viewModel.get_is_dec_motor_inverted().getValue()) {
            is_dec_motor_inverted_str = "1";
        } else if (!viewModel.get_is_dec_motor_inverted().getValue()) {
            is_dec_motor_inverted_str = "0";
        }

        String command_to_send = "<config:" + ra_speed_str + ":" + dec_speed_str + ":" + sidereal_rate_str + ":" + is_ra_motor_inverted_str + ":" + is_dec_motor_inverted_str + ":>\n";
        String str = ra_speed_str;
        MainActivity.handler.obtainMessage(MESSAGE_WRITE, command_to_send).sendToTarget();
        viewModel.setConfig_command(command_to_send);
        SharedPreferences sharedPreferences3 = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor3 = sharedPreferences3.edit();
        editor3.putInt("ra_micro_1", (int) (RA_micro_1 / 4));
        editor3.putInt("dec_micro_1", (int) (DEC_micro_1 / 4));
        editor3.putString("config_command", command_to_send);
        editor3.putString("sidereal_rate_str", sidereal_rate_str);
        editor3.putString("RA_micro_1_X4_mode", RA_micro_1_X4_mode_str);
        editor3.putString("DEC_micro_1_X4_mode", DEC_micro_1_X4_mode_str);
        editor3.apply();
        Toast.makeText(getContext(), "Configuration Saved \n  and sent to mount", Toast.LENGTH_LONG).show();
    }
}
