package com.my_project.telescopecontrol;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.OPEN_AUTO_CENTER_SCREEN;
import static com.my_project.telescopecontrol.MainActivity.OPEN_CROSS_TEST_SCREEN;
import static com.my_project.telescopecontrol.MainActivity.SHARED_PREFS;
import static com.my_project.telescopecontrol.MainActivity.handler;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FixBacklashFragment extends CameraFragment {
    private final int CAMERA_REQUEST_CODE = 345;
    private ConstraintLayout backlash_help_frame;
    private Camera camera;
    private SurfaceHolder camera_surface_holder;
    private SurfaceView camera_surface_v;
    private MaterialButton close_help;
    private MaterialButton close_help_corner;
    private MaterialButton cross_test;

    private MaterialButton add_point_fixes;
    private MaterialButton delete_last_point_fixes;
    private MaterialButton delete_all_point_fixes;
    private View divider_camera_x1;
    private View divider_camera_x2;
    private View divider_camera_y1;
    private View divider_camera_y2;
    private MaterialButton down;
    private int down_times_pressed = 0;
    private EditText edit_steps;
    SharedPreferences.Editor editor;
    private MaterialButton left;
    private int left_times_pressed = 0;
    private MaterialButton open_close_camera;
    private RadioButton radioButton;
    private int radioId;
    private RadioGroup radio_group_axis;
    private MaterialButton right;
    private int right_times_pressed = 0;
    SharedPreferences sharedPreferences;
    private MaterialButton show_help;
    private Slider slider_camera_zoom;
    private MaterialButton stop;
    private MaterialButton test_fix;
    private int test_steps_ra = 0;
    private int test_steps_dec = 0;
    private TextView text_selected_object_name;
    private TextView text_side_of_meridian;
    private TextView text_zoom_level;
    private MaterialButton up;
    private int up_times_pressed = 0;
    private SharedViewModel viewModel;
    private int zoom_level;
    BacklashFixesPoint backlashFixesPoint;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout relativeLayout = getActivity().findViewById(R.id.relative);
        NestedScrollView nestedScrollview = relativeLayout.getRootView().findViewById(R.id.scrollview);
        BottomNavigationView bottomNav = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);

        bottomNav.getMenu().getItem(2).setChecked(true);

        viewModel = (SharedViewModel) new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        final View v = inflater.inflate(R.layout.fix_backlash, container, false);

        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        up = v.findViewById(R.id.button_up_camera);
        down = v.findViewById(R.id.button_down_camera);
        left = v.findViewById(R.id.button_left_camera);
        right = v.findViewById(R.id.button_right_camera);
        stop = v.findViewById(R.id.button_stop_camera);
        open_close_camera = v.findViewById(R.id.button_open_close_camera);
        cross_test = v.findViewById(R.id.button_cross_test);
        divider_camera_x1 = v.findViewById(R.id.divider_camera_x1);
        divider_camera_x2 = v.findViewById(R.id.divider_camera_x2);
        divider_camera_y1 = v.findViewById(R.id.divider_camera_y1);
        divider_camera_y2 = v.findViewById(R.id.divider_camera_y2);
        radio_group_axis = v.findViewById(R.id.radio_group_axis);
        add_point_fixes=v.findViewById(R.id.button_add_fix);
        delete_last_point_fixes=v.findViewById(R.id.button_clear_last_fix);
        delete_all_point_fixes=v.findViewById(R.id.button_clear_all_fixes);
        edit_steps = v.findViewById(R.id.editText_backlash_steps);
        test_fix = v.findViewById(R.id.button_test_fix);
        backlash_help_frame = v.findViewById(R.id.frame_fix_backlash_info);
        show_help = v.findViewById(R.id.button_show_backlash_help);
        close_help = v.findViewById(R.id.button_hide_backlash_help);
        close_help_corner = v.findViewById(R.id.button_close_backlash_help);
        slider_camera_zoom = v.findViewById(R.id.slider_camera_zoom_backlash);
        text_zoom_level = v.findViewById(R.id.text_zoom_level);
        text_selected_object_name = v.findViewById(R.id.text_object_selected_backlash);
        text_side_of_meridian = v.findViewById(R.id.text_east_or_west_backlash);
        SurfaceView surfaceView = v.findViewById(R.id.surfaceView_camera);
        camera_surface_v = v.findViewById(R.id.surfaceView_camera);
        camera_surface_holder = camera_surface_v.getHolder();
        camera_surface_holder.addCallback(this);
        camera_surface_v.getHolder().setFixedSize((int) (getResources().getDisplayMetrics().widthPixels), (int) (getResources().getDisplayMetrics().heightPixels / 3));
        camera_surface_v.setVisibility(INVISIBLE);
        divider_camera_x1.setVisibility(INVISIBLE);
        divider_camera_x2.setVisibility(INVISIBLE);
        divider_camera_y1.setVisibility(INVISIBLE);
        divider_camera_y2.setVisibility(INVISIBLE);

        slider_camera_zoom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                nestedScrollview.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


        radio_group_axis.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioId = radio_group_axis.getCheckedRadioButtonId();
                radioButton = v.findViewById(radioId);
                String radioText = radioButton.getText().toString();

                switch (radioText) {
                    case "Test RA \n   Axis   ":
                        test_fix.setText("Check RA \n fix");

                        break;
                    case "Test DEC\n    Axis":
                        test_fix.setText("Check DEC \n fix");

                        break;

                }


            }
        });


        test_fix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                if (test_fix.getText().equals("Check RA \n fix") && !edit_steps.getText().toString().equals("")) {
                    test_steps_ra = -Integer.parseInt(edit_steps.getText().toString());
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<b_lash_ra:" + test_steps_ra  + ";>\n").sendToTarget();
                }


                if (test_fix.getText().equals("Check DEC \n fix") && !edit_steps.getText().toString().equals("")) {
                    test_steps_dec = -Integer.parseInt(edit_steps.getText().toString());
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<b_lash_dec:" + test_steps_dec + ";>\n").sendToTarget();
                }
            }
        });


        show_help.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                backlash_help_frame.setVisibility(View.VISIBLE);

            }
        });


        close_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                backlash_help_frame.setVisibility(GONE);
            }
        });


        close_help_corner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                backlash_help_frame.setVisibility(GONE);
            }
        });


         add_point_fixes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                backlashFixesPoint=new BacklashFixesPoint(viewModel.getHA_degrees().getValue(),viewModel.getDEC_decimal().getValue(),test_steps_ra,test_steps_dec,viewModel.getSide_of_meridian().getValue());
                if(viewModel.getBacklashfixes().getValue()!=null){
                    viewModel.getBacklashfixes().getValue().add(backlashFixesPoint);
                }
                if(viewModel.getBacklashfixes().getValue()==null){
                    ArrayList<BacklashFixesPoint> tempt_array_list =new ArrayList<BacklashFixesPoint>();
                    tempt_array_list.add(backlashFixesPoint);
                    viewModel.setBacklashfixes(tempt_array_list);
                }

                viewModel.set_ra_backlash_fix(test_steps_ra);
                viewModel.set_dec_backlash_fix(test_steps_dec);

                save_fixes();
                Toast.makeText(getContext(), "Backlash fixes for this point saved ", Toast.LENGTH_SHORT).show();

                handler.obtainMessage(MESSAGE_WRITE, "<update_current_bl_fixes:" + test_steps_ra+ ":" + test_steps_dec + ":>\n").sendToTarget();

            }
        });


          delete_last_point_fixes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

                if(viewModel.getBacklashfixes().getValue()!=null) {
                    if(viewModel.getBacklashfixes().getValue().size()==0){
                        Toast.makeText(getContext(), "No backlash fixes were found for any point ", Toast.LENGTH_SHORT).show();
                    }
                    if (viewModel.getBacklashfixes().getValue().size() > 0) {
                        viewModel.getBacklashfixes().getValue().remove(viewModel.getBacklashfixes().getValue().size() - 1);
                        save_fixes();
                        Toast.makeText(getContext(), "Backlash fixes of latest point deleted ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

              delete_all_point_fixes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

                if(viewModel.getBacklashfixes().getValue()!=null) {
                    if(viewModel.getBacklashfixes().getValue().size()==0){
                        Toast.makeText(getContext(), "No backlash fixes were found for any point ", Toast.LENGTH_SHORT).show();
                    }
                    if (viewModel.getBacklashfixes().getValue().size() > 0) {
                        viewModel.getBacklashfixes().getValue().clear();
                        save_fixes();
                        Toast.makeText(getContext(), "All Backlash fixes deleted ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });





        cross_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                handler.obtainMessage(OPEN_CROSS_TEST_SCREEN, -1, -1).sendToTarget();
                //cross_test_frame.setVisibility(View.VISIBLE);
               // test_fix.setVisibility(GONE);
               // edit_steps.setVisibility(GONE);
               // cross_test.setVisibility(GONE);
            }
        });


        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                up_times_pressed = 0;
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (down_times_pressed == 0) {
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<micro_move_ra-:>\n").sendToTarget();
                    down_times_pressed++;

                } else if (down_times_pressed >= 1 && down_times_pressed < 7 && up_times_pressed == 0) {
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<r+:>\n").sendToTarget();
                    down_times_pressed++;

                }
            }
        });


        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 down_times_pressed = 0;
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (up_times_pressed == 0) {
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<micro_move_ra+:>\n").sendToTarget();
                    up_times_pressed++;
                } else if (up_times_pressed >= 1 && up_times_pressed < 7 && down_times_pressed == 0) {
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<r+:>\n").sendToTarget();
                    up_times_pressed++;
                }
            }
        });


        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                right_times_pressed = 0;
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (left_times_pressed == 0) {
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<micro_move_dec-:>\n").sendToTarget();
                    left_times_pressed++;
                } else if (left_times_pressed >= 1 && left_times_pressed < 7 && right_times_pressed == 0) {
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<d+:>\n").sendToTarget();
                    left_times_pressed++;
                }
            }
        });


        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 left_times_pressed = 0;
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (right_times_pressed == 0) {
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<micro_move_dec+:>\n").sendToTarget();
                    right_times_pressed = right_times_pressed + 1;
                } else if (right_times_pressed >= 1 && right_times_pressed < 7 && left_times_pressed == 0) {
                    right_times_pressed++;
                    MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<d+:>\n").sendToTarget();
                }
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                left_times_pressed = 0;
                right_times_pressed = 0;
                up_times_pressed = 0;
                down_times_pressed = 0;
                MainActivity.handler.obtainMessage(MESSAGE_WRITE, "<stop:0:>\n").sendToTarget();
                viewModel.setTracking_status(false);
            }
        });


        open_close_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (open_close_camera.getText().toString().equals("open camera")) {
                    open_close_camera.setText("close camera");

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                        return;
                    }
                    camera_surface_v.getHolder().setFixedSize((int) (getResources().getDisplayMetrics().widthPixels), (int) (getResources().getDisplayMetrics().heightPixels / 1.9));
                    camera_surface_v.setVisibility(View.VISIBLE);
                    divider_camera_x1.setVisibility(View.VISIBLE);
                    divider_camera_x2.setVisibility(View.VISIBLE);
                    divider_camera_y1.setVisibility(View.VISIBLE);
                    divider_camera_y2.setVisibility(View.VISIBLE);
                    slider_camera_zoom.setVisibility(View.VISIBLE);
                    slider_camera_zoom.setVisibility(View.VISIBLE);
                    text_zoom_level.setVisibility(View.VISIBLE);

                    if (viewModel.getStar_object().getValue() != null && viewModel.getSide_of_meridian().getValue() != "unspecified") {

                        text_selected_object_name.setText("Selected :\n" + viewModel.getStar_object().getValue().getName_ascii());
                        text_side_of_meridian.setText("Direction (E-W) :\n " + viewModel.getSide_of_meridian().getValue());
                        text_selected_object_name.setVisibility(View.VISIBLE);
                        text_side_of_meridian.setVisibility(View.VISIBLE);
                    }
                } else if (open_close_camera.getText().toString().equals("close camera")) {
                    open_close_camera.setText("open camera");
                    camera_surface_v.setVisibility(INVISIBLE);
                    divider_camera_x1.setVisibility(GONE);
                    divider_camera_x2.setVisibility(GONE);
                    divider_camera_y1.setVisibility(GONE);
                    divider_camera_y2.setVisibility(GONE);
                    slider_camera_zoom.setVisibility(GONE);
                    text_zoom_level.setVisibility(GONE);
                    text_selected_object_name.setVisibility(GONE);
                    text_side_of_meridian.setVisibility(GONE);
                    camera_surface_v.getHolder().setFixedSize(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels / 3);
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
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        camera = Camera.open();
        Camera.Parameters parameters;
        parameters = camera.getParameters();
        camera.setDisplayOrientation(90);

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

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    public void save_fixes() {
        String json_backlash_fixes = new Gson().toJson(viewModel.getBacklashfixes().getValue());
        editor.putString("backlash_fixes", json_backlash_fixes);
        editor.apply();
            }
}
