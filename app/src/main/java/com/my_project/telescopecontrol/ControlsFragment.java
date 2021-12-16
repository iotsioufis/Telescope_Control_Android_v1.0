package com.my_project.telescopecontrol;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.time.LocalTime;

import static android.content.Context.MODE_PRIVATE;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.OPEN_ALIGNMENT_SCREEN;
import static com.my_project.telescopecontrol.MainActivity.OPEN_AUTO_CENTER_SCREEN;
import static com.my_project.telescopecontrol.MainActivity.OPEN_CAMERA_SCREEN;
import static com.my_project.telescopecontrol.MainActivity.OPEN_FIX_BACKLASH_SCREEN;
import static com.my_project.telescopecontrol.MainActivity.SHARED_PREFS;
import static com.my_project.telescopecontrol.MainActivity.handler;

public class ControlsFragment extends Fragment {

    private MaterialButton up;
    private MaterialButton down;
    private MaterialButton left;
    private MaterialButton right;
    private MaterialButton stop;
    private MaterialButton star_centered;
    private MaterialButton camera_utility;
    private MaterialButton fix_backlash;

    private Slider slider_ra_speed;
    private float previous_ra_speed;
    private Slider slider_dec_speed;
    private float previous_dec_speed;

    private SharedViewModel viewModel;
    private int right_times_pressed = 0;
    private int left_times_pressed = 0;
    private int up_times_pressed = 0;
    private int down_times_pressed = 0;
    private CheckBox check_motor_directions;

    double DEC_micro_1;
    double RA_micro_1;
    private TextView add_textview;
    private ConstraintLayout add_to_alignment_frame;
    double alignment_time;
    private MaterialButton button_add_point;
    private MaterialButton button_cancel_add_point;
    double dec;
    int dec_offset;
    double dec_transformed;
    SharedPreferences.Editor editor;
    double goto_time;
    double ha_degrees;
    double initial_time;
    double ra;
    int ra_offset;
    private MaterialButton return_to_initial;
    SharedPreferences sharedPreferences;
    String side_of_meridian;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        BottomNavigationView bottomNav = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.getMenu().getItem(2).setChecked(true);
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        View v = inflater.inflate(R.layout.controls, container, false);
        ExtraAlignments extraAlignments = new ExtraAlignments(getContext(), viewModel);
        up = v.findViewById(R.id.button_up);
        down = v.findViewById(R.id.button_down);
        left = v.findViewById(R.id.button_left);
        right = v.findViewById(R.id.button_right);
        stop = v.findViewById(R.id.button_stop);
        slider_ra_speed = v.findViewById(R.id.slider_ra);
        slider_dec_speed = v.findViewById(R.id.slider_dec);
        return_to_initial = v.findViewById(R.id.button_goto_initial);
        star_centered = v.findViewById(R.id.button_for_alignment);
        camera_utility = v.findViewById(R.id.button_camera);
        fix_backlash = v.findViewById(R.id.button_backlash_fix);
        add_to_alignment_frame = v.findViewById(R.id.frame_add_to_alignment_controls);
        button_cancel_add_point = v.findViewById(R.id.button_cancel_add_to_alignment_controls);
        button_add_point = v.findViewById(R.id.button_add_to_alignment_controls);
        add_textview = v.findViewById(R.id.text_add_to_alignment_controls);
        check_motor_directions = v.findViewById(R.id.checkBox_ending_directions);
        previous_ra_speed = 0;
        previous_dec_speed = 0;

