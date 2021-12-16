package com.my_project.telescopecontrol;

import Jama.Matrix;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static android.content.ContentValues.TAG;
import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.util.stream.Collectors.toList;

public class ExtraAlignments {
    String Alignment_points_list_json_str = "";
    double DEC_micro_1;
    double RA_micro_1;
    double alignment_time;
    String centroids_list_json_str = "";
    private Context context;
    double dec;
    double dec_offset;
    double dec_transformed;
    SharedPreferences.Editor editor;
    double[][] extra_alignment_stars_matrix = new double[3][5];
    CoordinatesTransformations extra_coords_transformations = new CoordinatesTransformations();
    Matrix extra_transformation_matrix;
    double ha_degrees;
    String name;
    AlignmentPoint new_alignmentPoint;
    double ra;
    double ra_offset;
    SharedPreferences sharedPreferences;
    String side_of_meridian;
    private SharedViewModel viewModel;

    public ExtraAlignments(Context activityContext, SharedViewModel sharedviewModel) {
        this.viewModel = sharedviewModel;
        this.context = activityContext;
        this.sharedPreferences = activityContext.getSharedPreferences("sharedPrefs", 0);
        this.editor = sharedPreferences.edit();
    }


    public void add_point_to_alignment(String name, double ra, double ha_degrees, double ra_offset, double dec, double dec_transformed, double dec_offset, double alignment_time) {
        this.name = name;
        this.ra = ra;
        this.ha_degrees = ha_degrees;
        this.ra_offset = ra_offset;
        this.dec = dec;
        this.dec_transformed = dec_transformed;
        this.dec_offset = dec_offset;
        this.alignment_time = alignment_time;
        RA_micro_1 = viewModel.getRA_micro_1().getValue();
        DEC_micro_1 = viewModel.getDEC_micro_1().getValue();

        double initial_time = viewModel.getInitial_time().getValue();
       // viewModel.setInitial_time(initial_time);
        side_of_meridian = viewModel.getSide_of_meridian().getValue();
        double time_difference = alignment_time - initial_time;
        if(time_difference<0){time_difference=time_difference+24;}
        double dif_in_RA_degrees = ra_offset / RA_micro_1;
        double dif_in_DEC_degrees = dec_offset / DEC_micro_1;
        if (side_of_meridian.equals("east")) {
            dif_in_DEC_degrees = -dif_in_DEC_degrees;
        }
        boolean calculate_3rd_star = false;

        new_alignmentPoint = new AlignmentPoint(name, viewModel.getSide_of_meridian().getValue(), ((ra * 15.0d) * PI) / 180.0d, (((-ha_degrees) + dif_in_RA_degrees) * PI) / 180.0d, (dec * PI) / 180.0d, ((dec_transformed + dif_in_DEC_degrees) * PI) / 180.0d, ((15.0d * time_difference) * PI) / 180.0d);
        double new_alignmentPoint_ra = new_alignmentPoint.get_non_transformed_ra();
        double new_alignmentPoint_dec = new_alignmentPoint.get_non_transformed_dec();
        int centroid_points_num = viewModel.getAlignmentcentroids().getValue().size();
        double[] distance = new double[centroid_points_num];


        for (int i = 0; i < centroid_points_num; i++) {
            double saved_centroid_ra = viewModel.getAlignmentcentroids().getValue().get(i).centroid_ra;
            double saved_centroid_dec = viewModel.getAlignmentcentroids().getValue().get(i).centroid_dec;
            distance[i] = acos((sin(new_alignmentPoint.get_corrected_dec()) * sin(saved_centroid_dec)) + (cos(new_alignmentPoint.get_corrected_dec()) * cos(saved_centroid_dec) * cos(new_alignmentPoint.get_non_transformed_ra() - saved_centroid_ra)));
            if (!(viewModel.getAlignmentcentroids().getValue().get(i).associated_point_3 == null || 0 == 0 || !(viewModel.getAlignmentcentroids().getValue().get(i).associated_point_1.getName().equals(new_alignmentPoint.getName()) || viewModel.getAlignmentcentroids().getValue().get(i).associated_point_2.getName().equals(new_alignmentPoint.getName()) || viewModel.getAlignmentcentroids().getValue().get(i).associated_point_3.getName().equals(new_alignmentPoint.getName())))) {
                distance[i] = distance[i] + 1000.0d;
            }
            if (!viewModel.getSide_of_meridian().getValue().equals(viewModel.getAlignmentcentroids().getValue().get(i).side_of_meridian)) {
                distance[i] = distance[i] + 100000.0d;
            }

        }


        AlignmentCentroid nearest_centroid = viewModel.getAlignmentcentroids().getValue().get((DoubleStream.of(distance).boxed().collect(toList())).indexOf(Double.valueOf(DoubleStream.of(distance).min().getAsDouble())));

        if (nearest_centroid.associated_point_3 != null) {
            double[] distances = {0.0d, 0.0d, 0.0d};
            double[][] triangle_points_coords = new double[3][2];
            triangle_points_coords[0][0] = nearest_centroid.associated_point_1.get_non_transformed_ra();
            triangle_points_coords[0][1] = nearest_centroid.associated_point_1.get_non_transformed_dec();
            triangle_points_coords[1][0] = nearest_centroid.associated_point_2.get_non_transformed_ra();
            triangle_points_coords[1][1] = nearest_centroid.associated_point_2.get_non_transformed_dec();
            triangle_points_coords[2][0] = nearest_centroid.associated_point_3.get_non_transformed_ra();
            triangle_points_coords[2][1] = nearest_centroid.associated_point_3.get_non_transformed_dec();
            for (int i = 0; i < 3; i++) {
                distances[i] = acos((sin(new_alignmentPoint_dec) * sin(triangle_points_coords[i][1])) + (cos(new_alignmentPoint_dec) * cos(triangle_points_coords[i][1]) * cos(new_alignmentPoint_ra - triangle_points_coords[i][0])));
            }
            OptionalDouble minimun = DoubleStream.of(distances).min();
            int index = DoubleStream.of(distances).boxed().collect(toList()).indexOf(minimun.getAsDouble());
            AlignmentPoint nearest_alignment_point = null;
            AlignmentPoint farthest_alignment_point_1 = null;
            AlignmentPoint farthest_alignment_point_2 = null;
            AlignmentPoint accepted_alignment_point = null;

            if (index == 0) {
                nearest_alignment_point = nearest_centroid.associated_point_1;
                farthest_alignment_point_1 = nearest_centroid.associated_point_2;
                farthest_alignment_point_2 = nearest_centroid.associated_point_3;
            }
            if (index == 1) {
                nearest_alignment_point = nearest_centroid.associated_point_2;
                farthest_alignment_point_1 = nearest_centroid.associated_point_1;
                farthest_alignment_point_2 = nearest_centroid.associated_point_3;
            }
            if (index == 2) {
                AlignmentPoint nearest_alignment_point2 = nearest_centroid.associated_point_3;
                AlignmentPoint nearest_alignment_point3 = nearest_centroid.associated_point_1;
                farthest_alignment_point_2 = nearest_centroid.associated_point_2;
                farthest_alignment_point_1 = nearest_alignment_point3;
                nearest_alignment_point = nearest_alignment_point2;
            }
            Log.e(TAG, "Nearest star of the centroid is: " + nearest_alignment_point.getName());

            double[] candidate_centroid_1 = nearest_centroid.calculate_new_centroid(new_alignmentPoint, nearest_alignment_point, farthest_alignment_point_1);
            double[] candidate_centroid_2 = nearest_centroid.calculate_new_centroid(new_alignmentPoint, nearest_alignment_point, farthest_alignment_point_2);
            double centroid_distance_1 = acos((sin(candidate_centroid_1[1]) * sin(nearest_centroid.centroid_dec)) + (cos(candidate_centroid_1[1]) * cos(nearest_centroid.centroid_dec) * cos(candidate_centroid_1[0] - nearest_centroid.centroid_ra)));
            double centroid_distance_2 = acos((sin(candidate_centroid_2[1]) * sin(nearest_centroid.centroid_dec)) + (cos(candidate_centroid_2[1]) * cos(nearest_centroid.centroid_dec) * cos(candidate_centroid_2[0] - nearest_centroid.centroid_ra)));
            if (centroid_distance_1 >= centroid_distance_2) {
                if (index == 0) {
                    accepted_alignment_point = nearest_centroid.associated_point_2;
                }
                if (index == 1) {
                    accepted_alignment_point = nearest_centroid.associated_point_1;
                }
                if (index == 2) {
                    accepted_alignment_point = nearest_centroid.associated_point_1;
                }
            }
            if (centroid_distance_2 >= centroid_distance_1) {
                if (index == 0) {
                    accepted_alignment_point = nearest_centroid.associated_point_3;
                }
                if (index == 1) {
                    accepted_alignment_point = nearest_centroid.associated_point_3;
                }
                if (index == 2) {
                    accepted_alignment_point = nearest_centroid.associated_point_2;
                }
            }
            Log.e(TAG, "Nearest ACCEPTED star of the new centroid is: " + accepted_alignment_point.getName());
            AlignmentCentroid new_centroid = new AlignmentCentroid();
            double[] new_centroid_coords = new_centroid.calculate_new_centroid(new_alignmentPoint, nearest_alignment_point, accepted_alignment_point);
            new_centroid.centroid_ra = new_centroid_coords[0];
            new_centroid.centroid_dec = new_centroid_coords[1];
            new_centroid.associate_point_1(new_alignmentPoint);
            new_centroid.associate_point_2(nearest_alignment_point);
            new_centroid.associate_point_3(accepted_alignment_point);
            Log.e(TAG, " ACCEPTED new centroid RA is : " + new_centroid.centroid_ra);
            Log.e(TAG, " ACCEPTED new centroid DEC is : " + new_centroid.centroid_dec);

            extra_alignment_stars_matrix[0][0] = new_alignmentPoint.get_non_transformed_ra();
            extra_alignment_stars_matrix[0][1] = new_alignmentPoint.get_corrected_ha();
            extra_alignment_stars_matrix[0][2] = new_alignmentPoint.get_non_transformed_dec();
            extra_alignment_stars_matrix[0][3] = new_alignmentPoint.get_corrected_dec();
            extra_alignment_stars_matrix[0][4] = new_alignmentPoint.get_time_difference();

            extra_alignment_stars_matrix[1][0] = nearest_alignment_point.get_non_transformed_ra();
            extra_alignment_stars_matrix[1][1] = nearest_alignment_point.get_corrected_ha();
            extra_alignment_stars_matrix[1][2] = nearest_alignment_point.get_non_transformed_dec();
            extra_alignment_stars_matrix[1][3] = nearest_alignment_point.get_corrected_dec();
            extra_alignment_stars_matrix[1][4] = nearest_alignment_point.get_time_difference();

            extra_alignment_stars_matrix[2][0] = accepted_alignment_point.get_non_transformed_ra();
            extra_alignment_stars_matrix[2][1] = accepted_alignment_point.get_corrected_ha();
            extra_alignment_stars_matrix[2][2] = accepted_alignment_point.get_non_transformed_dec();
            extra_alignment_stars_matrix[2][3] = accepted_alignment_point.get_corrected_dec();
            extra_alignment_stars_matrix[2][4] = accepted_alignment_point.get_time_difference();

            extra_transformation_matrix = extra_coords_transformations.transformation_matrix_construct(extra_alignment_stars_matrix, false);
            new_centroid.side_of_meridian = viewModel.getSide_of_meridian().getValue();
            viewModel.getAlignmentcentroids().getValue().add(new_centroid);
            new_alignmentPoint.associate_matrix(extra_transformation_matrix);
        }
        if (nearest_centroid.associated_point_3 == null) {
            nearest_centroid.associated_point_3 = new_alignmentPoint;

            extra_alignment_stars_matrix[0][0] = nearest_centroid.associated_point_1.get_non_transformed_ra();
            extra_alignment_stars_matrix[0][1] = nearest_centroid.associated_point_1.get_corrected_ha();
            extra_alignment_stars_matrix[0][2] = nearest_centroid.associated_point_1.get_non_transformed_dec();
            extra_alignment_stars_matrix[0][3] = nearest_centroid.associated_point_1.get_corrected_dec();
            extra_alignment_stars_matrix[0][4] = nearest_centroid.associated_point_1.get_time_difference();

            extra_alignment_stars_matrix[1][0] = nearest_centroid.associated_point_2.get_non_transformed_ra();
            extra_alignment_stars_matrix[1][1] = nearest_centroid.associated_point_2.get_corrected_ha();
            extra_alignment_stars_matrix[1][2] = nearest_centroid.associated_point_2.get_non_transformed_dec();
            extra_alignment_stars_matrix[1][3] = nearest_centroid.associated_point_2.get_corrected_dec();
            extra_alignment_stars_matrix[1][4] = nearest_centroid.associated_point_2.get_time_difference();

            extra_alignment_stars_matrix[2][0] = nearest_centroid.associated_point_3.get_non_transformed_ra();
            extra_alignment_stars_matrix[2][1] = nearest_centroid.associated_point_3.get_corrected_ha();
            extra_alignment_stars_matrix[2][2] = nearest_centroid.associated_point_3.get_non_transformed_dec();
            extra_alignment_stars_matrix[2][3] = nearest_centroid.associated_point_3.get_corrected_dec();
            extra_alignment_stars_matrix[2][4] = nearest_centroid.associated_point_3.get_time_difference();

            if (!check_before_transformation_construction(nearest_centroid.associated_point_1, nearest_centroid.associated_point_2, nearest_centroid.associated_point_3)) {
                Toast.makeText(context, "FAILED to add the alignmnent point . Please select another point. ", Toast.LENGTH_SHORT).show();
            }

            Matrix transformation_matrix_construct = extra_coords_transformations.transformation_matrix_construct(extra_alignment_stars_matrix, false);
            extra_transformation_matrix = transformation_matrix_construct;
            new_alignmentPoint.associate_matrix(transformation_matrix_construct);
            double[] temp_centroid_coords = nearest_centroid.calculate_new_centroid(nearest_centroid.associated_point_1, nearest_centroid.associated_point_2, nearest_centroid.associated_point_3);
            nearest_centroid.centroid_ra = temp_centroid_coords[0];
            nearest_centroid.centroid_dec = temp_centroid_coords[1];
            nearest_centroid.side_of_meridian = viewModel.getSide_of_meridian().getValue();
            new_alignmentPoint.associate_matrix(extra_transformation_matrix);
        }

            add_point();

    }

