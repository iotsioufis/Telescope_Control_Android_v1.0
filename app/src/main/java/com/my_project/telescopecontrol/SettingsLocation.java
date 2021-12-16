package com.my_project.telescopecontrol;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.Locale;

import Jama.Matrix;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.SHARED_PREFS;
import static com.my_project.telescopecontrol.MainActivity.handler;
//import static com.my_project.telescopecontrol.MainActivity.viewModel;

public class SettingsLocation extends Fragment implements LocationListener {
    View v;
    private SharedViewModel viewModel;
    private TextView text_saved_longitude;
    private TextView text_saved_latitude;
    private TextView text_enter_location;
    private TextView text_latitude;
    private TextView text_longitude;
    private TextView text_gps_info;
    private EditText editText_latitude;
    private EditText editText_longitude;
    private MaterialButton button_get_gps;
    private MaterialButton button_set_manually;
    private MaterialButton button_save_location;
    private MaterialButton button_cancel;
    private ProgressBar progressBar;
    String latitude_str = "";
    String longitude_str = "";
    boolean gps_requested_by_click = false;
    double gps_latitude = 0.0;
    double gps_longitude = 0.0;
    LocationManager lm;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        v = inflater.inflate(R.layout.settings_location, container, false);
        View myActivityView = (RelativeLayout) getActivity().findViewById(R.id.relative);
        BottomNavigationView bottomNav = (BottomNavigationView) myActivityView.findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(GONE);
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        text_saved_latitude = v.findViewById(R.id.textView_saved_latitude);
        text_saved_longitude = v.findViewById(R.id.textView_saved_longitude);
        text_enter_location = v.findViewById(R.id.text_enter_location);
        text_latitude = v.findViewById(R.id.text_latitude);
        text_longitude = v.findViewById(R.id.text_longitude);
        text_gps_info = v.findViewById(R.id.textView_info_gps);
        editText_latitude = v.findViewById(R.id.editText_latitude);
        editText_longitude = v.findViewById(R.id.editText_longitude);
        button_get_gps = v.findViewById(R.id.button_get_gps);
        button_save_location = v.findViewById(R.id.button_save_location);
        button_set_manually = v.findViewById(R.id.button_set_manually);
        button_cancel = v.findViewById(R.id.button_cancel);
        progressBar = v.findViewById(R.id.gps_progress_bar);


