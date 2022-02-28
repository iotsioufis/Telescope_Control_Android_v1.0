package com.my_project.telescopecontrol;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import Jama.Matrix;

public class AlignmentPoint {
    public Matrix associated_transformation_matrix;
    private double corrected_dec;
    private double corrected_ha;
    public boolean is_initial_alignment_point = false;
    private String name;
    private double non_transformed_dec;
    private double non_transformed_ra;
    private String side_of_meridian;
    private double time_difference;


    //private double transformed_ra;
    //private double transformed_dec;

    public AlignmentPoint(String name, String side_of_meridian, double non_transformed_ra, double corrected_ha, double non_transformed_dec, double corrected_dec, double time_difference) {
        this.name = name;
        this.non_transformed_ra = non_transformed_ra;
        this.corrected_ha = corrected_ha;
        this.non_transformed_dec = non_transformed_dec;
        this.corrected_dec = corrected_dec;
        this.time_difference = time_difference;
        this.side_of_meridian = side_of_meridian;


        // this.transformed_ra= transformed_ra;
        //this.transformed_dec = transformed_dec;


    }


    public String getName() {
        return name.toString();
    }


    public double get_non_transformed_ra() {
        return non_transformed_ra;
    }

    public double get_corrected_ha() {
        return corrected_ha;
    }

    public double get_non_transformed_dec() {
        return non_transformed_dec;
    }

    public double get_corrected_dec() {
        return corrected_dec;
    }

    public double get_time_difference() {
        return time_difference;
    }

    public String get_side_of_meridian() {
        return this.side_of_meridian;
    }

    public void associate_matrix(Matrix associated_transformation_matrix) {
        this.associated_transformation_matrix = associated_transformation_matrix;
    }





    @NonNull
    @Override
    public String toString() {
        return name.toString();
    }


}




