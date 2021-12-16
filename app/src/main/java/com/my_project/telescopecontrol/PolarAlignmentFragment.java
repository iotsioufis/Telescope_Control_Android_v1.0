package com.my_project.telescopecontrol;

import static com.my_project.telescopecontrol.MainActivity.OPEN_CAMERA_SCREEN;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;

public class PolarAlignmentFragment extends Fragment {
    private MaterialButton close_help;
    private MaterialButton close_help_corner;
    private MaterialButton get_polaris_position;
    private MaterialButton open_camera;
    private ConstraintLayout polar_alignment_help_frame;
    private ConstraintLayout polar_alignment_scope_frame;
    double polaris_HA = 0.0d;
    private MaterialButton show_help;
    TelescopeCalcs tel_calcs = new TelescopeCalcs();
    private TextView text_polaris_ha;
    private SharedViewModel viewModel;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation)).getMenu().getItem(3).setChecked(true);
        final View v = inflater.inflate(R.layout.polar_alignment, container, false);
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        get_polaris_position = v.findViewById(R.id.button_get_polaris_position);
        open_camera = v.findViewById(R.id.button_polar_alignment_camera_open);
        text_polaris_ha = v.findViewById(R.id.text_polaris_ha);
        show_help = v.findViewById(R.id.button_show_polar_alignment_help);
        close_help = v.findViewById(R.id.button_hide_polar_alignment_help);
        close_help_corner = v.findViewById(R.id.button_close_polar_alignment_help);
        polar_alignment_help_frame = v.findViewById(R.id.frame_polar_alignment_info);
        polar_alignment_scope_frame = v.findViewById(R.id.outer_circle);
        polaris_HA = get_polaris_HA();
        TextView textView = text_polaris_ha;
        textView.setText("" + polaris_HA);
        polar_alignment_scope_frame.setRotation((-((float) polaris_HA)) * 15.0f);
        text_polaris_ha.setText(decimal_to_dms(polaris_HA));


        show_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                polar_alignment_help_frame.setVisibility(View.VISIBLE);
            }
        });
        close_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                polar_alignment_help_frame.setVisibility(View.GONE);
            }
        });
        close_help_corner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                polar_alignment_help_frame.setVisibility(View.GONE);
            }
        });
        get_polaris_position.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                polaris_HA = get_polaris_HA();
                polar_alignment_scope_frame.setRotation((-((float) polaris_HA)) * 15.0f);
                text_polaris_ha.setText(decimal_to_dms(polaris_HA));
            }
        });
        open_camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                MainActivity.handler.obtainMessage(OPEN_CAMERA_SCREEN, -1, -1).sendToTarget();
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });
        return v;
    }


    public double get_polaris_HA() {
        double[] polaris_current_coords = tel_calcs.calculate_coords_with_precession(2.5303040666666665d, 89.264109d, viewModel);
        viewModel.setSide_of_meridian(viewModel.getSide_of_meridian().getValue());
        return polaris_current_coords[0];
    }

    public String decimal_to_dms(double decimal_coord) {
        Integer degrees_int_part = Integer.valueOf((int) decimal_coord);
        double decimal_part = Math.abs((decimal_coord - ((double) degrees_int_part.intValue())) * 60.0d);
        Integer mm = Integer.valueOf((int) decimal_part);
        Double ss = Double.valueOf((decimal_part - ((double) mm.intValue())) * 60.0d);
        String mm_str = new DecimalFormat("00").format(mm);
        String ss_str = new DecimalFormat("00.0").format(ss);
        return degrees_int_part.toString() + "h " + mm_str + "m " + ss_str + "s";
    }
}
