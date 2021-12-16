package com.my_project.telescopecontrol;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static android.view.View.GONE;
import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.handler;

public class SettingsPowerManagement extends Fragment {
    View v;
    private SharedViewModel viewModel;
    private RadioGroup radio_group_ESM;
    private RadioButton radioButton;
    private int radioId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        v = inflater.inflate(R.layout.settings_power_management, container, false);
        View myActivityView = (RelativeLayout) getActivity().findViewById(R.id.relative);
        BottomNavigationView bottomNav = (BottomNavigationView) myActivityView.findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(GONE);
        radio_group_ESM = v.findViewById(R.id.radio_group_sky_model);
        handler.obtainMessage(MESSAGE_WRITE, "<request_energy_saving:>\n").sendToTarget();


        viewModel.get_energy_saving_on().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean received_energy_saving_state) {
                if (viewModel.get_energy_saving_on().getValue()) {
                    radioButton = v.findViewById(R.id.radio_ESM_ON);
                                 }
                if (!viewModel.get_energy_saving_on().getValue()) {
                    radioButton = v.findViewById(R.id.radio_ESM_OFF);
                                  }
                if(!radioButton.isChecked()){
                    radioButton.setChecked(true);}

            }
        });


        radio_group_ESM.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioId = radio_group_ESM.getCheckedRadioButtonId();
                radioButton = v.findViewById(radioId);
                String radioText = radioButton.getText().toString();

                switch (radioText) {
                    case "ON":
                        handler.obtainMessage(MESSAGE_WRITE, "<set_energy_saving:1:>\n").sendToTarget();
                        break;
                    case "OFF":
                        handler.obtainMessage(MESSAGE_WRITE, "<set_energy_saving:0:>\n").sendToTarget();
                        break;

                }
            }
        });

        return v;
    }

}
