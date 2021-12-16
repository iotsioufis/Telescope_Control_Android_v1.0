package com.my_project.telescopecontrol;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;


public class hipObject {


    @SerializedName("RArad")
    private double raj2000;
    @SerializedName("DErad")
    private double decj2000;
    @SerializedName("Hpmag")
    private double mmag;


    public hipObject(double mmag, double raj2000, double decj2000) {


        this.mmag = mmag;

        this.raj2000 = raj2000;
        this.decj2000 = decj2000;

    }


    public double getMmag() {
        return mmag;
    }

    public void setMmag(double mmag) {
        this.mmag = mmag;
    }


    public double getRaj2000() {
        return raj2000 / 15;
    }

    public void setRaj2000(double raj2000) {
        this.raj2000 = raj2000;
    }

    public double getDecj2000() {
        return decj2000;
    }

    public void setDecj2000(double decj2000) {
        this.decj2000 = decj2000;
    }


}
