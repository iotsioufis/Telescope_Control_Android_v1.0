package com.my_project.telescopecontrol;

import android.util.Log;

import java.lang.reflect.Array;

import Jama.Matrix;

import static android.content.ContentValues.TAG;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class CoordinatesTransformations {
    double k = 1.002737908;

    public Matrix transformation_matrix_construct(double[][] star_alignment_matrix, boolean calculate_3rd_star) {
        Matrix transformation_matrix;
        Matrix alignment_stars = new Matrix(star_alignment_matrix);
        double[][] telescope_dir_cosine_array = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        double[][] equatorial_dir_cosine_array = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};

       /*  getMatrix:Get a submatrix.
             Parameters:
        i0 - Initial row index
        i1 - Final row index
        j0 - Initial column index
        j1 - Final column index */
        double[] alpha = alignment_stars.getMatrix(0, 2, 0, 0).getColumnPackedCopy();
        double[] delta = alignment_stars.getMatrix(0, 2, 2, 2).getColumnPackedCopy();
        double[] phi = alignment_stars.getMatrix(0, 2, 1, 1).getColumnPackedCopy();
        double[] theta = alignment_stars.getMatrix(0, 2, 3, 3).getColumnPackedCopy();
        double[] t = alignment_stars.getMatrix(0, 2, 4, 4).getColumnPackedCopy();


        double[][] telescope_dir_cosine = {
                {cos(theta[0]) * cos(phi[0]), cos(theta[1]) * cos(phi[1]), cos(theta[2]) * cos(phi[2])},
                {cos(theta[0]) * sin(phi[0]), cos(theta[1]) * sin(phi[1]), cos(theta[2]) * sin(phi[2])},
                {sin(theta[0]), sin(theta[1]), sin(theta[2])},
        };

        double[][] equatorial_dir_cosine = {
                {cos(delta[0]) * cos(alpha[0] - k * t[0]), cos(delta[1]) * cos(alpha[1] - k * t[1]), cos(delta[2]) * cos(alpha[2] - k * t[2])},
                {cos(delta[0]) * sin(alpha[0] - k * t[0]), cos(delta[1]) * sin(alpha[1] - k * t[1]), cos(delta[2]) * sin(alpha[2] - k * t[2])},
                {sin(delta[0]), sin(delta[1]), sin(delta[2])},
        };

        double l1 = telescope_dir_cosine[0][0];
        double m1 = telescope_dir_cosine[1][0];
        double n1 = telescope_dir_cosine[2][0];

        double l2 = telescope_dir_cosine[0][1];
        double m2 = telescope_dir_cosine[1][1];
        double n2 = telescope_dir_cosine[2][1];

        double le1 = equatorial_dir_cosine[0][0];
        double me1 = equatorial_dir_cosine[1][0];
        double ne1 = equatorial_dir_cosine[2][0];

        double le2 = equatorial_dir_cosine[0][1];
        double me2 = equatorial_dir_cosine[1][1];
        double ne2 = equatorial_dir_cosine[2][1];


        if (calculate_3rd_star) {

            double[] tempvector = {m1 * n2 - n1 * m2, n1 * l2 - l1 * n2, l1 * m2 - m1 * l2};
            Matrix temp_vector = new Matrix(tempvector, 1);

            Matrix telescope_dir_cosine3 = temp_vector.times(1 / (sqrt(pow((m1 * n2 - n1 * m2), 2) + pow((n1 * l2 - l1 * n2), 2) + pow((l1 * m2 - m1 * l2), 2))));

            double l3 = telescope_dir_cosine3.transpose().get(0, 0);
            double m3 = telescope_dir_cosine3.transpose().get(1, 0);
            double n3 = telescope_dir_cosine3.transpose().get(2, 0);
            telescope_dir_cosine_array = new double[][]{{l1, l2, l3}, {m1, m2, m3}, {n1, n2, n3}};


            double[] tempvector2 = {me1 * ne2 - ne1 * me2, ne1 * le2 - le1 * ne2, le1 * me2 - me1 * le2};
            Matrix temp_vector2 = new Matrix(tempvector2, 1);
            Matrix equatorial_dir_cosine3 = temp_vector2.times(1 / (sqrt(pow((me1 * ne2 - ne1 * me2), 2) + pow((ne1 * le2 - le1 * ne2), 2) + pow((le1 * me2 - me1 * le2), 2))));

            double le3 = equatorial_dir_cosine3.transpose().get(0, 0);
            double me3 = equatorial_dir_cosine3.transpose().get(1, 0);
            double ne3 = equatorial_dir_cosine3.transpose().get(2, 0);
            equatorial_dir_cosine_array = new double[][]{{le1, le2, le3}, {me1, me2, me3}, {ne1, ne2, ne3}};

        }

        if (!calculate_3rd_star) {

            double l3 = telescope_dir_cosine[0][2];
            double m3 = telescope_dir_cosine[1][2];
            double n3 = telescope_dir_cosine[2][2];
            double le3 = equatorial_dir_cosine[0][2];
            double me3 = equatorial_dir_cosine[1][2];
            double ne3 = equatorial_dir_cosine[2][2];


            telescope_dir_cosine_array = new double[][]{{l1, l2, l3}, {m1, m2, m3}, {n1, n2, n3}};

            Matrix telescope_dir_cosine_1_2_3 = new Matrix(telescope_dir_cosine_array);

            equatorial_dir_cosine_array = new double[][]{{le1, le2, le3}, {me1, me2, me3}, {ne1, ne2, ne3}};
            Matrix equatorial_dir_cosine_1_2_3 = new Matrix(equatorial_dir_cosine_array);

        }

        Matrix telescope_dir_cosine_1_2_3 = new Matrix(telescope_dir_cosine_array);
        Matrix equatorial_dir_cosine_1_2_3 = new Matrix(equatorial_dir_cosine_array);
        transformation_matrix = telescope_dir_cosine_1_2_3.times(equatorial_dir_cosine_1_2_3.inverse());


        Log.e(TAG, "alignment_stars_matrix inside CoordinatesTansformatios :\n " + strung(alignment_stars));
        return transformation_matrix;

    }


    public double[] get_transformed_coordinates(Matrix transformation_matrix, double[] input_vector) {
        double alpha = input_vector[0];
        double delta = input_vector[1];
        double t = input_vector[2];
        double k = 1.002737908;

        double[] equatorial_dir_cosine_array = {cos(delta) * cos(alpha - k * t), cos(delta) * sin(alpha - k * t), sin(delta)};
        Matrix equatorial_dir_cosine = new Matrix(equatorial_dir_cosine_array, 1);

        Matrix telescope_dir_cosine = transformation_matrix.times(equatorial_dir_cosine.transpose());
        double ra_transformed = -Math.atan2(telescope_dir_cosine.get(1, 0), telescope_dir_cosine.get(0, 0)) * 180 / PI;
        if (ra_transformed < 0) {
            ra_transformed = 360 + ra_transformed;
        }

        double dec_tranformed = Math.asin(telescope_dir_cosine.get(2, 0)) * 180 / PI;


        double[] transformed_coordinates = {ra_transformed, dec_tranformed};
        //Matrix transformed_coords = new Matrix(transformed_coordinates, 1);
        //Log.e(TAG, "transformed_coords are :\n " + strung(transformed_coords));
        return transformed_coordinates;
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
