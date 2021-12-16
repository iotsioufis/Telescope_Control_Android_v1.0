package com.my_project.telescopecontrol;

import android.util.Log;

import Jama.Matrix;

import static android.content.ContentValues.TAG;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class AlignmentCentroid {

    Matrix associated_transformation_matrix;
    AlignmentPoint associated_point_1;
    AlignmentPoint associated_point_2;
    AlignmentPoint associated_point_3;
    public double centroid_dec;
    public double centroid_ra;
    public String side_of_meridian;
    public boolean is_initial_alignment_centroid = false;


    public void associate_point_1(AlignmentPoint point) {
        this.associated_point_1 = point;
    }

    public void associate_point_2(AlignmentPoint point) {
        this.associated_point_2 = point;
    }

    public void associate_point_3(AlignmentPoint point) {
        this.associated_point_3 = point;
    }


    public double[] calculate_new_centroid(AlignmentPoint point1, AlignmentPoint point2, AlignmentPoint point3) {
        double[] new_centroid_coords = new double[2];
        double x1 = cos(point1.get_non_transformed_ra()) * cos(point1.get_non_transformed_dec());
        double y1 = sin(point1.get_non_transformed_ra()) * cos(point1.get_non_transformed_dec());
        double z1 = sin(point1.get_non_transformed_dec());

        double x2 = cos(point2.get_non_transformed_ra()) * cos(point2.get_non_transformed_dec());
        double y2 = sin(point2.get_non_transformed_ra()) * cos(point2.get_non_transformed_dec());
        double z2 = sin(point2.get_non_transformed_dec());

        double x3 = cos(point3.get_non_transformed_ra()) * cos(point3.get_non_transformed_dec());
        double y3 = sin(point3.get_non_transformed_ra()) * cos(point3.get_non_transformed_dec());
        double z3 = sin(point3.get_non_transformed_dec());

        double x_centroid = (x1 + x2 + x3) / 3;
        double y_centroid = (y1 + y2 + y3) / 3;
        double z_centroid = (z1 + z2 + z3) / 3;

        new_centroid_coords[0] = atan2(y_centroid, x_centroid);
        new_centroid_coords[1] = asin(z_centroid);




       /* new_centroid_coords[0]=(point1.get_non_transformed_ra()+point2.get_non_transformed_ra()+point3.get_non_transformed_ra()) /3 ;
        new_centroid_coords[1]=(point1.get_non_transformed_dec()+point2.get_non_transformed_dec()+point3.get_non_transformed_dec()) /3 ;*/

       /* Log.e(TAG, "new centroid ra : " +   new_centroid_coords[0]+"new centroid dec: "+new_centroid_coords[1]);
        Log.e(TAG, "point2.get_non_transformed_ra: " +  point2.get_non_transformed_ra());
        Log.e(TAG, "point3.get_non_transformed_ra: " +  point3.get_non_transformed_ra());*/
        return new_centroid_coords;

    }

}
