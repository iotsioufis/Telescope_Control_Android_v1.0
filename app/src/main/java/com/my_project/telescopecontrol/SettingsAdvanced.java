package com.my_project.telescopecontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.SHARED_PREFS;
import static com.my_project.telescopecontrol.MainActivity.handler;

public class SettingsAdvanced extends Fragment {
    View v;
    private SharedViewModel viewModel;
    private RadioGroup radio_group_sky_model;
    private RadioButton radioButton;
    private RadioGroup radio_group_accuracy_alignment;
    private RadioGroup radio_group_auto_tracking;
    private int radioId;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        v = inflater.inflate(R.layout.settings_advanced, container, false);
        View myActivityView = (RelativeLayout) getActivity().findViewById(R.id.relative);
        BottomNavigationView bottomNav = (BottomNavigationView) myActivityView.findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(GONE);
        radio_group_sky_model = v.findViewById(R.id.radio_group_sky_model);
        radio_group_accuracy_alignment = v.findViewById(R.id.radio_group_accuracy_during_alignment);
        radio_group_auto_tracking=v.findViewById(R.id.radio_group_auto_tracking);
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        radio_group_auto_tracking.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioId = radio_group_auto_tracking.getCheckedRadioButtonId();
                radioButton = v.findViewById(radioId);
                String radioText = radioButton.getText().toString();

                switch (radioText) {
                    case "ON":
                        viewModel.set_auto_tracking(true);
                        editor.putBoolean("auto_tracking", true);
                        editor.apply();
                        handler.obtainMessage(MESSAGE_WRITE, "<set_auto_tracking:1:>\n").sendToTarget();
                        break;
                    case "OFF":
                        viewModel.set_auto_tracking(false);
                        editor.putBoolean("auto_tracking", false);
                        editor.apply();
                        handler.obtainMessage(MESSAGE_WRITE, "<set_auto_tracking:0:>\n").sendToTarget();
                        break;

                }


            }
        });

        radio_group_sky_model.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioId = radio_group_sky_model.getCheckedRadioButtonId();
                radioButton = v.findViewById(radioId);
                String radioText = radioButton.getText().toString();

                switch (radioText) {
                    case "Initial Star\nAlignment":
                        viewModel.set_use_only_basic_transformation(true);
                        editor.putBoolean("use_only_basic_transformation", true);
                        editor.apply();
                        break;
                    case "Extra Alignment\nStars":
                        viewModel.set_use_only_basic_transformation(false);
                        editor.putBoolean("use_only_basic_transformation", false);
                        editor.apply();
                        break;
                }
            }
        });



        radio_group_accuracy_alignment.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioId = radio_group_accuracy_alignment.getCheckedRadioButtonId();
                radioButton = v.findViewById(radioId);
                String radioText = radioButton.getText().toString();

                switch (radioText) {
                    case "Improved":
                        viewModel.set_include_initial_offsets(true);
                        editor.putBoolean("include_initial_offsets", true);
                        editor.apply();
                        break;
                    case "Normal":
                        viewModel.set_include_initial_offsets(false);
                        editor.putBoolean("include_initial_offsets", false);
                        editor.apply();
                        break;
                }
            }
        });




        if (viewModel.get_use_only_basic_transformation().getValue()) {
            radio_group_sky_model.check(R.id.radio_initial_alignment);
        }
        if (!viewModel.get_use_only_basic_transformation().getValue()) {
            radio_group_sky_model.check(R.id.radio_extra_alignment_stars);
        }


        if (viewModel.get_include_initial_offsets().getValue()) {
            radio_group_accuracy_alignment.check(R.id.radio_increase_accuracy);
        }
        if (!viewModel.get_include_initial_offsets().getValue()) {
            radio_group_accuracy_alignment.check(R.id.radio_do_not_increase_accuracy);
        }

        if (viewModel.get_auto_tracking().getValue()) {
            radio_group_auto_tracking.check(R.id.radio_auto_tracking_on);
        }
        if (!viewModel.get_auto_tracking().getValue()) {
            radio_group_auto_tracking.check(R.id.radio_auto_tracking_off);
        }


        return v;
    }

}