        loadLocation();
        latitude_str = String.format(Locale.ENGLISH, "%.1f", viewModel.getLatitute().getValue());
        longitude_str = String.format(Locale.ENGLISH, "%.1f", viewModel.getLongitude().getValue());
        text_saved_latitude.setText("Saved Latitude :    \n" + latitude_str);
        text_saved_longitude.setText("Saved Longitude : \n" + longitude_str);
        editText_longitude.setHint(longitude_str);
        editText_latitude.setHint(latitude_str);

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);


        button_set_manually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                text_saved_latitude.setVisibility(GONE);
                text_saved_longitude.setVisibility(GONE);
                progressBar.setVisibility(GONE);
                button_cancel.setVisibility(GONE);
                text_gps_info.setVisibility(GONE);
                text_enter_location.setVisibility(View.VISIBLE);
                text_latitude.setVisibility(View.VISIBLE);
                text_longitude.setVisibility(View.VISIBLE);
                editText_latitude.setVisibility(View.VISIBLE);
                editText_longitude.setVisibility(View.VISIBLE);
                button_save_location.setVisibility(View.VISIBLE);

                if (lm != null) {
                    lm.removeUpdates(SettingsLocation.this);
                }

            }
        });


        button_save_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                text_saved_latitude.setVisibility(View.VISIBLE);
                text_saved_longitude.setVisibility(View.VISIBLE);
                text_enter_location.setVisibility(GONE);
                text_latitude.setVisibility(GONE);
                text_longitude.setVisibility(GONE);
                button_save_location.setVisibility(GONE);
                text_saved_latitude.setText("Saved Latitude :   \n" + editText_latitude.getText());
                text_saved_longitude.setText("Saved Longitude : \n" + editText_longitude.getText());
                if (!editText_latitude.getText().toString().equals("") && !editText_longitude.getText().toString().equals("")) {
                    viewModel.setLatitude(Double.parseDouble(editText_latitude.getText().toString()));
                    viewModel.setLongitude(Double.parseDouble(editText_longitude.getText().toString()));
                }
                editText_latitude.setVisibility(GONE);
                editText_longitude.setVisibility(GONE);
                saveLocation();

            }
        });

        button_get_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                gps_requested_by_click = true;
                getLocation();


                LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (isGPSEnabled) {
                    progressBar.setVisibility(View.VISIBLE);
                }


                text_saved_latitude.setVisibility(GONE);
                text_saved_longitude.setVisibility(GONE);
                text_enter_location.setVisibility(GONE);
                text_latitude.setVisibility(GONE);
                text_longitude.setVisibility(GONE);
                editText_latitude.setVisibility(GONE);
                editText_longitude.setVisibility(GONE);
                button_save_location.setVisibility(GONE);

            }
        });


        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(GONE);
                    lm.removeUpdates(SettingsLocation.this);
                } else if (progressBar.getVisibility() == GONE) {
                    /*open the gps settings*/
                    Intent settingintent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(settingintent);

                }
                button_cancel.setVisibility(GONE);
                text_gps_info.setVisibility(GONE);


            }
        });


        return v;

    }

    public Location getLocation() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permission not granded", Toast.LENGTH_SHORT).show();

            /*if persmission is not granded getlocation does not continue its execution and returns null*/
            /*for newer versions of android it is mandatory to stop if permission is not granded*/
            return null;
        }
        lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            /*if permission IS granded and gps is ENABLED get location*/
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);

            //Location location=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            text_gps_info.setText("Receiving GPS coordinates ...");
            button_cancel.setText(" cancel ");
            button_cancel.setVisibility(View.VISIBLE);
            text_gps_info.setVisibility(View.VISIBLE);


        } else {
            /*open the gps settings*/
            Intent settingintent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(settingintent, 0);


        }
        /*if GPS is NOT ENABLED return null*/

        return null;
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        gps_latitude = location.getLatitude();
        gps_longitude = location.getLongitude();
        viewModel.setLongitude(gps_longitude);
        viewModel.setLatitude(gps_latitude);
        double gps_accuracy = location.getAccuracy();
        // Toast.makeText(getContext(),"Lat: "+gps_latitude+"\n"+"Lon: "+gps_longitude+"\n"+"Accuracy"+gps_accuracy + " meters",Toast.LENGTH_LONG).show();
        Toast.makeText(getContext(), "Accuracy" + ((int) gps_accuracy) + " meters", Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
        latitude_str = String.format(Locale.ENGLISH, "%.1f", gps_latitude);
        longitude_str = String.format(Locale.ENGLISH, "%.1f", gps_longitude);
        text_saved_latitude.setText("Saved Latitude :    \n" + latitude_str);
        text_saved_longitude.setText("Saved Longitude : \n" + longitude_str);
        text_saved_latitude.setVisibility(View.VISIBLE);
        text_saved_longitude.setVisibility(View.VISIBLE);
        text_gps_info.setText("  GPS coordinates successfully received.\n\n    Device's GPS can now be disabled");
        button_cancel.setText("disable GPS");

        saveLocation();
        lm.removeUpdates(this);

    }


    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "GPS is turned ON ", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "GPS is turned OFF ", Toast.LENGTH_LONG).show();
        }
        progressBar.setVisibility(View.GONE);
        button_cancel.setVisibility(GONE);
        text_gps_info.setVisibility(GONE);
    }


    public void saveLocation() {
        editor.putFloat("latitude", (float) viewModel.getLatitute().getValue().doubleValue());
        editor.putFloat("longitude", (float) viewModel.getLongitude().getValue().doubleValue());
        editor.apply();
    }


    public void loadLocation() {
        viewModel.setLatitude(sharedPreferences.getFloat("latitude", 38.2f));
        viewModel.setLongitude(sharedPreferences.getFloat("longitude", 21.2f));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            button_get_gps.performClick();
        }

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}
