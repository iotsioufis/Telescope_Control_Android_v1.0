package com.my_project.telescopecontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.my_project.telescopecontrol.DeviceListAdapter;

import java.util.ArrayList;
import java.util.Set;

import static com.my_project.telescopecontrol.MainActivity.BLUETOOTH_DEVICE;
import static com.my_project.telescopecontrol.MainActivity.handler;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import static android.view.View.GONE;

public class SettingsBluetooth extends Fragment implements DeviceListAdapter.OnDeviceListener {
    String deviceName;
    String deviceHardwareAddress;
    private ArrayList<DeviceInfoModel> deviceList;
    private MaterialButton pair_new_device;
    private MaterialButton reload_list;
    RecyclerView recyclerView;
    private MaterialButton show_help;
    private MaterialButton close_help;
    private MaterialButton close_help_corner;
    private ConstraintLayout bluetooth_help_frame;



    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_bluetooth, container, false);
        View myActivityView = (RelativeLayout) getActivity().findViewById(R.id.relative);
        BottomNavigationView bottomNav = (BottomNavigationView) myActivityView.findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(GONE);
        recyclerView = view.findViewById(R.id.recyclerViewDevice);
        pair_new_device = view.findViewById(R.id.button_open_bluetooth_settings);
        reload_list = view.findViewById(R.id.button_reload_bluetooth_list);
        show_help = view.findViewById(R.id.button_show_bluetooth_help);
        bluetooth_help_frame = view.findViewById(R.id. frame_bluetooth_info);
        close_help_corner =view.findViewById(R.id.button_close_bluetooth_help);
        close_help =view.findViewById(R.id.button_hide_bluetooth_help);



        pair_new_device.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);

            }
        });
        reload_list.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                load_bt_list();
            }
        });

        show_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                bluetooth_help_frame.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(GONE);
                reload_list.setVisibility(GONE);
                pair_new_device.setVisibility(GONE);
            }
        });
       close_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                bluetooth_help_frame.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                reload_list.setVisibility(View.VISIBLE);
                pair_new_device.setVisibility(View.VISIBLE);
            }
        });

        close_help_corner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                bluetooth_help_frame.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                reload_list.setVisibility(View.VISIBLE);
                pair_new_device.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Get List of Paired Bluetooth Device
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList = new ArrayList<>();
        if (!bluetoothAdapter.isEnabled() || pairedDevices.size() == 0) {
            Snackbar snackbar = Snackbar.make(view, "Activate Bluetooth or pair a Bluetooth device", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                    getActivity().onBackPressed();
                    Intent intentOpenBluetoothSettings = new Intent();
                    intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(intentOpenBluetoothSettings);
                }
            });
            snackbar.show();
        }
        if (pairedDevices.size() > 0) {
            load_bt_list();
        }
    }

    @Override
    public void onDeviceClick(int position) {
        deviceName = deviceList.get(position).getDeviceName();
        deviceHardwareAddress = deviceList.get(position).getDeviceHardwareAddress();
        Bundle bundle = new Bundle();
        bundle.putString("deviceName", deviceName);
        bundle.putString("deviceAddress", deviceHardwareAddress);
        handler.obtainMessage(BLUETOOTH_DEVICE, bundle).sendToTarget();
        getActivity().onBackPressed();
    }


    private void load_bt_list() {
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        deviceList = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                deviceName = device.getName();
                deviceHardwareAddress = device.getAddress(); // MAC address
                DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName, deviceHardwareAddress);
                deviceList.add(deviceInfoModel);
            }
            // Display paired device using recyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            DeviceListAdapter deviceListAdapter = new DeviceListAdapter(deviceList, this);
            recyclerView.setAdapter(deviceListAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }
}
