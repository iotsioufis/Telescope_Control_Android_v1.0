package com.my_project.telescopecontrol;


import static android.view.View.GONE;

import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.Thread.sleep;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CrossTestFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {
    private SharedViewModel viewModel;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private MaterialButton cross_test_cancel;
    private ConstraintLayout cross_test_frame;
    private MaterialButton cross_test_ok;
    private EditText edit_degrees;
    private TextView detected_ra_backlash;
    private TextView detected_dec_backlash;
    MaterialButton button_run_cross_test;
    MaterialButton button_clear_image;
    private Slider slider_threshold;
    private static final int MY_CAMERA_REQUEST_CODE = 345;
    int activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
    double threshold = 180;
    boolean cross_test_started=false ;
    List<double []> list_of_points = new ArrayList<double[]>();
    Point checkpoint_0 = new Point(0,0);
    Point checkpoint_1 = new Point(0 , 0);
    Point checkpoint_2 = new Point(0 , 0);
    Point checkpoint_3 = new Point(0 , 0);
    Point checkpoint_4 = new Point(0 , 0);
    Point checkpoint_5 = new Point(0 , 0);
    Point checkpoint_6 = new Point(0 , 0);
    double dec_cross_steps = 0;
    double ra_cross_steps = 0;
    double distance_0_to_1=0;
    double distance_2_to_3=0;
    double distance_3_to_4=0;
    double distance_5_to_6=0;
    String distance_0_to_1_str="";
    String distance_2_to_3_str="";
    int calculated_dec_backlash_steps=0;
    String distance_3_to_4_str="";
    String distance_5_to_6_str="";
    int calculated_ra_backlash_steps=0;




    // List<double []> list_of_points = new ArrayList<double[]>();

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
        View v = inflater.inflate(R.layout.cross_test, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(GONE);
        detected_ra_backlash =v.findViewById(R.id.text_ra_backlash);
        detected_dec_backlash =v.findViewById(R.id.text_dec_backlash);
        button_run_cross_test = v.findViewById(R.id.button_run_cross_test);
        button_clear_image = v.findViewById(R.id.button_clear_image);
        cross_test_frame = v.findViewById(R.id.frame_cross_test);
        edit_degrees = v.findViewById(R.id.editText_cross_degrees);
        cross_test_ok = v.findViewById(R.id.button_ok_cross);
        cross_test_cancel = v.findViewById(R.id.button_cancel_cross);
        slider_threshold = v.findViewById(R.id.slider_threshold_cross_test);
        cameraView = v.findViewById(R.id.cameraview_cross_test);
        cameraView.setCvCameraViewListener(this);
        cameraView.enableFpsMeter();

     /*   if(viewModel.getMoving_status().getValue().equals("motors stopped.")){

            if(!viewModel.getTracking_status().getValue()){
                viewModel.setTracking_status(true);
                editor.putBoolean("tracking_is_on", true);
                editor.apply();}}*/

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            initializeCamera(cameraView, activeCamera);
            cameraView.enableView();

        }

        /*viewModel.get_cross_move_done().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean move_done) {
                if(viewModel.get_cross_move_done().getValue()){
                    if (distance_degrees >= 0.02 && auto_center_tries_counter < 4 ) {
                        auto_center_run();
                        auto_center_tries_counter++;
                        viewModel.set_cross_move_done(false);
                    }

                }

            }
        });
*/

        viewModel.get_cross_move_checkpoint().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer checkpoint_num) {
                if(viewModel.get_cross_move_checkpoint().getValue()==1){
                   checkpoint_1.x=brightest_point.x;
                   checkpoint_1.y=brightest_point.y;
                }
                if(viewModel.get_cross_move_checkpoint().getValue()==2){
                    checkpoint_2.x=brightest_point.x;
                    checkpoint_2.y=brightest_point.y;
                }
                if(viewModel.get_cross_move_checkpoint().getValue()==3){
                    checkpoint_3.x=brightest_point.x;
                    checkpoint_3.y=brightest_point.y;
                }
                if(viewModel.get_cross_move_checkpoint().getValue()==4){
                    checkpoint_4.x=brightest_point.x;
                    checkpoint_4.y=brightest_point.y;
                }
                if(viewModel.get_cross_move_checkpoint().getValue()==5){
                    checkpoint_5.x=brightest_point.x;
                    checkpoint_5.y=brightest_point.y;
                }
                if(viewModel.get_cross_move_checkpoint().getValue()==6){
                    checkpoint_6.x=brightest_point.x;
                    checkpoint_6.y=brightest_point.y;
                    distance_0_to_1 = sqrt((checkpoint_0.x - checkpoint_1.x) * (checkpoint_0.x - checkpoint_1.x) + (checkpoint_0.y - checkpoint_1.y) * (checkpoint_0.y - checkpoint_1.y));
                    distance_0_to_1_str = String.valueOf(distance_0_to_1);
                    distance_2_to_3 = sqrt((checkpoint_2.x - checkpoint_3.x) * (checkpoint_2.x - checkpoint_3.x) + (checkpoint_2.y - checkpoint_3.y) * (checkpoint_2.y - checkpoint_3.y));
                    distance_2_to_3_str = String.valueOf(distance_2_to_3);
                    distance_3_to_4 = sqrt((checkpoint_3.x - checkpoint_4.x) * (checkpoint_3.x - checkpoint_4.x) + (checkpoint_3.y - checkpoint_4.y) * (checkpoint_3.y - checkpoint_4.y));
                    distance_3_to_4_str = String.valueOf(distance_3_to_4);
                    distance_5_to_6 = sqrt((checkpoint_5.x - checkpoint_6.x) * (checkpoint_5.x - checkpoint_6.x) + (checkpoint_5.y - checkpoint_6.y) * (checkpoint_5.y - checkpoint_6.y));
                    distance_5_to_6_str = String.valueOf(distance_5_to_6);

                    //calculated_dec_backlash_steps=abs(dec_cross_steps)-abs((distance_2_to_3/distance_0_to_1)*dec_cross_steps);
                   // calculated_ra_backlash_steps=abs(ra_cross_steps)-abs((distance_5_to_6/distance_3_to_4)*ra_cross_steps);
                    calculated_dec_backlash_steps=(int)(dec_cross_steps*(distance_2_to_3-distance_0_to_1)/distance_0_to_1);
                    calculated_ra_backlash_steps=(int)(ra_cross_steps*(distance_5_to_6-distance_3_to_4)/distance_3_to_4);
                    Log.e("TAG", "distance_0_to_1_str  :" + distance_0_to_1_str);
                    Log.e("TAG", "distance_2_to_3_str  :" + distance_2_to_3_str);
                    Log.e("TAG", "distance_3_to_4_str  :" + distance_3_to_4_str);
                    Log.e("TAG", "distance_5_to_6_str  :" + distance_5_to_6_str);

                    detected_dec_backlash.setText("Detected DEC backlash error : " +  abs(calculated_dec_backlash_steps)+" microsteps"+ "\nApplied DEC backlash fix  : "+(-viewModel.get_dec_backlash_fix().getValue())+" microsteps");
                    detected_ra_backlash.setText("Detected RA backlash  error : "+abs(calculated_ra_backlash_steps)+" microsteps"+"\nApplied RA backlash fix  : "+(-viewModel.get_ra_backlash_fix().getValue())+" microsteps");
                    viewModel.set_cross_move_checkpoint(7);
                    cross_test_started=false;

                }
                if(viewModel.get_cross_move_checkpoint().getValue()==7){

                    detected_dec_backlash.setText("Detected DEC backlash error : " +  abs(calculated_dec_backlash_steps)+" microsteps"+ "\nApplied DEC backlash fix  : "+(-viewModel.get_dec_backlash_fix().getValue())+" microsteps");
                    detected_ra_backlash.setText("Detected RA backlash  error : "+abs(calculated_ra_backlash_steps)+" microsteps"+"\nApplied RA backlash fix  : "+(-viewModel.get_ra_backlash_fix().getValue())+" microsteps");
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



        button_run_cross_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                cross_test_frame.setVisibility(View.VISIBLE);
                slider_threshold.setVisibility(View.GONE);
                viewModel.set_cross_move_done(false);
                list_of_points.clear();
            }
        });


        cross_test_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (edit_degrees.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Entered degrees should be a decimal \n between 0 and 5 ", Toast.LENGTH_LONG).show();
                }
                if (!edit_degrees.getText().toString().equals("")) {
                    Float degrees = Float.valueOf(Float.parseFloat(edit_degrees.getText().toString()));
                    if (degrees >= 0 && degrees <= 5) {

                        ra_cross_steps = sharedPreferences.getInt("ra_micro_1", 722) * degrees;
                        dec_cross_steps = sharedPreferences.getInt("dec_micro_1", 361) * degrees ;
                        Handler handler = MainActivity.handler;
                        handler.obtainMessage(MESSAGE_WRITE, "<cross_move:" + ((int) ra_cross_steps) + ":"
                                + ((int) ( dec_cross_steps)) + ":>\n").sendToTarget();
                        cross_test_frame.setVisibility(GONE);
                        cross_test_started=true;

                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                    }
                    if (degrees < 0 || degrees > 5) {
                        Toast.makeText(getContext(), "Entered degrees should be \n between 0 and 5 degrees  ", Toast.LENGTH_LONG).show();
                    }
                    edit_degrees.setText(edit_degrees.getText().toString());
                    checkpoint_0.x = brightest_point.x;
                    checkpoint_0.y = brightest_point.y;
                }
                slider_threshold.setVisibility(View.VISIBLE);
            }
        });


        cross_test_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                cross_test_frame.setVisibility(GONE);
                slider_threshold.setVisibility(View.VISIBLE);
                cross_test_started=false;
                           }
        });


        button_clear_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.set_cross_move_done(false);
                list_of_points.clear();


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

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }



    Point brightest_point = new Point(0, 0);
    Point center = new Point(0, 0);
    Mat input_g;
    Mat mRgba;
    Scalar red = new Scalar(255, 0, 0);
    Scalar white = new Scalar(255,255,255);
    Scalar green = new Scalar(0, 255, 0);
    Scalar blue = new Scalar(0, 0, 255);
    Scalar yellow = new Scalar(255, 255, 0);

    Size erode_kernel_size = new Size((threshold * 2) + 1, (threshold * 2) + 1);
    List<MatOfPoint> contours = new ArrayList<>();
    Point contour_center = new Point(0, 0);
    Mat hierarchy = new Mat();
    double maxVal = 0;
    int maxValIdx = 0;
    double contourArea = 0;
    MatOfPoint2f c2f = new MatOfPoint2f();
    float[] radius = new float[1];





    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        input_g = inputFrame.gray();
        center.x = input_g.cols() / 2.0;
        center.y = input_g.rows() / 2.0;
        erode_kernel_size.width = (threshold * 2) + 1;
        erode_kernel_size.height = (threshold * 2) + 1;
        Imgproc.GaussianBlur(input_g, input_g, new Size(5, 5), 25);
        Imgproc.threshold(input_g, input_g, (int) threshold, 255, Imgproc.THRESH_BINARY);
        Imgproc.findContours(input_g, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS);
        /*find the contour that covers the maximum area , which corresponds to the brightest and largest object :*/

        if (contours.size() > 0) {

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
            c2f = new MatOfPoint2f(contours.get(maxValIdx).toArray());
            c2f.fromArray(contours.get(maxValIdx).toArray());
            Imgproc.minEnclosingCircle(c2f, contour_center, radius);
            Imgproc.circle(mRgba, contour_center, (int)radius[0], red, 2);
            brightest_point.x = contour_center.x;
            brightest_point.y = contour_center.y;

        }


       // define_mean_position();


        if ( cross_test_started) {
            list_of_points.add(new double [] {brightest_point.x,brightest_point.y});
        }

        if(viewModel.get_cross_move_done().getValue()){


          /* for(int i=0;i<list_of_points.size();i++){
                test_point.x=list_of_points.get(i)[0];
                test_point.y=list_of_points.get(i)[1];
                Imgproc.circle(mRgba, test_point, 2, red, 2);

            }*/

           /* Imgproc.circle(mRgba, checkpoint_0, 10, red, 5);
            Imgproc.circle(mRgba, checkpoint_1, 10, red, 5);
            Imgproc.circle(mRgba, checkpoint_2, 10, red, 5);
            Imgproc.circle(mRgba, checkpoint_3, 10, red, 5);
            Imgproc.circle(mRgba, checkpoint_4, 10, red, 5);
            Imgproc.circle(mRgba, checkpoint_5, 10, red, 5);
            Imgproc.circle(mRgba, checkpoint_6, 10, red, 5);
*/
            Imgproc.line(mRgba, checkpoint_0, checkpoint_1, green, 3, 16);
            Imgproc.line(mRgba, checkpoint_2, checkpoint_3, blue, 3, 16);
            Imgproc.line(mRgba, checkpoint_3, checkpoint_4, red, 4, 16);
            Imgproc.line(mRgba, checkpoint_5, checkpoint_6, white, 3, 16);
            Imgproc.line(mRgba, checkpoint_0, checkpoint_3, yellow , 3, 16);



        }


        Imgproc.circle(mRgba, brightest_point, 5, green, 1);
        Imgproc.circle(mRgba, center, 10, yellow, 2);
        Imgproc.line(mRgba, center, brightest_point, green, 1, 16);

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

    private void initializeCamera(JavaCameraView javaCameraView, int activeCamera) {
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }





}