    public boolean check_before_transformation_construction(AlignmentPoint point1, AlignmentPoint point2, AlignmentPoint point3) {
        if (point1.getName().equals(point2.getName()) && point1.get_corrected_ha() == point2.get_corrected_ha() && point1.get_time_difference() == point2.get_time_difference()) {
            return false;
        }
        if (point1.getName().equals(point3.getName()) && point1.get_corrected_ha() == point3.get_corrected_ha() && point1.get_time_difference() == point3.get_time_difference()) {
            return false;
        }
        if (point2.getName().equals(point3.getName()) && point2.get_corrected_ha() == point3.get_corrected_ha() && point2.get_time_difference() == point3.get_time_difference()) {
            return false;
        }
        return true;
    }


    public void add_point() {
        if (new_alignmentPoint.associated_transformation_matrix == null) {
            Toast.makeText(context, "FAILED3 to add the alignmnent point . Please select another point. ", Toast.LENGTH_SHORT).show();
            return;
        }
        AlignmentCentroid last_added_centroid = viewModel.getAlignmentcentroids().getValue().get(viewModel.getAlignmentcentroids().getValue().size() - 1);
        if (!last_added_centroid.associated_point_1.getName().equals(last_added_centroid.associated_point_2.getName()) || !last_added_centroid.associated_point_1.getName().equals(last_added_centroid.associated_point_3.getName())) {
            viewModel.getAlignmentpoints().getValue().add(new_alignmentPoint);
            Toast.makeText(context, "alignment point added ", Toast.LENGTH_SHORT).show();
            centroids_list_json_str = new Gson().toJson(viewModel.getAlignmentcentroids().getValue());
            String json = new Gson().toJson(viewModel.getAlignmentpoints().getValue());
            Alignment_points_list_json_str = json;
            saveExtraPoints(json, centroids_list_json_str);
            return;
        }
        Toast.makeText(context, "Point already added .", Toast.LENGTH_SHORT).show();
        viewModel.getAlignmentcentroids().getValue().remove(viewModel.getAlignmentcentroids().getValue().size() - 1);
    }




    public void saveExtraPoints(String Alignment_points, String centroids_list) {
        if (viewModel.get_allow_extra_points().getValue().booleanValue()) {
            editor.putString("alignment_points", Alignment_points);
            editor.putString("centroids", centroids_list);
            editor.apply();
            Toast.makeText(context, "point saved", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "Extra point valid ONLY for current session .", Toast.LENGTH_SHORT).show();
    }
}
