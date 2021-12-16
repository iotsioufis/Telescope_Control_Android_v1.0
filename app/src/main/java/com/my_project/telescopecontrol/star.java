package com.my_project.telescopecontrol;

import com.google.gson.annotations.SerializedName;

public class star {
    @SerializedName("Con")
    private String con;
    @SerializedName("Date")
    private transient String date;
    @SerializedName("Dec(J2000)")
    private double decj2000;
    @SerializedName("Designation")
    private transient String designation;
    private double distance_from_nearest_selected;
    @SerializedName("#")
    private transient String hashtag;
    @SerializedName("HD")
    private transient int hd;
    @SerializedName("HIP")
    private String hip;
    @SerializedName("ID")
    private transient String id;
    @SerializedName("ID/Diacritics")
    private transient String id_diacritics;
    @SerializedName("bnd")
    private transient String mbnd;
    @SerializedName("mag")
    private double mmag;
    @SerializedName("notes")
    private transient String mnotes;
    @SerializedName("Name/ASCII")
    private String name_ascii;
    @SerializedName("Name/Diacritics")
    private transient String name_diacritics;
    @SerializedName("RA(J2000)")
    private double raj2000;
    @SerializedName("WDS_J")
    private transient int wds_j;

    public star(String name_ascii, String name_diacritics, String designation, String id, String id_diacritics, String con, String hashtag, int wds_j, double mmag, String mbnd, String hip, int hd, double raj2000, double decj2000, String date, String mnotes) {
        this.name_ascii = name_ascii;
        this.name_diacritics = name_diacritics;
        this.designation = designation;
        this.id = id;
        this.id_diacritics = id_diacritics;
        this.con = con;
        this.hashtag = hashtag;
        this.wds_j = wds_j;
        this.mmag = mmag;
        this.mbnd = mbnd;
        this.hip = hip;
        this.hd = hd;
        this.raj2000 = raj2000;
        this.decj2000 = decj2000;
        this.date = date;
        this.mnotes = mnotes;
    }

    public String getName_ascii() {
        return this.name_ascii;
    }

    public void setName_ascii(String name_ascii2) {
        this.name_ascii = name_ascii2;
    }

    public String getName_diacritics() {
        return this.name_diacritics;
    }

    public void setName_diacritics(String name_diacritics2) {
        this.name_diacritics = name_diacritics2;
    }

    public String getDesignation() {
        return this.designation;
    }

    public void setDesignation(String designation2) {
        this.designation = designation2;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id2) {
        this.id = id2;
    }

    public String getId_diacritics() {
        return this.id_diacritics;
    }

    public void setId_diacritics(String id_diacritics2) {
        this.id_diacritics = id_diacritics2;
    }

    public String getCon() {
        return this.con;
    }

    public void setCon(String con2) {
        this.con = con2;
    }

    public String getHashtag() {
        return this.hashtag;
    }

    public void setHashtag(String hashtag2) {
        this.hashtag = hashtag2;
    }

    public int getWds_j() {
        return this.wds_j;
    }

    public void setWds_j(int wds_j2) {
        this.wds_j = wds_j2;
    }

    public double getMmag() {
        return this.mmag;
    }

    public void setMmag(double mmag2) {
        this.mmag = mmag2;
    }

    public String getMbnd() {
        return this.mbnd;
    }

    public void setMbnd(String mbnd2) {
        this.mbnd = mbnd2;
    }

    public String getHip() {
        return this.hip;
    }

    public void setHip(String hip2) {
        this.hip = hip2;
    }

    public int getHd() {
        return this.hd;
    }

    public void setHd(int hd2) {
        this.hd = hd2;
    }

    public double getRaj2000() {
        return this.raj2000 / 15.0d;
    }

    public void setRaj2000(double raj20002) {
        this.raj2000 = raj20002;
    }

    public double getDecj2000() {
        return this.decj2000;
    }

    public void setDecj2000(double decj20002) {
        this.decj2000 = decj20002;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date2) {
        this.date = date2;
    }

    public String getMnotes() {
        return this.mnotes;
    }

    public void setMnotes(String mnotes2) {
        this.mnotes = mnotes2;
    }

    public void setDistance_from_nearest_selected(Double distance) {
        this.distance_from_nearest_selected = distance.doubleValue();
    }

    public Double getDistance_from_nearest_Selected() {
        return Double.valueOf(this.distance_from_nearest_selected);
    }

    public String toString() {
        return this.name_ascii.toString();
    }
}
