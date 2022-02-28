package com.my_project.telescopecontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import Jama.Matrix;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static com.my_project.telescopecontrol.MainActivity.SHARED_PREFS;
import static java.lang.Math.PI;
import static java.lang.Math.floor;

public class AlignmentFragment extends Fragment {
    private MaterialButton begin;
    private MaterialButton cancel;
    private MaterialButton next_star;
    private MaterialButton load_matrix;
    private MaterialButton delete_west_extra_points;
    private MaterialButton delete_east_extra_points;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private TextView text_step1;
    private TextView text_step2;
    private TextView text_step3;

    private SharedViewModel viewModel;
    private boolean begin_alignment = false;

    double ra;
    double dec;
    double ha_degrees;
    int ra_offset;
    int dec_offset;
    double initial_time;
    double alignment_time;
    double RA_micro_1;
    double DEC_micro_1;

    String side_of_meridian;
    int star_number = 1;
    int west_array_counter;
    int east_array_counter;
    double[][] alignment_stars_matrix_west = new double[3][5];
    double[][] alignment_stars_matrix_east = new double[3][5];
    Matrix transformation_matrix_east;
    Matrix transformation_matrix_west;
    ArrayList<AlignmentPoint> temp_alignment_points_east = new ArrayList<AlignmentPoint>();
    ArrayList<AlignmentPoint> temp_alignment_points_west = new ArrayList<AlignmentPoint>();
    ArrayList<AlignmentCentroid> temp_alignment_centroids = new ArrayList<AlignmentCentroid>();
    String str_matrix_west;
    String str_matrix_east;
    String saved_initial_time = "initial_time";
    CoordinatesTransformations coords_transformations = new CoordinatesTransformations();
    private int alignment_stars_num = 2;
    boolean calculate_3rd_star = true;
    int radioId = 0;
    boolean toast_msg_displayed_1_time = true;
    String alignment_star_name;
    String next_star_text = "Confirm Centered Star " + star_number + " of " + alignment_stars_num;
    private MaterialButton close_help;
    private MaterialButton close_help_corner;
    SharedPreferences.Editor editor;
    private MaterialButton get_stars;
    private MaterialButton polar_alignment;
    SharedPreferences sharedPreferences;
    private MaterialButton show_help;
    private ConstraintLayout alignment_help_frame;


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (viewModel.get_alignment_fragment_restore().getValue().booleanValue()) {
            begin_alignment = sharedPreferences.getBoolean("begin_alignment", false);
            boolean alignment_done = sharedPreferences.getBoolean("alignment_done", false);
            viewModel.set_alignment_done(Boolean.valueOf(alignment_done));
            Toast.makeText(getContext(), "restored from memory ", Toast.LENGTH_SHORT).show();
            if (alignment_done) {
                loadmatrix();
            }
        } else if (!viewModel.get_alignment_fragment_restore().getValue().booleanValue()) {
            begin_alignment = viewModel.get_begin_alignment().getValue().booleanValue();
        }
        viewModel.set_alignment_fragment_restore(false);
    }


    @Nullable
    @Override


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences sharedPreferences2 = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        sharedPreferences = sharedPreferences2;
        editor = sharedPreferences2.edit();
        BottomNavigationView bottomNav =getActivity().findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.getMenu().getItem(3).setChecked(true);
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        final View v = inflater.inflate(R.layout.alignment, container, false);
        show_help = v.findViewById(R.id.button_show_alignment_help);
        close_help = v.findViewById(R.id.button_hide_alignment_help);
        close_help_corner =  v.findViewById(R.id.button_close_alignment_help);
        alignment_help_frame = v.findViewById(R.id.frame_alignment_info);
        begin =  v.findViewById(R.id.button_begin);
        cancel = v.findViewById(R.id.button_cancel);
        next_star = v.findViewById(R.id.button_next_star);
        load_matrix =v.findViewById(R.id.button_load_matrix);
        next_star.setEnabled(false);
        text_step1 =  v.findViewById(R.id.text_step1);
        text_step2 =  v.findViewById(R.id.text_step2);
        text_step3 =  v.findViewById(R.id.text_step3);
        get_stars =  v.findViewById(R.id.button_get_stars);
        polar_alignment =  v.findViewById(R.id.button_polar_alignment);
        radioGroup = v.findViewById(R.id.radio_group_alignment);
        delete_west_extra_points=v.findViewById(R.id.button_delete_extra_points_west);
        delete_east_extra_points=v.findViewById(R.id.button_delete_extra_points_east);

        if (begin_alignment) {
           begin.setEnabled(false);
        }

        if (!begin_alignment) {
            next_star.setVisibility(View.GONE);
            text_step1.setVisibility(View.GONE);
            text_step2.setVisibility(View.GONE);
            text_step3.setVisibility(View.GONE);
            radioId = v.findViewById(R.id.radio1).getId();
        }
        if (star_number > alignment_stars_num) {
            next_star.setEnabled(false);
            next_star.setText("Alignment Completed!");
        }
        if (star_number <= alignment_stars_num) {
            next_star.setEnabled(true);
            next_star.setText(next_star_text);
        }
        if (!begin_alignment && viewModel.get_alignment_done().getValue()) {
            begin.setEnabled(false);
            next_star.setText("Using previous alignment ");
            next_star.setEnabled(false);
                    }
        show_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                alignment_help_frame.setVisibility(View.VISIBLE);
            }
        });
        close_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                alignment_help_frame.setVisibility(View.GONE);
            }
        });
        close_help_corner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                alignment_help_frame.setVisibility(View.GONE);
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!begin.isEnabled()) {
                    radioGroup.check(radioId);
                    if (!toast_msg_displayed_1_time) {
                        toast_msg_displayed_1_time = true;
                    }
                }
                if (begin.isEnabled()) {
                    radioId = radioGroup.getCheckedRadioButtonId();
                    radioButton = v.findViewById(radioId);
                    String radioText = radioButton.getText().toString();


                    switch (radioText) {
                        case "2 stars":
                            alignment_stars_num = 2;
                            calculate_3rd_star = true;
                            next_star_text = "Confirm Star " + star_number + " of " + alignment_stars_num + " is centered";
                            next_star.setText(next_star_text);
                            return;
                        case "4 stars":
                            alignment_stars_num = 4;
                            calculate_3rd_star = true;
                            next_star_text = "Confirm Star " + star_number + " of " + alignment_stars_num + " is centered";
                            next_star.setText(next_star_text);
                            return;
                        case "3 stars":
                            alignment_stars_num = 3;
                            calculate_3rd_star = false;
                            next_star_text = "Confirm Star " + star_number + " of " + alignment_stars_num + " is centered";
                            next_star.setText(next_star_text);
                            return;
                        case "6 stars":
                            alignment_stars_num = 6;
                            calculate_3rd_star = false;
                            next_star_text = "Confirm Star " + star_number + " of " + alignment_stars_num + " is centered";
                            next_star.setText(next_star_text);
                            return;

                        default:
                            return;
                    }
                }
            }
        });


        get_stars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                MainActivity.handler.obtainMessage(9, -1, -1).sendToTarget();
            }
        });
        polar_alignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                MainActivity.handler.obtainMessage(7, -1, -1).sendToTarget();
            }
        });
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                toast_msg_displayed_1_time = false;
                Arrays.fill(alignment_stars_matrix_east[0], 0.0d);
                Arrays.fill(alignment_stars_matrix_east[1], 0.0d);
                Arrays.fill(alignment_stars_matrix_east[2], 0.0d);
                Arrays.fill(alignment_stars_matrix_west[0], 0.0d);
                Arrays.fill(alignment_stars_matrix_west[1], 0.0d);
                Arrays.fill(alignment_stars_matrix_west[2], 0.0d);
                viewModel.set_alignment_ongoing(true);
                viewModel.set_alignment_done(false);
                west_array_counter = 0;
                east_array_counter = 0;
                text_step1.setVisibility(View.VISIBLE);
                text_step2.setVisibility(View.VISIBLE);
                text_step3.setVisibility(View.VISIBLE);
                begin_alignment = true;
                viewModel.set_begin_alignment(true);
                begin.setEnabled(false);
                star_number = 1;
                next_star.setEnabled(true);
                AlignmentFragment alignmentFragment = AlignmentFragment.this;
                alignmentFragment.next_star_text = "Confirm Star " + star_number + " of " + alignment_stars_num + " is Centered";
                next_star.setText(next_star_text);
                next_star.setVisibility(View.VISIBLE);
                editor.putBoolean("begin_alignment", begin_alignment);
                editor.apply();
                viewModel.setAlignmentcentroids(null);
                viewModel.setAlignmentpoints(null);
                temp_alignment_points_east.clear();
                temp_alignment_points_west.clear();
                viewModel.set_ra_initial_offset_east(0);
                viewModel.set_dec_initial_offset_east(0);
                viewModel.set_ra_initial_offset_west(0);
                viewModel.set_dec_initial_offset_west(0);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                next_star.setVisibility(View.GONE);
                text_step1.setText("Step 1 : Select and go to  a bright star.");
                text_step2.setText("Step 2 : Center the star and press \"STAR CENTERED\".");
                text_step3.setText("Step 3 : Confirm and repeat for the next alignment star");
                text_step1.setVisibility(View.GONE);
                text_step2.setVisibility(View.GONE);
                text_step3.setVisibility(View.GONE);
                begin_alignment = false;
                viewModel.set_begin_alignment(false);
                begin.setEnabled(true);
                next_star.setEnabled(false);
                viewModel.set_alignment_done(false);
                viewModel.setAlignmentcentroids(null);
                viewModel.setAlignmentpoints(null);
                editor.putBoolean("alignment_done", false);
                editor.putBoolean("begin_alignment", begin_alignment);
                editor.apply();
                viewModel.set_ra_initial_offset_east(0);
                viewModel.set_dec_initial_offset_east(0);
                viewModel.set_ra_initial_offset_west(0);
                viewModel.set_dec_initial_offset_west(0);
                viewModel.setAlignmentcentroids(null);
                viewModel.setAlignmentpoints(null);
            }
        });


        delete_east_extra_points.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                //delete all east centroids except from the initial one of the east side.
                int deleted_counter=0;
                if (viewModel.getAlignmentcentroids().getValue() != null) {
                    for (int i = 0; i < viewModel.getAlignmentcentroids().getValue().size(); i++) {
                       if(viewModel.getAlignmentcentroids().getValue().get(i).side_of_meridian.equals("east")
                        && !viewModel.getAlignmentcentroids().getValue().get(i).is_initial_alignment_centroid) {
                           viewModel.getAlignmentcentroids().getValue().remove(i);
                                                 }
                    }
                    viewModel.setAlignmentcentroids( viewModel.getAlignmentcentroids().getValue());
                    editor.putString("centroids", new Gson().toJson(viewModel.getAlignmentcentroids().getValue()));
                    editor.apply();
            }
                if (viewModel.getAlignmentpoints().getValue() != null) {
                    for (int i = 0; i < viewModel.getAlignmentpoints().getValue().size(); i++) {
                        if(viewModel.getAlignmentpoints().getValue().get(i).get_side_of_meridian().equals("east")
                                && !viewModel.getAlignmentpoints().getValue().get(i).is_initial_alignment_point) {
                            viewModel.getAlignmentpoints().getValue().remove(i);
                            deleted_counter=deleted_counter+1;
                        }
                    }
                    viewModel.setAlignmentpoints( viewModel.getAlignmentpoints().getValue());
                    editor.putString("alignment_points", new Gson().toJson(viewModel.getAlignmentpoints().getValue()));
                    editor.apply();

                }
                Toast.makeText(getContext(), deleted_counter+" extra alignment points deleted", Toast.LENGTH_SHORT).show();
            }
        });

        delete_west_extra_points.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                //delete all east centroids except from the initial one of the east side.
                int deleted_counter=0;
                if (viewModel.getAlignmentcentroids().getValue() != null) {
                    for (int i = 0; i < viewModel.getAlignmentcentroids().getValue().size(); i++) {
                        if(viewModel.getAlignmentcentroids().getValue().get(i).side_of_meridian.equals("west")
                                && !viewModel.getAlignmentcentroids().getValue().get(i).is_initial_alignment_centroid) {
                            viewModel.getAlignmentcentroids().getValue().remove(i);
                        }
                    }
                    viewModel.setAlignmentcentroids( viewModel.getAlignmentcentroids().getValue());
                    editor.putString("centroids", new Gson().toJson(viewModel.getAlignmentcentroids().getValue()));
                    editor.apply();
                }
                if (viewModel.getAlignmentpoints().getValue() != null) {
                    for (int i = 0; i < viewModel.getAlignmentpoints().getValue().size(); i++) {
                        if(viewModel.getAlignmentpoints().getValue().get(i).get_side_of_meridian().equals("west")
                                && !viewModel.getAlignmentpoints().getValue().get(i).is_initial_alignment_point) {
                            viewModel.getAlignmentpoints().getValue().remove(i);
                            deleted_counter=deleted_counter+1;
                        }
                    }
                    viewModel.setAlignmentpoints( viewModel.getAlignmentpoints().getValue());
                    editor.putString("alignment_points", new Gson().toJson(viewModel.getAlignmentpoints().getValue()));
                    editor.apply();
                }
                Toast.makeText(getContext(), deleted_counter+" extra alignment points deleted", Toast.LENGTH_SHORT).show();
            }
        });

        load_matrix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                              loadmatrix();
                begin.setEnabled(false);
                next_star.setText("Using previous alignment ");
                next_star.setEnabled(false);
                viewModel.set_alignment_done(true);
                editor.putBoolean("alignment_done", true);
                editor.putBoolean("begin_alignment", begin_alignment);
                editor.apply();
            }
        });

        viewModel.get_alignment_done().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean alignment_done) {
                if (viewModel.getAlignmentpoints().getValue() != null) {
                    if (alignment_done) {
                        delete_east_extra_points.setVisibility(View.VISIBLE);
                        delete_west_extra_points.setVisibility(View.VISIBLE);
                    }
                }
                if (!alignment_done) {
                    delete_east_extra_points.setVisibility(GONE);
                    delete_west_extra_points.setVisibility(GONE);
                }
            }
        });

        viewModel.getStar_object().observe(getViewLifecycleOwner(), new Observer<star>() {
            @Override
            public void onChanged(star star) {
                // ra=viewModel.getStar_object().getValue().getRaj2000();
                //  dec=viewModel.getStar_object().getValue().getDecj2000();


                viewModel.getStar_object().observe(getViewLifecycleOwner(), new Observer<star>() {
                    @Override
                    public void onChanged(star star) {
                        viewModel.getAlignment_time().observe(getViewLifecycleOwner(), new Observer<Double>() {
                            @Override
                            public void onChanged(Double alignment_time_from_main) {
                                alignment_star_name = viewModel.getStar_object().getValue().getName_ascii();
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

                                next_star.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if ((side_of_meridian.equals("east") && ((alignment_stars_num == 4 || alignment_stars_num == 6) && east_array_counter >= alignment_stars_num / 2)) || (side_of_meridian.equals("west") && ((alignment_stars_num == 4 || alignment_stars_num == 6) && west_array_counter >= alignment_stars_num / 2))) {
                                            Toast.makeText(getContext(), "Select a star on the opposite side of the meridian .", Toast.LENGTH_SHORT).show();
                                            next_star.setEnabled(false);
                                        }
                                        if (viewModel.getLatitute().getValue().doubleValue() < 0.0d) {
                                            ra_offset = -ra_offset;
                                        }

                                        if (east_array_counter == 0 && side_of_meridian.equals("east") && viewModel.get_include_initial_offsets().getValue()) {
                                            viewModel.set_ra_initial_offset_east(-ra_offset);
                                            viewModel.set_dec_initial_offset_east(-dec_offset);
                                            Toast.makeText(getContext(), "first point in east", Toast.LENGTH_SHORT).show();
                                        }
                                        if (west_array_counter == 0 && side_of_meridian.equals("west") && viewModel.get_include_initial_offsets().getValue()) {
                                            viewModel.set_ra_initial_offset_west(-ra_offset);
                                            viewModel.set_dec_initial_offset_west(dec_offset);
                                            Toast.makeText(getContext(), "first point in west", Toast.LENGTH_SHORT).show();
                                        }

                                        if ((side_of_meridian.equals("east") && ((alignment_stars_num == 4 || alignment_stars_num == 6) && east_array_counter < alignment_stars_num / 2)) || ((side_of_meridian.equals("west") && ((alignment_stars_num == 4 || alignment_stars_num == 6) && west_array_counter < alignment_stars_num / 2)) || alignment_stars_num == 2 || alignment_stars_num == 3)) {
                                            star_number++;
                                            if (star_number <= alignment_stars_num + 1) {
                                                Toast.makeText(getContext(), "received :" + ra + " ," + dec + " ," + ra_offset + " ," + dec_offset + " ," + initial_time + " ," + alignment_time, Toast.LENGTH_SHORT).show();
                                                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                                                viewModel.setRA_offset(0);
                                                viewModel.setDEC_offset(0);
                                                AlignmentFragment alignmentFragment = AlignmentFragment.this;
                                                alignmentFragment.next_star_text = "Confirm Star " + star_number + " of " + alignment_stars_num + " is centered";
                                                next_star.setText(next_star_text);
                                                next_star.setEnabled(false);
                                            }
                                            if (star_number > alignment_stars_num) {
                                                next_star.setText("Alignment Completed!");
                                                next_star.setEnabled(false);
                                            }
                                            add_to_alignment_stars_matrix(ra, ha_degrees, ra_offset, dec, dec_offset, alignment_time, initial_time, side_of_meridian);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

               /* next_star.post(new Runnable() {
                    @Override
                    public void run() {
                        next_star.performClick();
                    }
                });
*/
            }
        });


        return v;

    }


    private void add_to_alignment_stars_matrix(double ra, double ha_degrees, int ra_offset, double dec, int dec_offset, double alignment_time, double initial_time, String side_of_meridian) {

        double dif_in_RA_degrees = ((double) ra_offset) / RA_micro_1;
        double dif_in_DEC_degrees = ((double) dec_offset) / DEC_micro_1;
        double DEC_bias_degrees_east = ((double) viewModel.get_dec_initial_offset_east().getValue().intValue()) / DEC_micro_1;
        double DEC_bias_degrees_west = ((double) viewModel.get_dec_initial_offset_west().getValue().intValue()) / DEC_micro_1;
        double time_difference = alignment_time - initial_time;
        if(time_difference<0){time_difference=time_difference+24;}
        int stars_num_for_current_matrix = 0;
        if (calculate_3rd_star) {
            stars_num_for_current_matrix = 2;
        }
        if (!calculate_3rd_star) {
            stars_num_for_current_matrix = 3;
        }


        if (side_of_meridian.equals("west") && west_array_counter < stars_num_for_current_matrix) {
            Log.e(TAG, "added to west matrix : ");

            alignment_stars_matrix_west[west_array_counter][0] = ((ra * 15.0d) * PI) / 180.0d;
            alignment_stars_matrix_west[west_array_counter][1] = (((-ha_degrees) + dif_in_RA_degrees) * PI) / 180.0d;
            alignment_stars_matrix_west[west_array_counter][2] = (dec * PI) / 180.0d;
            alignment_stars_matrix_west[west_array_counter][3] = (((dec + dif_in_DEC_degrees) + DEC_bias_degrees_west) * PI) / 180.0d;
            if (west_array_counter == 0) {
                alignment_stars_matrix_west[west_array_counter][3] = (((dec + dif_in_DEC_degrees) + 0.0d) * PI) / 180.0d;
            }
            alignment_stars_matrix_west[west_array_counter][4] = ((time_difference * 15.0d) * PI) / 180.0d;
            west_array_counter = west_array_counter + 1;
            AlignmentPoint alignmentPoint = new AlignmentPoint(alignment_star_name, viewModel.getSide_of_meridian().getValue(), ((ra * 15.0d) * PI) / 180.0d, (((-ha_degrees) + dif_in_RA_degrees) * PI) / 180.0d, (dec * PI) / 180.0d, ((dec + dif_in_DEC_degrees) * PI) / 180.0d, ((time_difference * 15.0d) * PI) / 180.0d);
            alignmentPoint.is_initial_alignment_point = true;
            temp_alignment_points_west.add(alignmentPoint);
        }


        if (side_of_meridian.equals("east") && east_array_counter < stars_num_for_current_matrix) {

            Log.e(TAG, "added to east matrix : ");
            dif_in_DEC_degrees = -dif_in_DEC_degrees;

            alignment_stars_matrix_east[east_array_counter][0] = ((ra * 15.0d) * PI) / 180.0d;
            alignment_stars_matrix_east[east_array_counter][1] = (((-ha_degrees) + dif_in_RA_degrees) * PI) / 180.0d;
            alignment_stars_matrix_east[east_array_counter][2] = (dec * PI) / 180.0d;
            alignment_stars_matrix_east[east_array_counter][3] = (((dec + dif_in_DEC_degrees) + DEC_bias_degrees_east) * PI) / 180.0d;
            if (east_array_counter == 0) {
                alignment_stars_matrix_east[east_array_counter][3] = (((dec + dif_in_DEC_degrees) + 0.0d) * PI) / 180.0d;
            }
            alignment_stars_matrix_east[east_array_counter][4] = ((time_difference * 15.0d) * PI) / 180.0d;
            east_array_counter = east_array_counter + 1;
            AlignmentPoint alignmentPoint2 = new AlignmentPoint(alignment_star_name, viewModel.getSide_of_meridian().getValue(), ((ra * 15.0d) * PI) / 180.0d, (((-ha_degrees) + dif_in_RA_degrees) * PI) / 180.0d, (dec * PI) / 180.0d, ((dec + dif_in_DEC_degrees) * PI) / 180.0d, ((15.0d * time_difference) * PI) / 180.0d);
            alignmentPoint2.is_initial_alignment_point = true;
            temp_alignment_points_east.add(alignmentPoint2);
        }

        Log.e(TAG, "dif_in_RA_degrees : " + dif_in_RA_degrees);
        Log.e(TAG, "dif_in_DEC_degrees : " + dif_in_DEC_degrees);



        /*2 stars or 3 stars alignment is done , with all the alignment stars on the east side of the meridian  : */
        if ((alignment_stars_num == 2 || alignment_stars_num == 3) && west_array_counter == 0 && east_array_counter == alignment_stars_num) {
            alignment_stars_matrix_west = alignment_stars_matrix_east;
            west_array_counter = stars_num_for_current_matrix;
        }
        /*2 stars or 3 stars alignment is done , with all the alignment stars on the west side of the meridian  : */
        if ((alignment_stars_num == 2 || alignment_stars_num == 3) && east_array_counter == 0 && west_array_counter == alignment_stars_num) {
            alignment_stars_matrix_east = alignment_stars_matrix_west;
            east_array_counter = stars_num_for_current_matrix;
        }
            /*2 stars alignment , but the two stars are on both sides of the meridian . An extra alignment point with a declination difference of 1 degree is added to each side of the meridian .
            This is done so a transformation matrix can be created for each side of the meridian ,
            using the actual alignment star for this side and the extra alignment point with a declination difference of 1 degree : */
        if (west_array_counter == 1 && east_array_counter == 1 && alignment_stars_num == 2) {

            if (alignment_stars_matrix_east[1][0] == 0.0d && alignment_stars_matrix_west[1][0] == 0.0d) {
                alignment_stars_matrix_east[1][0] = alignment_stars_matrix_east[0][0];
                alignment_stars_matrix_east[1][1] = alignment_stars_matrix_east[0][1];
                alignment_stars_matrix_east[1][2] = alignment_stars_matrix_east[0][2] + 1 * PI / 180;
                alignment_stars_matrix_east[1][3] = alignment_stars_matrix_east[0][3] + 1 * PI / 180;
                alignment_stars_matrix_east[1][4] = alignment_stars_matrix_east[0][4];
                east_array_counter = east_array_counter + 1;

                AlignmentPoint theoritical_alignmentPoint_east = new AlignmentPoint(temp_alignment_points_east.get(0).getName() + " 2 ", "east", alignment_stars_matrix_east[1][0], alignment_stars_matrix_east[1][1], alignment_stars_matrix_east[1][2], alignment_stars_matrix_east[1][3], alignment_stars_matrix_east[1][4]);
                theoritical_alignmentPoint_east.is_initial_alignment_point = true;
                temp_alignment_points_east.add(theoritical_alignmentPoint_east);

                alignment_stars_matrix_west[1][0] = alignment_stars_matrix_west[0][0];
                alignment_stars_matrix_west[1][1] = alignment_stars_matrix_west[0][1];
                alignment_stars_matrix_west[1][2] = alignment_stars_matrix_west[0][2] + 1 * PI / 180;
                alignment_stars_matrix_west[1][3] = alignment_stars_matrix_west[0][3] + 1 * PI / 180;
                alignment_stars_matrix_west[1][4] = alignment_stars_matrix_west[0][4];
                west_array_counter++;

                AlignmentPoint theoritical_alignmentPoint_west = new AlignmentPoint(temp_alignment_points_west.get(0).getName() + " 2 ", "west", alignment_stars_matrix_west[1][0], alignment_stars_matrix_west[1][1], alignment_stars_matrix_west[1][2], alignment_stars_matrix_west[1][3], alignment_stars_matrix_west[1][4]);
                theoritical_alignmentPoint_west.is_initial_alignment_point = true;
                temp_alignment_points_west.add(theoritical_alignmentPoint_west);
            }
        }


             /*3 stars alignment , but two stars are on the West side of the meridian and the 3rd star is on the East side.
              An extra alignment point with a declination difference of 1 degree is added to the East side .
            This is done so a transformation matrix can be created for the East side ,
            using the actual alignment star and the extra alignment point with a declination difference of 1 degree : */
        if (west_array_counter == 2 && east_array_counter == 1 && alignment_stars_num == 3) {

            if (alignment_stars_matrix_east[1][0] == 0.0d && alignment_stars_matrix_west[2][0] == 0.0d) {
                calculate_3rd_star = true;
                alignment_stars_matrix_east[1][0] = alignment_stars_matrix_east[0][0];
                alignment_stars_matrix_east[1][1] = alignment_stars_matrix_east[0][1];
                alignment_stars_matrix_east[1][2] = alignment_stars_matrix_east[0][2] + 1 * PI / 180;
                alignment_stars_matrix_east[1][3] = alignment_stars_matrix_east[0][3] + 1 * PI / 180;
                alignment_stars_matrix_east[1][4] = alignment_stars_matrix_east[0][4];
                east_array_counter = east_array_counter + 1;

                AlignmentPoint theoritical_alignmentPoint_east = new AlignmentPoint(temp_alignment_points_east.get(0).getName() + " 2 ", "east", alignment_stars_matrix_east[1][0], alignment_stars_matrix_east[1][1], alignment_stars_matrix_east[1][2], alignment_stars_matrix_east[1][3], alignment_stars_matrix_east[1][4]);
                theoritical_alignmentPoint_east.is_initial_alignment_point = true;
                temp_alignment_points_east.add(theoritical_alignmentPoint_east);
                stars_num_for_current_matrix = 2;
            }
        }
                    /*3 stars alignment , but two stars are on the East side of the meridian and the 3rd star is on the West side.
              An extra alignment point with a declination difference of 1 degree is added to the West side .
            This is done so a transformation matrix can be created for the West side ,
            using the actual alignment star and the extra alignment point with a declination difference of 1 degree : */
        if (east_array_counter == 2 && west_array_counter == 1 && alignment_stars_num == 3) {

            if (alignment_stars_matrix_west[1][0] == 0.0d && alignment_stars_matrix_east[2][0] == 0.0d) {
                calculate_3rd_star = true;
                stars_num_for_current_matrix = 2;
                alignment_stars_matrix_west[1][0] = alignment_stars_matrix_west[0][0];
                alignment_stars_matrix_west[1][1] = alignment_stars_matrix_west[0][1];
                alignment_stars_matrix_west[1][2] = alignment_stars_matrix_west[0][2] + 1 * PI / 180;
                alignment_stars_matrix_west[1][3] = alignment_stars_matrix_west[0][3] + 1 * PI / 180;
                alignment_stars_matrix_west[1][4] = alignment_stars_matrix_west[0][4];
                west_array_counter = west_array_counter + 1;

                AlignmentPoint alignmentPoint23 = new AlignmentPoint(temp_alignment_points_west.get(0).getName() + " 2 ", "west", alignment_stars_matrix_west[1][0], alignment_stars_matrix_west[1][1], alignment_stars_matrix_west[1][2], alignment_stars_matrix_west[1][3], alignment_stars_matrix_west[1][4]);
                alignmentPoint23.is_initial_alignment_point = true;
                temp_alignment_points_west.add(alignmentPoint23);
            }
        }

                    /*At this point , both alignment_stars_matrix_west and alignment_stars_matrix_east contain information af at least 2 stars each .
                   Calculate the transformations for each side , create one centroid for each side and store the centroids and the alignment stars : */

        if (west_array_counter == stars_num_for_current_matrix && east_array_counter == stars_num_for_current_matrix) {
            Toast.makeText(getContext(), "Both alignment Matrices are complete !", Toast.LENGTH_SHORT).show();
            Matrix alignment_stars_east = new Matrix(alignment_stars_matrix_east);
            Log.e(TAG, "alignment_stars_matrix_east :\n " + strung(alignment_stars_east));
            Matrix alignment_stars_west = new Matrix(alignment_stars_matrix_west);
            Log.e(TAG, "alignment_stars_matrix_west :\n " + strung(alignment_stars_west));
            transformation_matrix_east = coords_transformations.transformation_matrix_construct(alignment_stars_matrix_east, calculate_3rd_star);
            transformation_matrix_west = coords_transformations.transformation_matrix_construct(alignment_stars_matrix_west, calculate_3rd_star);


            if (temp_alignment_points_west.size() != 0) {
                AlignmentCentroid centroid_west = new AlignmentCentroid();
                centroid_west.associate_point_1(temp_alignment_points_west.get(0));
                centroid_west.associate_point_2(temp_alignment_points_west.get(1));
                if (temp_alignment_points_west.size() == 2) {
                    double[] temp_centroid_coords_west = centroid_west.calculate_new_centroid(centroid_west.associated_point_1, centroid_west.associated_point_2, centroid_west.associated_point_2);
                    centroid_west.centroid_ra = temp_centroid_coords_west[0];
                    centroid_west.centroid_dec = temp_centroid_coords_west[1];
                }
                if (temp_alignment_points_west.size() == 3) {
                    centroid_west.associate_point_3(temp_alignment_points_west.get(2));
                    double[] temp_centroid_coords_west2 = centroid_west.calculate_new_centroid(centroid_west.associated_point_1, centroid_west.associated_point_2, centroid_west.associated_point_3);
                    centroid_west.centroid_ra = temp_centroid_coords_west2[0];
                    centroid_west.centroid_dec = temp_centroid_coords_west2[1];
                }
                centroid_west.associated_point_1.associate_matrix(transformation_matrix_west);
                centroid_west.associated_point_2.associate_matrix(transformation_matrix_west);

                if (temp_alignment_points_west.size() == 3) {
                    centroid_west.associated_point_3.associate_matrix(transformation_matrix_west);
                                    }
                centroid_west.is_initial_alignment_centroid=true;
                centroid_west.side_of_meridian = "west";
                temp_alignment_centroids.add(centroid_west);
            }


            if (temp_alignment_points_east.size() != 0) {
                AlignmentCentroid centroid_east = new AlignmentCentroid();
                centroid_east.associate_point_1(temp_alignment_points_east.get(0));
                centroid_east.associate_point_2(temp_alignment_points_east.get(1));
                if (temp_alignment_points_east.size() == 2) {
                    double[] temp_centroid_coords_east = centroid_east.calculate_new_centroid(centroid_east.associated_point_1, centroid_east.associated_point_2, centroid_east.associated_point_2);
                    centroid_east.centroid_ra = temp_centroid_coords_east[0];
                    centroid_east.centroid_dec = temp_centroid_coords_east[1];
                }
                if (temp_alignment_points_east.size() == 3) {
                    centroid_east.associate_point_3(temp_alignment_points_east.get(2));
                    double[] temp_centroid_coords_east2 = centroid_east.calculate_new_centroid(centroid_east.associated_point_1, centroid_east.associated_point_2, centroid_east.associated_point_3);
                    centroid_east.centroid_ra = temp_centroid_coords_east2[0];
                    centroid_east.centroid_dec = temp_centroid_coords_east2[1];
                }
                centroid_east.associated_point_1.associate_matrix(transformation_matrix_east);
                centroid_east.associated_point_2.associate_matrix(transformation_matrix_east);

                if (temp_alignment_points_east.size() == 3) {
                    centroid_east.associated_point_3.associate_matrix(transformation_matrix_east);
                }
                centroid_east.side_of_meridian = "east";
                centroid_east.is_initial_alignment_centroid=true;
                temp_alignment_centroids.add(centroid_east);
            }
            if (viewModel.getAlignmentcentroids().getValue() == null) {
                viewModel.setAlignmentcentroids(temp_alignment_centroids);
            }
            temp_alignment_points_east.addAll(temp_alignment_points_west);
            if (viewModel.getAlignmentpoints().getValue() == null) {
                viewModel.setAlignmentpoints(temp_alignment_points_east);
                viewModel.set_allow_extra_points(true);
                editor.putInt("number_of_stars", alignment_stars_num);
                editor.apply();
            }
            Log.e(TAG, "transformation_matrix east  :\n " + strung(transformation_matrix_east));
            Log.e(TAG, "transformation_matrix west  :\n " + strung(transformation_matrix_west));
            viewModel.set_alignment_done(true);
            viewModel.set_alignment_ongoing(false);
            viewModel.set_ra_initial_offset_east(0);
            viewModel.set_dec_initial_offset_east(0);
            viewModel.set_ra_initial_offset_west(0);
            viewModel.set_dec_initial_offset_west(0);
            viewModel.setTransformation_matrix_west(transformation_matrix_west);
            viewModel.setTransformation_matrix_east(transformation_matrix_east);
            str_matrix_west = new Gson().toJson(transformation_matrix_west);
            String json = new Gson().toJson(transformation_matrix_east);
            str_matrix_east = json;
            saveMatrix(json, str_matrix_west, viewModel.getInitial_time().getValue());
            editor.putString("alignment_points", new Gson().toJson(viewModel.getAlignmentpoints().getValue()));
            editor.putString("centroids", new Gson().toJson(viewModel.getAlignmentcentroids().getValue()));
            editor.putBoolean("alignment_done", true);
            editor.putBoolean("begin_alignment", begin_alignment);
            editor.apply();

        }


    }


    public void saveMatrix(String matrix_east, String matrix_west, Double initial_time) {
        viewModel.setAlignment_julian_day(current_julianday());
        editor.putFloat("alignment_julianday", (float) current_julianday());
        editor.putString("matrix_e", matrix_east);
        editor.putString("matrix_w", matrix_west);
        editor.putLong(saved_initial_time, Double.doubleToRawLongBits(initial_time.doubleValue()));
        editor.putString("one_sided_alignment", viewModel.get_one_sided_alignment().getValue());
        editor.apply();
        Toast.makeText(getContext(), "Alignment Saved", Toast.LENGTH_SHORT).show();
    }


    public void loadmatrix() {
        SharedPreferences sharedPreferences2 = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        String temp_str_east = sharedPreferences2.getString("matrix_e", "");
        String temp_str_west = sharedPreferences2.getString("matrix_w", "");
        editor2.commit();
        if (!temp_str_east.equals("") && !temp_str_west.equals("")) {
            Matrix transformation_matrix_east = new Gson().fromJson(temp_str_east, Matrix.class);
            Matrix transformation_matrix_west = new Gson().fromJson(temp_str_west, Matrix.class);
            viewModel.set_alignment_done(true);
            Log.e(TAG, "transformation_matrix east  :\n " + strung(transformation_matrix_east));
            Log.e(TAG, "transformation_matrix west  :\n " + strung(transformation_matrix_west));
            viewModel.setTransformation_matrix_west(transformation_matrix_west);
            viewModel.setTransformation_matrix_east(transformation_matrix_east);
            double longBitsToDouble = Double.longBitsToDouble(sharedPreferences2.getLong(saved_initial_time, Double.doubleToLongBits(0.0d)));
            initial_time = longBitsToDouble;
            viewModel.setInitial_time(initial_time);
            viewModel.setAlignment_julian_day((double) sharedPreferences2.getFloat("alignment_julianday", 0.0f));
            viewModel.set_one_sided_alignment(sharedPreferences2.getString("one_sided_alignment", ""));
            Toast.makeText(getContext(), "Alignment Loaded", Toast.LENGTH_SHORT).show();
        }
        load_alignment_points();
    }
    //stored_address = sharedPreferences1.getString(MAC_ADDRESS, "24:42:16:08:00:00");
    // stored_device = sharedPreferences1.getString(BT_DEVICE, "telescope");

    //editor1.apply();

    private void load_alignment_points() {
        Type AlignmentPointslistType = new TypeToken<ArrayList<AlignmentPoint>>() {
        }.getType();
        Type AlignmentCentroidslistType = new TypeToken<ArrayList<AlignmentCentroid>>() {
        }.getType();
        viewModel.setAlignmentpoints((ArrayList) new Gson().fromJson(sharedPreferences.getString("alignment_points", ""), AlignmentPointslistType));
        viewModel.setAlignmentcentroids((ArrayList) new Gson().fromJson(sharedPreferences.getString("centroids", ""), AlignmentCentroidslistType));
        if (viewModel.getAlignmentpoints().getValue() != null) {
            Toast.makeText(getContext(), "extra alignment points loaded", Toast.LENGTH_SHORT).show();
        }
    }


    private double current_julianday() {
        //Calculations of current Julian day:
        LocalDateTime localtime = LocalDateTime.now();
        //LocalDateTime UTCtime = LocalDateTime.of(2021,1,7,4,30,15);

        //double decimal_time2=UTCtime.getHour()+UTCtime.getMinute()/60.0+UTCtime.getSecond()/3600.0;
        int year = localtime.getYear();
        int month = localtime.getMonthValue();
        int day = localtime.getDayOfMonth();
        int hour = localtime.getHour();
        int minute = localtime.getMinute();
        int second = localtime.getSecond();


        if (month <= 2) { // January & February
            year = year - 1;
            month = month + 12;
        }
        double dayFraction = (hour + minute / 60.0 + second / 3600.0) / 24.0;
        double day2 = floor(365.25 * (year + 4716.0)) + floor(30.6001 * (month + 1.0)) + 2.0 - floor(year / 100.0) + floor(floor(year / 100.0) / 4.0) + day - 1524.5;
        double JD = dayFraction + day2;
        return JD;
    }

    public static String strung(Matrix m) {
        StringBuffer sb = new StringBuffer();
        for (int r = 0; r < m.getRowDimension(); ++r) {
            for (int c = 0; c < m.getColumnDimension(); ++c)
                sb.append(m.get(r, c)).append("\t");
            sb.append("\n");
        }
        return sb.toString();
    }


}
