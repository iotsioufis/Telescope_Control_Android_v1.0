package com.my_project.telescopecontrol;


import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import static android.view.View.GONE;

import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.OPEN_ALIGNMENT_SCREEN;
import static com.my_project.telescopecontrol.MainActivity.handler;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static java.lang.Thread.sleep;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AutoCenterFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {
    private SharedViewModel viewModel;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    MaterialButton button_auto_center;
    MaterialButton button_calibrate;
    MaterialButton button_star_centered;
    private TextView text_rotation;
    private TextView text_distance_degrees;
    private Slider slider_calibration_speed;
    private Slider slider_threshold;
    private CheckBox check_invert_dec_correction;
    private CheckBox check_invert_ra_correction;
    private ConstraintLayout add_to_alignment_frame;
    private MaterialButton button_add_point;
    private MaterialButton button_cancel_add_point;
    private TextView add_point_textview;
    private MaterialButton show_help;
    private MaterialButton close_help;
    private MaterialButton close_help_corner;
    private ConstraintLayout auto_center_help_frame;
    double distance_from_center = 0;
    private static final int MY_CAMERA_REQUEST_CODE = 345;
    int activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
    boolean moving_right = false;
    boolean moving_left = false;
    boolean begin_auto_centering = false;
    boolean start_testing_dec = false;
    double distance_before_dec_test = 0;
    int position_detected_counter = 0;
    double mean_x = 0;
    double mean_y = 0;
    double threshold = 180;
    double calibration_speed = 1;
    double current_rotation = 0;
    double rotation_after_calibration = 0;
    boolean calibration_started = false;
    boolean calibration_finished = false;
    int dec_position_before_calibration = 0;
    int dec_position_after_calibration = 0;
    int dec_offset = 0;
    double distance_moved_during_calibration = 0;
    double degrees_to_pixel = 0;
    double DEC_micro_1;
    double RA_micro_1;
    double alignment_time;
    double dec;
    double dec_transformed;
    double goto_time;
    double ha_degrees;
    double initial_time;
    double ra;
    int ra_offset;
    String side_of_meridian;
    int auto_center_tries_counter = 0;
    double distance_degrees=0;






    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("TAG", "OpenCV installed sussessfully");
        } else {
            Log.d("TAG", "OpenCV is not installed");
        }
    }

    private JavaCameraView cameraView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFS, 0);
        editor = sharedPreferences.edit();
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        View v = inflater.inflate(R.layout.auto_centering, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(GONE);
        ExtraAlignments extraAlignments = new ExtraAlignments(getContext(), viewModel);
        slider_calibration_speed = v.findViewById(R.id.slider_calibration_auto_center_speed);
        slider_threshold = v.findViewById(R.id.slider_threshold);
        button_auto_center = v.findViewById(R.id.button_auto_center);
        button_calibrate = v.findViewById(R.id.button_start_camera_calibration);
        button_star_centered = v.findViewById(R.id.button_star_centered);
        check_invert_ra_correction =v.findViewById(R.id.checkBox_invert_ra_correction);
        check_invert_dec_correction =v.findViewById(R.id.checkBox_invert_dec_correction);
        add_to_alignment_frame = v.findViewById(R.id.frame_add_to_alignment_auto_centered);
        button_cancel_add_point = v.findViewById(R.id.button_cancel_add_to_alignment_auto_centered);
        button_add_point = v.findViewById(R.id.button_add_to_alignment_auto_centered);
        add_point_textview = v.findViewById(R.id.text_add_to_alignment_auto_centered);
        cameraView = v.findViewById(R.id.cameraview);
        cameraView.setCvCameraViewListener(this);
        text_distance_degrees=v.findViewById(R.id.text_distance_degrees);
        text_rotation=v.findViewById(R.id.text_rotation);
        show_help = v.findViewById(R.id.button_show_auto_center_help);
        auto_center_help_frame = v.findViewById(R.id. frame_auto_center_info);
        close_help_corner =v.findViewById(R.id.button_close_auto_center_help);
        close_help =v.findViewById(R.id.button_hide_auto_center_help);

        //cameraView.setMaxFrameSize(640, 480);
        //.setFixedSize((int) (getResources().getDisplayMetrics().widthPixels*2), (int) (0.5*getResources().getDisplayMetrics().heightPixels / 1.2));
        cameraView.enableFpsMeter();
        calibration_finished = false;
        if(viewModel.getMoving_status().getValue().equals("motors stopped.")){
        start_sidereal_tracking();
        if(!viewModel.getTracking_status().getValue()){
        viewModel.setTracking_status(true);
        editor.putBoolean("tracking_is_on", true);
        editor.apply();}}

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            initializeCamera(cameraView, activeCamera);
            cameraView.enableView();

        }





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
                            }
                        });

                    }
                });
            }
        });


        button_star_centered.setOnClickListener(new View.OnClickListener() {
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
                        add_point_textview.setVisibility(View.VISIBLE);
                    } else if (viewModel.getAlignmentpoints().getValue() == null) {
                        Toast.makeText(getContext(), "To add this object as an alignment point , first load n-star alignment data . ", Toast.LENGTH_SHORT).show();
                    }
                }
                start_sidereal_tracking();
                if(!viewModel.getTracking_status().getValue()){
                    viewModel.setTracking_status(true);
                    editor.putBoolean("tracking_is_on", true);
                    editor.apply();
                }}
        });

        button_cancel_add_point.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                add_to_alignment_frame.setVisibility(View.GONE);
            }
        });


        viewModel.get_dec_returned_current_position().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer received_position) {
                dec_offset = viewModel.getDEC_offset().getValue();
                if (calibration_started) {
                    dec_position_before_calibration = viewModel.get_dec_returned_current_position().getValue() / 4;
                    start_moving_dec_motor_untested();
                }
                if (!calibration_started && calibration_finished) {
                    viewModel.set_camera_calibration_done(true);
                    dec_position_after_calibration = viewModel.get_dec_returned_current_position().getValue() / 4;
                    dec_offset = dec_position_after_calibration - dec_position_before_calibration;
                    Log.e("TAG", "dec_offset :" + dec_offset);

                    distance_moved_during_calibration = abs(sqrt((brightest_point_before_testing.x - brightest_point.x) * (brightest_point_before_testing.x - brightest_point.x) + (brightest_point_before_testing.y - brightest_point.y) * (brightest_point_before_testing.y - brightest_point.y)));
                    Log.e("TAG", "distance_moved_during_calibration :" + distance_moved_during_calibration);
                    rotation_after_calibration = rotation(brightest_point, brightest_point_before_testing);
                    viewModel.set_calculated_rotation(rotation_after_calibration);
                    degrees_to_pixel = dec_offset / (distance_moved_during_calibration * viewModel.getDEC_micro_1().getValue());
                    viewModel.set_degrees_to_pixel(degrees_to_pixel * viewModel.get_dec_goto_ending_direction().getValue());
                    Log.e("TAG", "rotation_after_calibration  :" + rotation_after_calibration);
                    Log.e("TAG", "degrees_to_pixel  :" + degrees_to_pixel);

                }

            }
        });

        viewModel.get_auto_centering_done().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean centering_done) {
                if(viewModel.get_auto_centering_done().getValue()){
                    if (distance_degrees >= 0.02 && auto_center_tries_counter < 4 ) {
                        auto_center_run();
                        auto_center_tries_counter++;
                        viewModel.set_auto_centering_done(false);
                    }

                }

            }
        });


        slider_threshold.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                // v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                threshold = value;

            }

        });

        slider_calibration_speed.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                //   v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                calibration_speed = value;
                if(viewModel.getMoving_status().getValue().equals("motors stopped.")) {
                    update_current_speed();
                }
            }

        });




        button_auto_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (!viewModel.get_camera_calibration_done().getValue()) {
                    Toast.makeText(getContext(), "Camera calibration is required ", Toast.LENGTH_SHORT).show();
                }
                if (viewModel.get_camera_calibration_done().getValue()) {
                    auto_center_tries_counter = 0;
                   auto_center_run();
                }
            }
        });

        button_calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                if (button_calibrate.getText().equals("start calibration")) {
                    viewModel.set_camera_calibration_done(false);
                    button_calibrate.setText("stop calibration");
                    button_calibrate.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0C7839")));
                    calibration_started = true;
                    calibration_finished = false;
                    dec_position_before_calibration = 0;
                    dec_position_after_calibration = 0;
                    dec_offset = 0;
                    handler.obtainMessage(MESSAGE_WRITE, "<get_motor_directions:>").sendToTarget();
                    get_current_dec_position();


                    //start_sidereal_tracking();

                } else if (button_calibrate.getText().equals("stop calibration")) {
                    button_calibrate.setText("start calibration");
                    button_calibrate.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2337A8")));
                    calibration_started = false;
                    calibration_finished = true;
                    stop();
                    start_sidereal_tracking();
                    if(!viewModel.getTracking_status().getValue()){
                    viewModel.setTracking_status(true);
                    editor.putBoolean("tracking_is_on", true);
                    editor.apply();}
                    get_current_dec_position();

                }

            }
        });
        show_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                auto_center_help_frame.setVisibility(View.VISIBLE);
                cameraView.disableView();
                button_auto_center.setVisibility(GONE);
                button_calibrate.setVisibility(GONE);
                slider_threshold.setVisibility(GONE);
                slider_calibration_speed.setVisibility(GONE);
                button_star_centered.setVisibility(GONE);
                check_invert_dec_correction.setVisibility(GONE);
                check_invert_ra_correction.setVisibility(GONE);
            }
        });
        close_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                auto_center_help_frame.setVisibility(GONE);
                cameraView.enableView();
                button_auto_center.setVisibility(View.VISIBLE);
                button_calibrate.setVisibility(View.VISIBLE);
                slider_threshold.setVisibility(View.VISIBLE);
                slider_calibration_speed.setVisibility(View.VISIBLE);
                button_star_centered.setVisibility(View.VISIBLE);
                check_invert_dec_correction.setVisibility(View.VISIBLE);
                check_invert_ra_correction.setVisibility(View.VISIBLE);
            }
        });

        close_help_corner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                auto_center_help_frame.setVisibility(GONE);
                cameraView.enableView();
                button_auto_center.setVisibility(View.VISIBLE);
                button_calibrate.setVisibility(View.VISIBLE);
                slider_threshold.setVisibility(View.VISIBLE);
                slider_calibration_speed.setVisibility(View.VISIBLE);
                button_star_centered.setVisibility(View.VISIBLE);
                check_invert_dec_correction.setVisibility(View.VISIBLE);
                check_invert_ra_correction.setVisibility(View.VISIBLE);
            }
        });

        return v;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraView.enableView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraView != null) {
            cameraView.disableView();
        }
        begin_auto_centering = false;
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    void move_right() {

        handler.obtainMessage(MESSAGE_WRITE, "<micro_move_dec+:>").sendToTarget();
        if (!moving_right) {
            handler.obtainMessage(MESSAGE_WRITE, "<d+:>").sendToTarget();
        }
        update_current_speed();
        moving_right = true;
        moving_left = false;
    }

    void move_left() {
        handler.obtainMessage(MESSAGE_WRITE, "<micro_move_dec-:>").sendToTarget();
        if (!moving_left) {
            handler.obtainMessage(MESSAGE_WRITE, "<d+:>").sendToTarget();
        }
        update_current_speed();
        moving_left = true;
        moving_right = false;
    }


    void stop() {
        handler.obtainMessage(MESSAGE_WRITE, "<stop:1:>\n").sendToTarget();
        moving_left = false;
        moving_right = false;
    }

    void start_sidereal_tracking() {
        try {
            sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int ra_horizon_tracking_limit = viewModel.get_ra_steps_at_horizon().getValue();
        if(viewModel.getLatitute().getValue()>=0) {
            handler.obtainMessage(MESSAGE_WRITE, "<track:" + viewModel.get_sidereal_rate().getValue() * (-1) + ":" + 0 + ":" + ra_horizon_tracking_limit + ":>\n").sendToTarget();
        }
        if(viewModel.getLatitute().getValue()<0) {
            handler.obtainMessage(MESSAGE_WRITE, "<track:" + viewModel.get_sidereal_rate().getValue()  + ":" + 0 + ":" + ra_horizon_tracking_limit + ":>\n").sendToTarget();
        }
        }


    Point center = new Point(0, 0);
    Point brightest_point = new Point(0, 0);
    Point brightest_point_rotated = new Point(0, 0);
    Mat input_g;
    Mat mRgba;
    String distance_str;
    Scalar red = new Scalar(255, 0, 0);
    Scalar green = new Scalar(0, 255, 0);
    Scalar yellow = new Scalar(255, 255, 0);
    Point brightest_point_before_testing = new Point(0, 0);
    Size erode_kernel_size = new Size((threshold * 2) + 1, (threshold * 2) + 1);
    List<MatOfPoint> contours = new ArrayList<>();
    Point contour_center = new Point(0, 0);
    Mat hierarchy = new Mat();
    double maxVal = 0;
    int maxValIdx = 0;
    double contourArea = 0;
    MatOfPoint2f c2f = new MatOfPoint2f();
    String rotation_str = "";
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    DecimalFormat decimal_format = new DecimalFormat("00.00", symbols);
    //Point test_point =new Point(0,0);




    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        input_g = inputFrame.gray();
        erode_kernel_size.width = (threshold * 2) + 1;
        erode_kernel_size.height = (threshold * 2) + 1;
        //Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, erode_kernel_size);
        //Imgproc.morphologyEx(input_g_2,input_g_2,Imgproc.MORPH_OPEN,kernel);
        // Imgproc.erode(input_g,input_g_2,kernel);
        // Imgproc.dilate(input_g_2,input_g_2,kernel);
        Imgproc.GaussianBlur(input_g, input_g, new Size(5, 5), 25);
        Imgproc.threshold(input_g, input_g, (int) threshold, 255, Imgproc.THRESH_BINARY);
        Imgproc.findContours(input_g, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS);
        /*find the contour that covers the maximum area , which corresponds to the brightest and largest object :*/
        if (contours.size() > 0) {
            float[] radius = new float[1];
            maxVal = 0;
            maxValIdx = 0;
            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
                contourArea = Imgproc.contourArea(contours.get(contourIdx));
                if (maxVal < contourArea) {
                    maxVal = contourArea;
                    maxValIdx = contourIdx;
                }
            }
            Imgproc.drawContours(mRgba, contours, maxValIdx, green, 2);
            // c2f = new MatOfPoint2f(contours.get(maxValIdx).toArray());
            c2f.fromArray(contours.get(maxValIdx).toArray());
            Imgproc.minEnclosingCircle(c2f, contour_center, radius);
            Imgproc.circle(mRgba, contour_center, (int)radius[0], red, 2);
           // Imgproc.circle(mRgba, brightest_point_rotated, (int) radius[0], red, 2);
            brightest_point.x = contour_center.x;
            brightest_point.y = contour_center.y;

        }


        define_mean_position();
        center.x = input_g.cols() / 2.0;
        center.y = input_g.rows() / 2.0;

        brightest_point_rotated.x = get_rotated_point_coords(brightest_point, center, viewModel.get_calculated_rotation().getValue())[0];
        brightest_point_rotated.y = get_rotated_point_coords(brightest_point, center, viewModel.get_calculated_rotation().getValue())[1];

        if (calibration_started) {
            distance_from_center = sqrt((brightest_point.x - center.x) * (brightest_point.x - center.x) + (brightest_point.y - center.y) * (brightest_point.y - center.y));
            distance_str = String.valueOf(distance_from_center);
            current_rotation = rotation(brightest_point, brightest_point_before_testing); // 90 degrees added because the frame in preview is rotated by 90°
            rotation_str = "Rotation : " + decimal_format.format((90+ current_rotation)) + " °";
            setText( text_rotation,rotation_str);
            //list_of_points.add(new double [] {brightest_point.x,brightest_point.y});
        }

        if (viewModel.get_camera_calibration_done().getValue()) {

            distance_from_center = sqrt((brightest_point.x - center.x) * (brightest_point.x - center.x) + (brightest_point.y - center.y) * (brightest_point.y - center.y));
             distance_str = String.valueOf(distance_from_center);
            // double tempt_rotation = 90 + rotation_after_calibration;
             rotation_str = "Rotation : " +  decimal_format.format((90 + rotation_after_calibration)) + " °";
            setText( text_rotation,rotation_str);
            distance_degrees = viewModel.get_degrees_to_pixel().getValue()* distance_from_center;
            distance_str = "Distance from center : " +   decimal_format.format(distance_degrees) + " °";
            setText(text_distance_degrees,distance_str);
           /* for(int i=0;i<list_of_points.size();i++){
                test_point.x=list_of_points.get(i)[0];
                test_point.y=list_of_points.get(i)[1];
                Imgproc.circle(mRgba, test_point, 5, red, 2);

            }*/

        }
        if (!calibration_started && !viewModel.get_camera_calibration_done().getValue()) {
            distance_from_center = sqrt((brightest_point.x - center.x) * (brightest_point.x - center.x) + (brightest_point.y - center.y) * (brightest_point.y - center.y));
            setText( text_rotation,"");
            setText(text_distance_degrees,"");

        }
        Imgproc.line(mRgba, center, brightest_point, green, 5, 16);
        Imgproc.circle(mRgba, brightest_point, 5, green, 1);
        Imgproc.circle(mRgba, center, 10, yellow , 2);
        contours.clear();
        input_g.release();
        return mRgba;

    }




    // callback to be executed after the user has given approval or rejection via system prompt
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
                initializeCamera(cameraView, activeCamera);

            } else {
                // camera will stay off
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();

            }
        }
    }
    private void setText(final TextView text,final String value){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }
    private void initializeCamera(JavaCameraView javaCameraView, int activeCamera) {
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }


    private void start_moving_dec_motor_untested() {

        // begin_auto_tracking=false; //so whatever is inside ,is executed only once
        /*begin testing if the distance is decreased when moving left or when moving right :*/
        //first check the ending move direction :

        if (viewModel.get_dec_goto_ending_direction().getValue() == 1) {
            start_testing_dec = true; //so we can later check
            //save distance before the move starts so later we can get new distance and compare with the saved one :
            if (distance_from_center != 0) {
                distance_before_dec_test = distance_from_center;
                brightest_point_before_testing.x = brightest_point.x;
                brightest_point_before_testing.y = brightest_point.y;
            }
            move_right();
            Log.e("TAG", "first_move: MOVE_RIGHT   ");
        }

        if (viewModel.get_dec_goto_ending_direction().getValue() == -1) {
            start_testing_dec = true; //so we can later check , maybe delete moving_right ..
            //save distance before the move starts so later we can get new distance and compare with the saved one :
            if (distance_from_center != 0) {
                distance_before_dec_test = distance_from_center;
                brightest_point_before_testing.x = brightest_point.x;
                brightest_point_before_testing.y = brightest_point.y;
            }

            move_left();
            Log.e("TAG", "first_move: MOVE_LEFT   ");
        }
        update_current_speed();
    }


    private void define_mean_position() {

        //defines the mean position ,last 5 values are used
        mean_x = mean_x + brightest_point.x;
        mean_y = mean_y + brightest_point.y;
        position_detected_counter++;

        if (position_detected_counter == 5) {
            mean_x = mean_x / 5;
            mean_y = mean_y / 5;
            brightest_point.x = mean_x;
            brightest_point.y = mean_y;
            mean_x = 0;
            mean_y = 0;
            position_detected_counter = 0;
        }


    }

    private void update_current_speed() {
        try {
            sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        handler.obtainMessage(MESSAGE_WRITE, "<set_calibration_speed:" + calibration_speed + ":>").sendToTarget();
    }

    private double rotation(Point point_1, Point point_2) {
        // Log.e("TAG", "LwMax.x :"+ LwMax.x + "LwMax.y :" +LwMax.y );
        // Log.e("TAG", "Lwmax_before_testing.x :"+ Lwmax_before_testing.x + "Lwmax_before_testing.y :" +Lwmax_before_testing.y );
        double slope = 0;
        slope = (point_2.y - point_1.y) / (point_2.x - point_1.x);
        return toDegrees(atan(slope));
    }

    private void get_current_dec_position() {
        try {
            sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        handler.obtainMessage(MESSAGE_WRITE, "<request_dec_position:>").sendToTarget();

    }

    private double[] get_rotated_point_coords(Point point, Point center, double angle) {

        angle = toRadians(-angle);
        double rotated_x = center.x + (point.x - center.x) * cos(angle) - (point.y - center.y) * sin(angle);
        double rotated_y = center.y + (point.x - center.x) * sin(angle) + (point.y - center.y) * cos(angle);
        double[] rotated_coords = new double[]{rotated_x, rotated_y};
        return rotated_coords;

    }

    private void auto_center_run(){
        double dif_in_ra = brightest_point_rotated.y - center.y;
        double dif_in_dec = brightest_point_rotated.x - center.x;
        if(check_invert_dec_correction.isChecked()){
            // Toast.makeText(getContext(), "dec correction inverted", Toast.LENGTH_SHORT).show();
            dif_in_dec = -dif_in_dec;}
        if(check_invert_ra_correction.isChecked()){
            // Toast.makeText(getContext(), "ra correction inverted", Toast.LENGTH_SHORT).show();
            dif_in_ra = -dif_in_ra;}
        degrees_to_pixel = viewModel.get_degrees_to_pixel().getValue();
        Log.e("TAG", "dif_in_ra  :" + (dif_in_ra * degrees_to_pixel) * viewModel.getRA_micro_1().getValue());
        Log.e("TAG", "dif_in_dec  :" + (dif_in_dec * degrees_to_pixel) * viewModel.getDEC_micro_1().getValue());
        if (viewModel.getSide_of_meridian().getValue().equals("west")) {
            dif_in_ra = -dif_in_ra;
        }
        handler.obtainMessage(MESSAGE_WRITE, "<center_object:"
                + (-dif_in_ra * degrees_to_pixel) * viewModel.getRA_micro_1().getValue()
                + ":" + (-dif_in_dec * degrees_to_pixel) * viewModel.getDEC_micro_1().getValue() + ">").sendToTarget();


    }


}