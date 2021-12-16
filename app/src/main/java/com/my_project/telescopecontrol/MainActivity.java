package com.my_project.telescopecontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static android.os.SystemClock.sleep;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    private static final int CONNECTION_LOST = -1; // used in bluetooth handler to identify connection lost state
    private static final int INITIAL = 0;
    private static final int CONNECTED = 1;
    private static final int CONNECTING_STATUS = 1; //used in bluetooth handler to identify message status
    private static final int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    public static final int MESSAGE_WRITE = 3; // used in bluetooth handler to send a command to Arduino
    public static final int BLUETOOTH_DEVICE = 4; // used in bluetooth handler to get the name and MAC of the connected bluetooth mount
    public static final int OPEN_ALIGNMENT_SCREEN = 5;  // used in bluetooth handler to switch to the star alignment screen
    public static final int OPEN_CAMERA_SCREEN = 6; // used in bluetooth handler to switch to the camera utility screen
    public static final int OPEN_POLAR_ALIGNMENT_SCREEN = 7; // used in bluetooth handler to switch to the polar alignment screen
    public static final int OPEN_FIX_BACKLASH_SCREEN = 8; // used in bluetooth handler to switch to the star alignment screen
    public static final int OPEN_STAR_SUGGESTION_SCREEN = 9; // used in bluetooth handler to switch to the star suggestion screen
    public static final int OPEN_HOME_SCREEN = 10; // used in bluetooth handler to switch to the Home screen
    public static final int OPEN_NEAREST_STAR_SUGGESTION_SCREEN = 11; // used in bluetooth handler to switch to the nearest star suggestion screen
    public static final int OPEN_AUTO_CENTER_SCREEN = 12; // used in bluetooth handler to switch to the auto_centering  screen
    public static final int OPEN_CROSS_TEST_SCREEN = 13; // used in bluetooth handler to switch to the auto_centering  screen
    public static final int OPEN_ABOUT_SCREEN = 14; // used in bluetooth handler to switch to the auto_centering  screen

    public static final int REQUEST_ENABLE_BT = 1;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String BT_DEVICE = "bt_device";
    public static final String MAC_ADDRESS = "mac_address";


    private String deviceAddress;
    private String deviceName = null;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public ConnectedThread connectedThread;
    public CreateConnectThread createConnectThread;
    private SharedViewModel viewModel;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private String stored_address;
    private String stored_device;


    AlignmentFragment alignmentFragment = new AlignmentFragment();
    AlignmentStarsSuggestionFragment alignmentStarsSuggestionFragment = new AlignmentStarsSuggestionFragment();
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    CameraFragment cameraFragment = new CameraFragment();
    ControlsFragment controlsFragment = new ControlsFragment();
    FixBacklashFragment fixBacklashFragment = new FixBacklashFragment();
    HomeFragment homeFragment = new HomeFragment();
    NearestStarSuggestionFragment nearestStarSuggestionFragment = new NearestStarSuggestionFragment();
    PolarAlignmentFragment polarAlignmentFragment = new PolarAlignmentFragment();
    AutoCenterFragment autoCenterFragment =new AutoCenterFragment();
    CrossTestFragment crossTestFragment = new CrossTestFragment();
    SelectObjectFragment selectObjectFragment = new SelectObjectFragment();
    BtBroadcastReceiver myBroadcastReceiver = new BtBroadcastReceiver();
    SolarCalcs solar_ob =new SolarCalcs();

    Fragment selectedFragment = null;
    int connect_flag = INITIAL;
    String selected_fragment_str = "";
    private TelescopeCalcs tel = new TelescopeCalcs();

    Menu menu1;


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu1 = menu;
        return super.onCreateOptionsMenu(menu);
    }


    public boolean onOptionsItemSelected(MenuItem item1) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        switch (item1.getItemId()) {


            case R.id.icon_bt_connection:
                item1.setIcon(R.drawable.ic_connected);
                loadBtDevice();
                deviceAddress = stored_address;
                deviceName = stored_device;

                if (this.connect_flag == INITIAL) {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        item1.setIcon(R.drawable.ic_disconnected);
                    }

                    if (stored_address.equals("24:42:16:08:00:00") && bluetoothAdapter.isEnabled()) {
                        item1.setIcon(R.drawable.ic_disconnected);
                        SettingsBluetooth settingsBluetooth = new SettingsBluetooth();
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsBluetooth).addToBackStack(null).commit();
                    }
                    if (bluetoothAdapter.isEnabled() && stored_address != "24:42:16:08:00:00") {
                        final Toolbar toolbar = findViewById(R.id.toolbar);
                        toolbar.setSubtitle("Connecting to " + deviceName + "...");
                        createConnectThread = new CreateConnectThread(bluetoothAdapter, stored_address);
                        createConnectThread.start();
                    }
                }
                if (connect_flag == CONNECTION_LOST && bluetoothAdapter.isEnabled()) {

                    final Toolbar toolbar = findViewById(R.id.toolbar);
                    toolbar.setSubtitle("Connecting to " + deviceName + "...");
                    if (createConnectThread != null) {
                        createConnectThread.cancel();
                    }
                    createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
                    createConnectThread.start();
                }
                if (connect_flag == CONNECTION_LOST && !bluetoothAdapter.isEnabled()) {

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                    item1.setIcon(R.drawable.ic_disconnected);
                }

                if (connect_flag == CONNECTED) {

                    item1.setIcon(R.drawable.ic_disconnected);
                    createConnectThread.cancel();
                }

                break;


            case R.id.bt_Settings:
                if (createConnectThread != null) {
                    createConnectThread.cancel();
                }

                SettingsBluetooth settingsBluetooth = new SettingsBluetooth();
                if (getSupportFragmentManager().getFragments().size() > 1) {
                    getSupportFragmentManager().popBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsBluetooth).addToBackStack(null).commit();
                break;

            case R.id.mount_settings:
                SettingsMountConfig settingsMountConfig = new SettingsMountConfig();
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsMountConfig).addToBackStack(null).commit();
                break;

            case R.id.location_settings:
                SettingsLocation settingsLocation = new SettingsLocation();
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsLocation).addToBackStack(null).commit();
                break;

            case R.id.power_management_settings:
                SettingsPowerManagement settingsPowerManagement = new SettingsPowerManagement();
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsPowerManagement).addToBackStack(null).commit();
                break;

            case R.id.advanced_settings:
                SettingsAdvanced settingsAdvanced = new SettingsAdvanced();
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsAdvanced).addToBackStack(null).commit();
                break;

            case R.id.action_About:
                AboutFragment aboutFragment = new AboutFragment();
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, aboutFragment).addToBackStack(null).commit();
                break;


        }
        return super.onOptionsItemSelected(item1);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.icon_bt_connection);
        switch (item.getItemId()) {
            case R.id.icon_bt_connection:
                if (connect_flag == CONNECTED) {

                    item.setIcon(R.drawable.ic_connected);
                }
        }
        if (connect_flag == CONNECTION_LOST) {
            item.setIcon(R.drawable.ic_disconnected);
        }

        return super.onPrepareOptionsMenu(menu);
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        viewModel.set_sidereal_rate(Double.parseDouble(sharedPreferences.getString("sidereal_rate_str", "0")));
        viewModel.setRA_micro_1(Double.parseDouble(sharedPreferences.getString("RA_micro_1_X4_mode", "0")));
        viewModel.setDEC_micro_1(Double.parseDouble(sharedPreferences.getString("DEC_micro_1_X4_mode", "0")));
        viewModel.setLatitude((double) sharedPreferences.getFloat("latitude", 38.2f));
        viewModel.setLongitude((double) sharedPreferences.getFloat("longitude", 21.2f));
        viewModel.set_use_only_basic_transformation(sharedPreferences.getBoolean("use_only_basic_transformation", false));
        viewModel.set_include_initial_offsets(sharedPreferences.getBoolean("include_initial_offsets", false));
        viewModel.set_auto_tracking(sharedPreferences.getBoolean("auto_tracking", false));
        Type BacklashfixeslistType = new TypeToken<ArrayList<BacklashFixesPoint>>() {}.getType();
        viewModel.setBacklashfixes((ArrayList) new Gson().fromJson(sharedPreferences.getString("backlash_fixes", ""), BacklashfixeslistType));
        setContentView(R.layout.activity_main);
        final BottomNavigationView bottonNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);


        bottonNav.setOnNavigationItemSelectedListener(navlistener);

        if (savedInstanceState != null) {

            connect_flag = savedInstanceState.getInt("connect_flag");
            if (connect_flag == CONNECTED) {
                loadBtDevice();
                deviceAddress = stored_address;
                deviceName = stored_device;
                createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
                createConnectThread.start();
            }
            viewModel.set_alignment_fragment_restore(true);
            viewModel.set_home_fragment_restore(true);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, alignmentFragment).addToBackStack(null).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).addToBackStack(null).commit();
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().popBackStack();
        }


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
            editor.putBoolean("alignment_done", false);
            editor.putBoolean("begin_alignment", false);
            editor.apply();
        }
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        LocalTime starting_time = LocalTime.now();
        double initial_time = starting_time.getHour() + starting_time.getMinute() / 60.0 + starting_time.getSecond() / 3600.0;
        viewModel.setInitial_time(initial_time);
        try {
            if (!bluetoothAdapter.isEnabled()) {

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
            catch (Exception bluetooth_not_found){
                Toast.makeText(MainActivity.this, "Device's Bluetooth not found or not supported", Toast.LENGTH_SHORT).show();
            }



        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");

            // Show progress and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            /*   When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below) */

            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }


        handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                int i;
                Message message = msg;
                switch (message.what) {

                    case CONNECTING_STATUS:
                        if (message.arg1 == 1) {
                            if (deviceName == null) {
                                deviceName = stored_device;
                            }
                            toolbar.setSubtitle("Connected to " + deviceName);
                            invalidateOptionsMenu();
                            connect_flag = CONNECTED;
                            saveBtDevice();
                            loadConfiguration();
                        }
                        if (msg.arg1 == -1) {
                            toolbar.setSubtitle("Device fails to connect");
                            connect_flag = CONNECTION_LOST;
                            saveBtDevice();
                            invalidateOptionsMenu();
                        }
                        break;

                    case MESSAGE_READ:
                        /*Receive a message from arduino .*/

                        String arduinoMsg = message.obj.toString();
                        if (arduinoMsg.contains("align_offsets:")) {
                            String[] parts1 = arduinoMsg.split(":", 2);
                            String second_part_of_arduinoMsg = parts1[1];
                            String parts2[] = second_part_of_arduinoMsg.split(",", 2);

                            String ra_offset_str = parts2[0];
                            String dec_offset_str = parts2[1];
                            Integer ra_offset = Integer.parseInt(ra_offset_str);
                            Integer dec_offset = Integer.parseInt(dec_offset_str);

                            LocalTime ltime = LocalTime.now();
                            double local_time = ltime.getHour() + ltime.getMinute() / 60.0 + ltime.getSecond() / 3600.0;

                            viewModel.setRA_offset(ra_offset.intValue());
                            viewModel.setDEC_offset(dec_offset.intValue());
                            viewModel.setAlignment_time(local_time);
                        }

                        if (arduinoMsg.contains("goto_ending_directions:")) {
                            String[] parts1 = arduinoMsg.split(":", 3);
                            String ra_ending_direction = parts1[1];
                            String dec_ending_direction = parts1[2];
                            viewModel.set_ra_goto_ending_direction(Integer.parseInt(ra_ending_direction));
                            viewModel.set_dec_goto_ending_direction(Integer.parseInt(dec_ending_direction));
                        }

                      /*  if (arduinoMsg.contains("request_backlash_fixes")) {
                            connectedThread.write("<backlash_fixes:" +  viewModel.get_ra_backlash_fix().getValue()+ ":"
                                    + viewModel.get_dec_backlash_fix().getValue() + ":>\n");
                                                }*/


						/* "motors_stopped" message is received every time both motors are stopped .
                        It is used as a way to indicate the mount's movement has ended */

                        if (arduinoMsg.equals("motors_stopped")) {
                            viewModel.setMoving_status("motors stopped.");
                        }
                        if (arduinoMsg.equals("motors_not_stopped")) {
                            viewModel.setMoving_status("motors not stopped.");
                        }
                        if (arduinoMsg.equals("move_canceled")) {
                            viewModel.setMoving_status("move canceled");
                        }


                        if (arduinoMsg.equals("configuration_received")) {
                            /*After configuration command , send initialization command.Both mototors will move5 degrees back and forth: */
                            connectedThread.write("<initialize:" + (int) (sharedPreferences.getInt("ra_micro_1", 722) * 5) + ":"
                                    + (int) (sharedPreferences.getInt("dec_micro_1", 361) * 5) + ":>\n");
                        }

                        if (arduinoMsg.equals("initialization_done")) {
                            if(viewModel.get_auto_tracking().getValue()){
                            connectedThread.write("<set_auto_tracking:1:>\n");}
                            if(!viewModel.get_auto_tracking().getValue()){
                                connectedThread.write("<set_auto_tracking:0:>\n");}
                        }
                        if (arduinoMsg.equals("esm_currently:0")) {
                            viewModel.set_energy_saving_on(false);
                        }
                        if (arduinoMsg.equals("esm_currently:1")) {
                            viewModel.set_energy_saving_on(true);

                        }
                        if (arduinoMsg.equals("ra_tracking_limit_reached")) {
                            viewModel.setTracking_status(false);
                        }
                        if (arduinoMsg.equals("auto_tracking_started")) {
                            viewModel.setTracking_status(true);
                        }
                        if (arduinoMsg.equals("request_tracking") && viewModel.getStar_object().getValue()!=null) {
                            int ra_horizon_tracking_limit = viewModel.get_ra_steps_at_horizon().getValue();
                            String name =viewModel.getStar_object().getValue().getName_ascii();
                            if (name.equals("Moon")) {
                                double[] tracking_rates_moon = solar_ob.GetMoonTrackingRates(viewModel);
                                connectedThread.write("<track:" + tracking_rates_moon[0] + ":" + tracking_rates_moon[1] + ":" + ra_horizon_tracking_limit + ":>\n");
                            }
                            if (name.equals("Mercury") || name.equals("Mars") || name.equals("Neptune") || name.equals("Venus")
                                    || name.equals("Jupiter") || name.equals("Saturn") || name.equals("Uranus") || name.equals("Pluto")) {
                                double[] tracking_rates = solar_ob.GetPlanetTrackingRates(name, viewModel);
                                connectedThread.write("<track:" + tracking_rates[0] + ":" + tracking_rates[1] + ":" + ra_horizon_tracking_limit + ":>\n");
                            }
                            if (!name.equals("Moon") && !name.equals("Mercury") && !name.equals("Mars") && !name.equals("Neptune") && !name.equals("Venus")
                                    && !name.equals("Jupiter") && !name.equals("Saturn") && !name.equals("Uranus") && !name.equals("Pluto")) {
                                if(viewModel.getLatitute().getValue()<0){connectedThread.write("<track:" + viewModel.get_sidereal_rate().getValue() + ":" + 0 + ":" + ra_horizon_tracking_limit + ":>\n");}
                                if(viewModel.getLatitute().getValue()>=0){connectedThread.write("<track:" + viewModel.get_sidereal_rate().getValue()*(-1) + ":" + 0 + ":" + ra_horizon_tracking_limit + ":>\n");}
                            }
                        }

                        if (arduinoMsg.contains("returned_current_dec_position")) {
                            String[] parts1 = arduinoMsg.split(":", 2);
                            String current_dec_position_str = parts1[1];
                            viewModel.set_dec_returned_current_position(Integer.parseInt(current_dec_position_str));
                        }

                        if (arduinoMsg.equals("auto_centering_done")) {
                            viewModel.set_auto_centering_done(true);
                        }

                        if (arduinoMsg.equals("cross_move_done")) {
                            viewModel.set_cross_move_done(true);
                        }
                        if (arduinoMsg.contains("cross_move_checkpoint")) {
                            String[] parts1 = arduinoMsg.split(":", 2);
                            String cross_move_checkpoint_str = parts1[1];
                            viewModel.set_cross_move_checkpoint(Integer.parseInt(cross_move_checkpoint_str));
                        }


                        break;

                    case CONNECTION_LOST:
                        toolbar.setSubtitle("Lost connection to " + deviceName);
                        connect_flag = CONNECTION_LOST;
                        invalidateOptionsMenu();
                        break;


                    case MESSAGE_WRITE:

                        String arduinoMsg2 = msg.obj.toString();
                        /*if the object is below the horizon dont send any command*/
                        if (connectedThread != null && connect_flag != CONNECTION_LOST && !arduinoMsg2.contains("Object is not visible as it is below the horizon")) {

                            //  Toast.makeText(MainActivity.this, "Command received" + arduinoMsg2, Toast.LENGTH_SHORT).show();
                            //send command to Arduino:
                            connectedThread.write(arduinoMsg2);
                        }
                        break;


                    case BLUETOOTH_DEVICE:


                        Bundle myBundle = (Bundle) msg.obj;
                        deviceName = myBundle.getString("deviceName");
                        deviceAddress = myBundle.getString("deviceAddress");
                        connect_to_selected_device(deviceName, deviceAddress);
                        BottomNavigationView bottonNav = findViewById(R.id.bottom_navigation);
                        bottonNav.setVisibility(VISIBLE);
                        break;

                    case OPEN_ALIGNMENT_SCREEN:
                        BottomNavigationView bottonNav2 = findViewById(R.id.bottom_navigation);
                        bottonNav2.setSelectedItemId(R.id.nav_align);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, alignmentFragment).addToBackStack(null).commit();
                        getSupportFragmentManager().popBackStack();
                        break;

                    case OPEN_CAMERA_SCREEN:
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, cameraFragment).addToBackStack(null).commit();
                        break;

                    case OPEN_POLAR_ALIGNMENT_SCREEN:
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, polarAlignmentFragment).addToBackStack(null).commit();
                        break;

                    case OPEN_FIX_BACKLASH_SCREEN:
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fixBacklashFragment).addToBackStack(null).commit();
                        break;

                    case OPEN_STAR_SUGGESTION_SCREEN:
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, alignmentStarsSuggestionFragment).addToBackStack(null).commit();
                        break;

                    case OPEN_HOME_SCREEN:
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).addToBackStack(null).commit();
                        break;

                    case OPEN_NEAREST_STAR_SUGGESTION_SCREEN:
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, nearestStarSuggestionFragment).addToBackStack(null).commit();
                        break;
                    case OPEN_AUTO_CENTER_SCREEN:
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, autoCenterFragment).addToBackStack(null).commit();
                        break;

                    case OPEN_CROSS_TEST_SCREEN:
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, crossTestFragment).addToBackStack(null).commit();
                        break;

                    default:
                        return;
                }
            }
        };


        viewModel.getMoving_status().observe(this, new Observer<String>() {
            double dec_to_goto;
            double ra_to_goto;

            @Override
            public void onChanged(String received_status) {
                if (viewModel.getStar_object().getValue() != null) {
                    ra_to_goto = viewModel.getRA_to_goto().getValue();
                    dec_to_goto = viewModel.getDEC_to_goto().getValue();
                    if (received_status.equals("motors stopped.") && viewModel.get_is_first_goto().getValue()) {
                        viewModel.set_first_goto(false);
                        tel.Goto(ra_to_goto, dec_to_goto, viewModel);
                        Toast.makeText(getApplicationContext(), "GOTO move completed", Toast.LENGTH_SHORT).show();
                        LocalTime ltime = LocalTime.now();
                        double local_time = ltime.getHour() + ltime.getMinute() / 60.0 + ltime.getSecond() / 3600.0;
                        viewModel.setGoto_time(local_time);
                    }
                    if (received_status.equals("move canceled")) {
                        Toast.makeText(getApplicationContext(), "move cancelled", Toast.LENGTH_SHORT).show();
                        viewModel.set_first_goto(false);
                    }
                }
            }
        });


        ((Button) findViewById(R.id.button_exit)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (createConnectThread != null) {
                    createConnectThread.cancel();
                }
                finish();


                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
                sleep(2000);
                System.exit(0);

            }
        });


        ((Button) findViewById(R.id.button_cancel_exit)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                findViewById(R.id.exit_frame).setVisibility(GONE);
                bottonNav.getMenu().setGroupEnabled(0, true);
                menu1.setGroupEnabled(0, true);
                findViewById(R.id.fragment_container).setVisibility(VISIBLE);
            }
        });


        IntentFilter filter2 = new IntentFilter((BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(myBroadcastReceiver, filter2);
        IntentFilter filter = new IntentFilter((BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(myBroadcastReceiver, filter);
        IntentFilter filter3 = new IntentFilter((BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(myBroadcastReceiver, filter3);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navlistener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.nav_align:

                            selectedFragment = alignmentFragment;
                            selected_fragment_str = "alignmentFragment";
                            break;
                        case R.id.nav_home:
                            selectedFragment = homeFragment;
                            selected_fragment_str = "homeFragment";
                            break;
                        case R.id.nav_manual_control:
                            selectedFragment = controlsFragment;
                            selected_fragment_str = "controlsFragment";
                            break;
                        case R.id.nav_select_object:
                            selectedFragment = selectObjectFragment;
                            selected_fragment_str = "selectObjectFragment";
                            break;
                    }
            /* get the top fragment ,which is the current one displayed,and don't make any changes
            if the selected fragment is the current one.change to another fragment if different from the current*/
                    Fragment topfragment = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
                    if (topfragment != selectedFragment) {
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).addToBackStack(selected_fragment_str).commit();
                    }
                    return true;
                }
            };


    private void connect_to_selected_device(String deviceName, String deviceAddress) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        if (deviceName != null) {
            toolbar.setSubtitle((CharSequence) "Connecting to " + deviceName + "...");
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }
    }

    public void saveBtDevice() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(MAC_ADDRESS, deviceAddress);
        editor.putString(BT_DEVICE, deviceName);
        editor.apply();
    }

    public void loadBtDevice() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        stored_address = sharedPreferences.getString(MAC_ADDRESS, "24:42:16:08:00:00");
        stored_device = sharedPreferences.getString(BT_DEVICE, "telescope");
        Toast.makeText(MainActivity.this, "MAC_address LOADED :" + stored_address, Toast.LENGTH_SHORT).show();
    }

    public void loadConfiguration() {
        viewModel.setRA_motor_steps(sharedPreferences.getInt("ra_motor_steps", 200));
        viewModel.setDEC_motor_steps(sharedPreferences.getInt("dec_motor_steps", 200));
        viewModel.setRA_gear_teeth(sharedPreferences.getInt("ra_gear_teeth", 130));
        viewModel.setDEC_gear_teeth(sharedPreferences.getInt("dec_gear_teeth", 65));
        viewModel.setRA_mount_pulley(sharedPreferences.getInt("ra_mount_pulley", 40));
        viewModel.setDEC_mount_pulley(sharedPreferences.getInt("dec_mount_pulley", 40));
        viewModel.setRA_motor_pulley(sharedPreferences.getInt("ra_motor_pulley", 16));
        viewModel.setDEC_motor_pulley(sharedPreferences.getInt("dec_motor_pulley", 16));
        viewModel.setRASpeed(sharedPreferences.getInt("ra_speed", 3500));
        viewModel.setDECSpeed(sharedPreferences.getInt("dec_speed", 2500));
        viewModel.set_is_ra_motor_inverted(sharedPreferences.getBoolean("is_ra_motor_inverted", true));
        viewModel.set_is_dec_motor_inverted(sharedPreferences.getBoolean("is_dec_motor_inverted", false));
        viewModel.setLatitude((double) sharedPreferences.getFloat("latitude", 38.2f));
        viewModel.setLongitude((double) sharedPreferences.getFloat("longitude", 21.2f));
        String command_to_send = sharedPreferences.getString("config_command", "\n");
        viewModel.setConfig_command(command_to_send);
        if (command_to_send.equals("\n")) {
            Toast.makeText(MainActivity.this, "Please ,select Settings -> Mount Configuration to configure the mount. ", Toast.LENGTH_LONG).show();
            Toast.makeText(MainActivity.this, "Please ,select Settings -> Mount Configuration to configure the mount. ", Toast.LENGTH_LONG).show();
        }
        if (!command_to_send.equals("\n")) {
            Toast.makeText(MainActivity.this, "config command :\n" + command_to_send, Toast.LENGTH_SHORT).show();
        }
        connectedThread.write(command_to_send);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
        //viewModel.setHipHashMap(null);
        //viewModel=null;
    }


    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("connect_flag", this.connect_flag);
    }

    public void onBackPressed() {
        BottomNavigationView bottonNav = findViewById(R.id.bottom_navigation);
        bottonNav.setSelectedItemId(R.id.nav_home);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            if (!getSupportFragmentManager().getFragments().contains(cameraFragment) && !getSupportFragmentManager().getFragments().contains(alignmentStarsSuggestionFragment)
                    && !getSupportFragmentManager().getFragments().contains(polarAlignmentFragment)) {
                getSupportFragmentManager().popBackStack();
            }
            if (getSupportFragmentManager().getFragments().contains(cameraFragment)) {
                bottonNav.setSelectedItemId(R.id.nav_manual_control);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, controlsFragment).addToBackStack(null).commit();
                getSupportFragmentManager().popBackStackImmediate();
            }
            if (getSupportFragmentManager().getFragments().contains(alignmentStarsSuggestionFragment)) {
                bottonNav.setSelectedItemId(R.id.nav_align);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, alignmentFragment).addToBackStack(null).commit();
                getSupportFragmentManager().popBackStackImmediate();
            }
            if (getSupportFragmentManager().getFragments().contains(polarAlignmentFragment)) {
                bottonNav.setSelectedItemId(R.id.nav_align);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, alignmentFragment).addToBackStack(null).commit();
                getSupportFragmentManager().popBackStackImmediate();
            }
            if (getSupportFragmentManager().getFragments().contains(fixBacklashFragment)) {
                bottonNav.setSelectedItemId(R.id.nav_manual_control);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, controlsFragment).addToBackStack(null).commit();
                getSupportFragmentManager().popBackStackImmediate();
            }

            if (getSupportFragmentManager().getFragments().contains(autoCenterFragment)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, cameraFragment).addToBackStack(null).commit();
            }
            if (getSupportFragmentManager().getFragments().contains(crossTestFragment)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fixBacklashFragment).addToBackStack(null).commit();
            }

            if (bottonNav.getVisibility() == GONE) {
                bottonNav.setVisibility(VISIBLE);

            }
            return;
        }
        findViewById(R.id.exit_frame).setVisibility(VISIBLE);
        bottonNav.getMenu().setGroupEnabled(0, false);
        this.menu1.setGroupEnabled(0, false);
        findViewById(R.id.fragment_container).setVisibility(INVISIBLE);
    }


    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e("TAG", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e("TAG", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("TAG", "Could not close the client socket", e);
            }
        }
    }


    /* =============================== Thread for Data Transfer =========================================== */
    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes - 1);
                        Log.e("Arduino Message", readMessage);
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error", "Unable to send message", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


}