        return_to_initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);

                previous_ra_speed = 0;
                slider_ra_speed.setValue(0);
                previous_dec_speed = 0;
                slider_dec_speed.setValue(0);
                viewModel.setTracking_status(false);
                editor.putBoolean("tracking_is_on", false);
                editor.apply();
                handler.obtainMessage(MESSAGE_WRITE, "<move_with_b_lash:RA:0;DEC:0:1:u;>").sendToTarget();


            }
        });


        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                up_times_pressed = 0;
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (down_times_pressed == 0) {

                    handler.obtainMessage(MESSAGE_WRITE, "<micro_move_ra-:>").sendToTarget();
                    previous_dec_speed = 0;
                    slider_ra_speed.setValue(1);

                    down_times_pressed++;
                    previous_ra_speed = 0;
                } else if (down_times_pressed >= 1 && down_times_pressed < 7 && up_times_pressed == 0) {

                    handler.obtainMessage(MESSAGE_WRITE, "<r+:>").sendToTarget();
                    down_times_pressed++;
                    slider_ra_speed.setValue(down_times_pressed);
                }
                viewModel.setTracking_status(false);
                editor.putBoolean("tracking_is_on", false);
                editor.apply();

            }
        });


        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                down_times_pressed = 0;
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (up_times_pressed == 0) {

                    handler.obtainMessage(MESSAGE_WRITE, "<micro_move_ra+:>").sendToTarget();
                    previous_dec_speed = 0;
                    slider_ra_speed.setValue(1);

                    up_times_pressed++;
                    previous_ra_speed = 0;
                } else if (up_times_pressed >= 1 && up_times_pressed < 7 && down_times_pressed == 0) {

                    handler.obtainMessage(MESSAGE_WRITE, "<r+:>").sendToTarget();
                    up_times_pressed++;
                    slider_ra_speed.setValue(up_times_pressed);
                }
                viewModel.setTracking_status(false);
                editor.putBoolean("tracking_is_on", false);
                editor.apply();

            }
        });


        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                right_times_pressed = 0;
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (left_times_pressed == 0) {

                    handler.obtainMessage(MESSAGE_WRITE, "<micro_move_dec-:>").sendToTarget();
                    previous_ra_speed = 0;
                    slider_dec_speed.setValue(1);

                    left_times_pressed++;
                    previous_dec_speed = 0;
                } else if (left_times_pressed >= 1 && left_times_pressed < 7 && right_times_pressed == 0) {

                    handler.obtainMessage(MESSAGE_WRITE, "<d+:>").sendToTarget();
                    left_times_pressed++;
                    slider_dec_speed.setValue(left_times_pressed);
                }


            }
        });


        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                left_times_pressed = 0;

                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (right_times_pressed == 0) {

                    handler.obtainMessage(MESSAGE_WRITE, "<micro_move_dec+:>").sendToTarget();
                    previous_ra_speed = 0;
                    slider_dec_speed.setValue(1);
                    right_times_pressed = right_times_pressed + 1;
                    previous_dec_speed = 0;


                } else if (right_times_pressed >= 1 && right_times_pressed < 7 && left_times_pressed == 0) {
                    right_times_pressed++;
                    handler.obtainMessage(MESSAGE_WRITE, "<d+:>").sendToTarget();
                    slider_dec_speed.setValue(right_times_pressed);

                }
                viewModel.setTracking_status(false);
                editor.putBoolean("tracking_is_on", false);
                editor.apply();


            }

        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                left_times_pressed = 0;
                right_times_pressed = 0;
                up_times_pressed = 0;
                down_times_pressed = 0;
                handler.obtainMessage(MESSAGE_WRITE, "<stop:1:>").sendToTarget();
                previous_ra_speed = 0;
                slider_ra_speed.setValue(0);
                previous_dec_speed = 0;
                slider_dec_speed.setValue(0);
                viewModel.setTracking_status(false);
                editor.putBoolean("tracking_is_on", false);
                editor.apply();

            }
        });

        viewModel.getStar_object().observe(getViewLifecycleOwner(), new Observer<star>() {
            public void onChanged(star star) {
                viewModel.getAlignment_time().observe(getViewLifecycleOwner(), new Observer<Double>() {
                    public void onChanged(Double alignment_time_from_main) {
                        final String name = viewModel.getStar_object().getValue().getName_ascii();
                        goto_time = viewModel.getGoto_time().getValue();
                        alignment_time = alignment_time_from_main;
                        initial_time = viewModel.getInitial_time().getValue().doubleValue();
                        ra_offset = viewModel.getRA_offset().getValue().intValue();
                        dec_offset = viewModel.getDEC_offset().getValue().intValue();
                        side_of_meridian = viewModel.getSide_of_meridian().getValue();
                        RA_micro_1 = viewModel.getRA_micro_1().getValue().doubleValue();
                        DEC_micro_1 = viewModel.getDEC_micro_1().getValue().doubleValue();
                        ha_degrees = viewModel.getHA_degrees().getValue().doubleValue();
                        ra = viewModel.getRA_decimal().getValue().doubleValue();
                        dec = viewModel.getDEC_decimal().getValue().doubleValue();
                        dec_transformed = viewModel.getDEC_decimal_transformed().getValue().doubleValue();
                        Log.e("TAG", "Name : " + name);
                        Log.e("TAG", "ra : " + ra);
                        Log.e("TAG", "ra_offset is : " + ra_offset);
                        Log.e("TAG", "dec is : " + dec);
                        Log.e("TAG", "dec_offset is : " + dec_offset);
                        Log.e("TAG", "ha_degrees is : " + ha_degrees);
                        Log.e("TAG", "initial time is : " + initial_time);
                        Log.e("TAG", "alignment time is : " + alignment_time);
                        Log.e("TAG", "RA_micro_1 is : " + RA_micro_1);
                        Log.e("TAG", "DEC_micro_1 is : " + DEC_micro_1);
                        Log.e("TAG", "side_of_meridian is : " + side_of_meridian);

                        button_add_point.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                                  extraAlignments.add_point_to_alignment(name, ra, ha_degrees, ra_offset, dec, dec_transformed, dec_offset, alignment_time);
                                        add_to_alignment_frame.setVisibility(View.GONE);
                                        add_textview.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                });
            }
        });



        star_centered.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LocalTime ltime = LocalTime.now();
                double local_time = ltime.getHour() + ltime.getMinute() / 60.0 + ltime.getSecond() / 3600.0;
                viewModel.setAlignment_time(local_time);
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (!viewModel.get_alignment_done().getValue()) {
                    handler.obtainMessage(MESSAGE_WRITE, "<stop&send_offsets:>").sendToTarget();
                    handler.obtainMessage(OPEN_ALIGNMENT_SCREEN, -1, -1).sendToTarget();
                }
                if (viewModel.get_alignment_done().getValue()) {
                    handler.obtainMessage(MESSAGE_WRITE, "<stop&send_offsets:>").sendToTarget();
                    if (viewModel.getAlignmentpoints().getValue() != null) {
                        add_to_alignment_frame.setVisibility(View.VISIBLE);
                        add_textview.setVisibility(View.VISIBLE);
                    } else if (viewModel.getAlignmentpoints().getValue() == null) {
                        Toast.makeText(getContext(), "To add this object as an alignment point , first load n-star alignment data . ", Toast.LENGTH_SHORT).show();
                    }
                }

                viewModel.setTracking_status(false);
                editor.putBoolean("tracking_is_on", false);
                editor.apply();
            }
        });


        camera_utility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);

                handler.obtainMessage(OPEN_CAMERA_SCREEN, -1, -1).sendToTarget();
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            }
        });

        button_cancel_add_point.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                add_to_alignment_frame.setVisibility(View.GONE);
            }
        });

        fix_backlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);

                handler.obtainMessage(OPEN_FIX_BACKLASH_SCREEN, -1, -1).sendToTarget();


            }
        });


        slider_dec_speed.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (value > previous_dec_speed) {
                    v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                    handler.obtainMessage(MESSAGE_WRITE, "<d+:>").sendToTarget();

                }
                if (value < previous_dec_speed) {
                    v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                    handler.obtainMessage(MESSAGE_WRITE, "<d-:>").sendToTarget();
                }
                previous_dec_speed = value;

                if (left_times_pressed > 0 && right_times_pressed == 0) {
                    left_times_pressed = (int) value;
                }

                if (right_times_pressed > 0 && left_times_pressed == 0) {
                    right_times_pressed = (int) value;

                }

            }

        });


        slider_ra_speed.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {

                if (value > previous_ra_speed) {
                    v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                    handler.obtainMessage(MESSAGE_WRITE, "<r+:>").sendToTarget();

                }
                if (value < previous_ra_speed) {
                    v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                    handler.obtainMessage(MESSAGE_WRITE, "<r-:>").sendToTarget();
                }
                previous_ra_speed = value;
                //Use the value

                if (up_times_pressed > 0 && down_times_pressed == 0) {
                    up_times_pressed = (int) value;
                }

                if (down_times_pressed > 0 && up_times_pressed == 0) {
                    down_times_pressed = (int) value;

                }

            }

        });

        viewModel.get_ra_goto_ending_direction().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer ra_dir) {
                if(ra_dir==1 && viewModel.get_check_motors_direction().getValue()){
                    up.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorGotoStopDirection)));
                    down.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));}
                if(ra_dir==-1 && viewModel.get_check_motors_direction().getValue()){
                    down.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorGotoStopDirection)));
                    up.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));}

            }
        });

        viewModel.get_dec_goto_ending_direction().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer dec_dir) {
                if(dec_dir==1 && viewModel.get_check_motors_direction().getValue()){
                    right.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorGotoStopDirection)));
                    left.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));}
                if(dec_dir==-1 && viewModel.get_check_motors_direction().getValue()){
                    left.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorGotoStopDirection)));
                    right.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));}
            }
        });

check_motor_directions.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {

        if (check_motor_directions.isChecked()) {
            viewModel.set_check_motors_direction(true);
            handler.obtainMessage(MESSAGE_WRITE, "<get_motor_directions:>").sendToTarget();
            if (viewModel.get_ra_goto_ending_direction().getValue() == 1) {
                up.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorGotoStopDirection)));
                down.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));
            }
            if (viewModel.get_ra_goto_ending_direction().getValue() == -1) {
                down.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorGotoStopDirection)));
                up.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));
            }

            if (viewModel.get_dec_goto_ending_direction().getValue() == 1) {
                right.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorGotoStopDirection)));
                left.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));
            }
            if (viewModel.get_dec_goto_ending_direction().getValue() == -1) {
                left.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorGotoStopDirection)));
                right.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));
            }
        }

        if (!check_motor_directions.isChecked()) {
            viewModel.set_check_motors_direction(false);
            up.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));
            down.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));
            right.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));
            left.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorHighlighted)));
        }
    }
});


        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


}
