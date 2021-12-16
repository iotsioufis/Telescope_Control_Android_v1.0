package com.my_project.telescopecontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import static android.view.View.VISIBLE;
import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.OPEN_ALIGNMENT_SCREEN;
import static com.my_project.telescopecontrol.MainActivity.OPEN_AUTO_CENTER_SCREEN;
import static com.my_project.telescopecontrol.MainActivity.handler;

import static java.lang.Math.abs;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback {

    private final int CAMERA_REQUEST_CODE = 345;
    double DEC_micro_1;
    double RA_micro_1;
    private TextView add_textview;
    private ConstraintLayout add_to_alignment_frame;
    double alignment_time;
    private MaterialButton button_add_point;
    private MaterialButton button_cancel_add_point;
    private Camera camera;
    private SurfaceHolder camera_surface_holder;
    private SurfaceView camera_surface_v;
    private MaterialButton close_camera;
    private MaterialButton button_open_auto_centering;
    double dec;
    int dec_offset;
    double dec_transformed;
    private View divider_camera_x1;
    private View divider_camera_x2;
    private View divider_camera_y1;
    private View divider_camera_y2;
    private MaterialButton down;
    private int down_times_pressed = 0;
    SharedPreferences.Editor editor;
    private FrameLayout frame_x;
    private FrameLayout frame_y;
    double goto_time;
    double ha_degrees;
    double initial_time;
    private MaterialButton invert_dec_tags;
    private MaterialButton invert_ra_tags;
    private boolean is_dec_inverted = false;
    private boolean is_ra_inverted = false;
    private String last_pressed_str = "";
    private MaterialButton left;
    private int left_times_pressed = 0;
    double ra;
    int ra_offset;
    private MaterialButton right;
    private int right_times_pressed = 0;
    private TextView selected_object_name;
    SharedPreferences sharedPreferences;
    String side_of_meridian;
    private Slider slider_camera_zoom;
    private Slider slider_cross_gap;
    private Slider slider_cross_rotate;
    private Slider slider_cross_width;
    private MaterialButton star_centered;
    private MaterialButton stop;
    private String temp_tag_str;
    private TextView text_dec_minus;
    private TextView text_dec_plus;
    private TextView text_ra_minus;
    private TextView text_ra_plus;
    private MaterialButton up;
    private int up_times_pressed = 0;
    private SharedViewModel viewModel;
    private int zoom_level;



    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final NestedScrollView nestedScrollview = (NestedScrollView) ((RelativeLayout) getActivity().findViewById(R.id.relative)).getRootView().findViewById(R.id.scrollview);
        sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFS, 0);
        editor = sharedPreferences.edit();
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        final ExtraAlignments extraAlignments = new ExtraAlignments(getContext(), viewModel);
        View v = inflater.inflate(R.layout.camera, container, false);
        BottomNavigationView bottomNav = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(GONE);
        up = v.findViewById(R.id.button_up_camera);
        down = v.findViewById(R.id.button_down_camera);
        left = v.findViewById(R.id.button_left_camera);
        right = v.findViewById(R.id.button_right_camera);
        stop = v.findViewById(R.id.button_stop_camera);
        close_camera = v.findViewById(R.id.button_open_close_camera);
        button_open_auto_centering=v.findViewById(R.id.button_open_auto_centering);
        invert_ra_tags = v.findViewById(R.id.button_invert_ra);
        invert_dec_tags = v.findViewById(R.id.button_invert_dec);
        selected_object_name = v.findViewById(R.id.text_object_selected);
        if (viewModel.getStar_object().getValue() == null) {
            selected_object_name.setText("");
        }
        slider_cross_gap = v.findViewById(R.id.slider_cross);
        slider_cross_width = v.findViewById(R.id.slider_cross_width);
        slider_cross_rotate = v.findViewById(R.id.slider_cross_rotate);
        slider_camera_zoom = v.findViewById(R.id.slider_camera_zoom);
        divider_camera_x1 = v.findViewById(R.id.divider_camera_x1);
        divider_camera_x2 = v.findViewById(R.id.divider_camera_x2);
        divider_camera_y1 = v.findViewById(R.id.divider_camera_y1);
        divider_camera_y2 = v.findViewById(R.id.divider_camera_y2);
        text_dec_minus = v.findViewById(R.id.text_dec_minus);
        text_dec_plus = v.findViewById(R.id.text_dec_plus);
        text_ra_plus = v.findViewById(R.id.text_ra_plus);
        text_ra_minus = v.findViewById(R.id.text_ra_minus);
        frame_x = v.findViewById(R.id.frame_x);
        frame_y = v.findViewById(R.id.frame_y);
        add_to_alignment_frame = (ConstraintLayout) v.findViewById(R.id.frame_add_to_alignment);
        button_cancel_add_point = v.findViewById(R.id.button_cancel_add_to_alignment);
        button_add_point = v.findViewById(R.id.button_add_to_alignment);
        add_textview = v.findViewById(R.id.text_add_to_alignment);
        star_centered = v.findViewById(R.id.button_for_alignment_camera);
        SurfaceView surfaceView = (SurfaceView) v.findViewById(R.id.surfaceView_camera);
        camera_surface_v = surfaceView;
        camera_surface_holder = surfaceView.getHolder();
        camera_surface_v.getHolder().setFixedSize((int) (getResources().getDisplayMetrics().widthPixels), (int) (getResources().getDisplayMetrics().heightPixels / 1.7));
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            camera_surface_holder.addCallback(this);

        }



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

        viewModel.getStar_object().observe(getViewLifecycleOwner(), new Observer<star>() {
            public void onChanged(star star) {
                viewModel.getAlignment_time().observe(getViewLifecycleOwner(), new Observer<Double>() {
                    public void onChanged(Double alignment_time_from_main) {
                        final String name = viewModel.getStar_object().getValue().getName_ascii();
                        goto_time = viewModel.getGoto_time().getValue();
                        alignment_time = alignment_time_from_main;
                        initial_time = viewModel.getInitial_time().getValue();
                        ra_offset = viewModel.getRA_offset().getValue();
                        dec_offset = viewModel.getDEC_offset().getValue();
                        side_of_meridian = viewModel.getSide_of_meridian().getValue();
                        RA_micro_1 = viewModel.getRA_micro_1().getValue();
                        DEC_micro_1 = viewModel.getDEC_micro_1().getValue();
                        ha_degrees = viewModel.getHA_degrees().getValue();
                        ra = viewModel.getRA_decimal().getValue();
                        dec = viewModel.getDEC_decimal().getValue();
                        dec_transformed = viewModel.getDEC_decimal_transformed().getValue();
                        if (viewModel.getStar_object().getValue() != null) {
                            selected_object_name.setText("Selected :\n" + viewModel.getStar_object().getValue().getName_ascii());
                        }
                        Log.e(TAG, "Name : " + name);
                        Log.e(TAG, "ra : " + ra);
                        Log.e(TAG, "ra_offset is : " + ra_offset);
                        Log.e(TAG, "dec is : " + dec);
                        Log.e(TAG, "dec_offset is : " + dec_offset);
                        Log.e(TAG, "ha_degrees is : " + ha_degrees);
                        Log.e(TAG, "initial time is : " + initial_time);
                        Log.e(TAG, "alignment time is : " + alignment_time);
                        Log.e(TAG, "RA_micro_1 is : " + RA_micro_1);
                        Log.e(TAG, "DEC_micro_1 is : " + DEC_micro_1);
                        Log.e(TAG, "side_of_meridian is : " + side_of_meridian);

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

        button_open_auto_centering.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                handler.obtainMessage(OPEN_AUTO_CENTER_SCREEN, -1, -1).sendToTarget();
            }
        });

        star_centered.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LocalTime ltime = LocalTime.now();
                double local_time = ltime.getHour() + ltime.getMinute() / 60.0 + ltime.getSecond() / 3600.0;
                viewModel.setAlignment_time(local_time);
                viewModel.setTracking_status(false);
                editor.putBoolean("tracking_is_on", false);
                editor.apply();
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (!viewModel.get_alignment_done().getValue().booleanValue()) {
                    handler.obtainMessage(MESSAGE_WRITE, "<stop&send_offsets:>\n").sendToTarget();
                    handler.obtainMessage(OPEN_ALIGNMENT_SCREEN, -1, -1).sendToTarget();
                }
                if (viewModel.get_alignment_done().getValue().booleanValue()) {
                    handler.obtainMessage(MESSAGE_WRITE, "<stop&send_offsets:>\n").sendToTarget();
                    if (viewModel.getAlignmentpoints().getValue() != null) {
                        add_to_alignment_frame.setVisibility(VISIBLE);
                        add_textview.setVisibility(VISIBLE);
                    } else if (viewModel.getAlignmentpoints().getValue() == null) {
                        Toast.makeText(getContext(), "To add this object as an alignment point , first load n-star alignment data . ", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        button_cancel_add_point.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                add_to_alignment_frame.setVisibility(GONE);
            }
        });



        /*setOntouchListener used to block the touch events to reach the NestedScrollview of the parent activity .
         This is done because this (slider_cross_gap) slider is a vertical one , inside a scrollview and
         touch events must reach the slider, rather than the scrollview. */
        slider_cross_gap.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {

                {
                    nestedScrollview.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

           /*setOntouchListener used to block the touch events to reach the NestedScrollview of the parent activity .
         This is done because this (slider_cross_width) slider is a vertical one , inside a scrollview and
         touch events must reach the slider, rather than the scrollview. */

        slider_cross_width.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {

                {
                    nestedScrollview.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

         /*setOntouchListener used to block the touch events to reach the NestedScrollview of the parent activity .
         This is done because this (slider_cross_rotate) slider is a vertical one , inside a scrollview and
         touch events must reach the slider, rather than the scrollview. */

        slider_cross_rotate.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {

                {
                    nestedScrollview.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

			/*setOntouchListener used to block the touch events to reach the NestedScrollview of the parent activity .
         This is done because this (slider_cross_zoom) slider is a vertical one , inside a scrollview and
         touch events must reach the slider, rather than the scrollview. */
        slider_camera_zoom.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                nestedScrollview.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        invert_ra_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                /*reset tag texts :*/
                temp_tag_str = text_ra_plus.getText().toString();
                text_ra_plus.setText(text_ra_minus.getText().toString());
                text_ra_minus.setText(temp_tag_str);
                if (!is_ra_inverted) {
                    is_ra_inverted = true;
                } else if (is_ra_inverted) {
                    is_ra_inverted = false;
                }
                if (last_pressed_str.equals("ra+") && is_ra_inverted) {
                    text_ra_plus.setBackgroundResource(R.drawable.crosshair_tag);
                    text_ra_minus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                } else if (last_pressed_str.equals("ra-") && is_ra_inverted) {
                    text_ra_minus.setBackgroundResource(R.drawable.crosshair_tag);
                    text_ra_plus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                }
                if (last_pressed_str.equals("ra+") && !is_ra_inverted) {
                    text_ra_minus.setBackgroundResource(R.drawable.crosshair_tag);
                    text_ra_plus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                } else if (last_pressed_str.equals("ra-") && !is_ra_inverted) {
                    text_ra_plus.setBackgroundResource(R.drawable.crosshair_tag);
                    text_ra_minus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                }
            }
        });
        invert_dec_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                /*reset tag texts :*/
                temp_tag_str = text_dec_plus.getText().toString();
                text_dec_plus.setText(text_dec_minus.getText().toString());
                text_dec_minus.setText(temp_tag_str);

                if (!is_dec_inverted) {
                    is_dec_inverted = true;
                } else if (is_dec_inverted) {
                    is_dec_inverted = false;
                }
                if (last_pressed_str.equals("dec+") && is_dec_inverted) {
                    text_dec_plus.setBackgroundResource(R.drawable.crosshair_tag);
                    text_dec_minus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                } else if (last_pressed_str.equals("dec-") && is_dec_inverted) {
                    text_dec_minus.setBackgroundResource(R.drawable.crosshair_tag);
                    text_dec_plus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                }
                if (last_pressed_str.equals("dec+") && !is_dec_inverted) {
                    text_dec_minus.setBackgroundResource(R.drawable.crosshair_tag);
                    text_dec_plus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                } else if (last_pressed_str.equals("dec-") && !is_dec_inverted) {
                    text_dec_plus.setBackgroundResource(R.drawable.crosshair_tag);
                    text_dec_minus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                }


            }
        });


        down.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                reset_crosshair_tags();
                if (!is_ra_inverted) {
                    text_ra_minus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                } else if (is_ra_inverted) {
                    text_ra_plus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                }
                last_pressed_str = "ra-";
                up_times_pressed = 0;
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (down_times_pressed == 0) {
                    handler.obtainMessage(MESSAGE_WRITE, "<micro_move_ra-:>\n").sendToTarget();
                    down_times_pressed++;
                } else if (down_times_pressed >= 1 && down_times_pressed < 7 && up_times_pressed == 0) {
                    handler.obtainMessage(MESSAGE_WRITE, "<r+:>\n").sendToTarget();
                    down_times_pressed++;
                    viewModel.setTracking_status(false);
                    editor.putBoolean("tracking_is_on", false);
                    editor.apply();
                }
            }
        });


        up.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                reset_crosshair_tags();
                if (is_ra_inverted) {
                    text_ra_minus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                } else if (!is_ra_inverted) {
                    text_ra_plus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                }
                last_pressed_str = "ra+";
                down_times_pressed = 0;
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (up_times_pressed == 0) {
                    handler.obtainMessage(MESSAGE_WRITE, "<micro_move_ra+:>\n").sendToTarget();
                    up_times_pressed++;
                } else if (up_times_pressed >= 1 && up_times_pressed < 7 && down_times_pressed == 0) {
                    handler.obtainMessage(MESSAGE_WRITE, "<r+:>\n").sendToTarget();
                    up_times_pressed++;
                }
                viewModel.setTracking_status(false);
                editor.putBoolean("tracking_is_on", false);
                editor.apply();
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                reset_crosshair_tags();
                if (!is_dec_inverted) {
                    text_dec_minus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                } else if (is_dec_inverted) {
                    text_dec_plus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                }
                last_pressed_str = "dec-";
                right_times_pressed = 0;
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (left_times_pressed == 0) {
                    handler.obtainMessage(MESSAGE_WRITE, "<micro_move_dec-:>\n").sendToTarget();
                    left_times_pressed++;
                } else if (left_times_pressed >= 1 && left_times_pressed < 7 && right_times_pressed == 0) {
                    handler.obtainMessage(MESSAGE_WRITE, "<d+:>\n").sendToTarget();
                    left_times_pressed++;
                }
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                reset_crosshair_tags();
                if (is_dec_inverted) {
                    text_dec_minus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                } else if (!is_dec_inverted) {
                    text_dec_plus.setBackgroundResource(R.drawable.crosshair_tag_selected);
                }
                last_pressed_str = "dec+";
                left_times_pressed = 0;
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (right_times_pressed == 0) {
                    handler.obtainMessage(MESSAGE_WRITE, "<micro_move_dec+:>\n").sendToTarget();
                    right_times_pressed++;
                } else if (right_times_pressed >= 1 && right_times_pressed < 7 && left_times_pressed == 0) {
                    right_times_pressed++;
                    handler.obtainMessage(MESSAGE_WRITE, "<d+:>\n").sendToTarget();
                }

            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                reset_crosshair_tags();
                last_pressed_str = "stop";
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                left_times_pressed = 0;
                right_times_pressed = 0;
                up_times_pressed = 0;
                down_times_pressed = 0;
                handler.obtainMessage(MESSAGE_WRITE, "<stop:1:>\n").sendToTarget();
                viewModel.setTracking_status(false);
                editor.putBoolean("tracking_is_on", false);
                editor.apply();
            }
        });
        close_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset_crosshair_tags();
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                getActivity().onBackPressed();
            }
        });


        slider_cross_gap.addOnChangeListener(new Slider.OnChangeListener() {


            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {


                int value_in_dips = (int) ((getResources().getDisplayMetrics().density) * value);


                divider_camera_x1.setTranslationX(value_in_dips);
                divider_camera_x2.setTranslationX(-value_in_dips);
                divider_camera_y1.setTranslationX(value_in_dips);
                divider_camera_y2.setTranslationX(-value_in_dips);


            }

        });

        slider_cross_width.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                FrameLayout.LayoutParams params_x1 = (FrameLayout.LayoutParams) divider_camera_x1.getLayoutParams();
                FrameLayout.LayoutParams params_x2 = (FrameLayout.LayoutParams) divider_camera_x2.getLayoutParams();
                FrameLayout.LayoutParams params_y1 = (FrameLayout.LayoutParams) divider_camera_y1.getLayoutParams();
                FrameLayout.LayoutParams params_y2 = (FrameLayout.LayoutParams) divider_camera_y2.getLayoutParams();


                int value_in_dips = (int) ((getResources().getDisplayMetrics().density) * value);
                params_x1.width = (int) value_in_dips;
                params_x2.width = (int) value_in_dips;
                params_y1.width = (int) value_in_dips;
                params_y2.width = (int) value_in_dips;


                divider_camera_x1.requestLayout();
                divider_camera_x2.requestLayout();
                divider_camera_y1.requestLayout();
                divider_camera_y2.requestLayout();


            }

        });


        slider_cross_rotate.addOnChangeListener(new Slider.OnChangeListener() {
            float previous_value = 0;

            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                previous_value = value;
                frame_x.setRotation(90 + value);
                frame_y.setRotation(value);


            }

        });

        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Camera open = Camera.open();
        camera = open;
        Camera.Parameters parameters = open.getParameters();
        camera.setDisplayOrientation(90);
        int dif = frame_x.getHeight() - frame_x.getWidth();
        int value_in_dips = (int) (dif / (getResources().getDisplayMetrics().density));
        text_ra_plus.setTranslationY((float) value_in_dips);
        text_ra_minus.setTranslationY((float) (-value_in_dips));
        text_dec_plus.setTranslationY((float) value_in_dips);
        text_dec_minus.setTranslationY((float) (-value_in_dips));

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        try {
            camera.setPreviewDisplay(camera_surface_holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        parameters.setZoom(zoom_level);
        camera.setParameters(parameters);
        slider_camera_zoom.addOnChangeListener(new Slider.OnChangeListener() {
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                zoom_level = (int) ((((float) parameters.getMaxZoom()) * value) / 10);
                Camera.Parameters parameters = camera.getParameters();
                parameters.setZoom(zoom_level);
                camera.setParameters(parameters);
            }
        });


    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void reset_crosshair_tags() {

        text_dec_plus.setBackgroundResource(R.drawable.crosshair_tag);
        text_dec_minus.setBackgroundResource(R.drawable.crosshair_tag);
        text_ra_plus.setBackgroundResource(R.drawable.crosshair_tag);
        text_ra_minus.setBackgroundResource(R.drawable.crosshair_tag);
    }

}