package com.my_project.telescopecontrol;

public class BacklashFixesPoint {

    private double ha;
    private double dec;
    private int ra_backlash_fix;
    private int dec_backlash_fix;
    private String side_of_meridian;


    public BacklashFixesPoint(double ha, double dec, int ra_backlash_fix, int dec_backlash_fix,String side_of_meridian) {
        this.ha = ha;
        this.dec = dec;
        this.ra_backlash_fix = ra_backlash_fix;
        this.dec_backlash_fix = dec_backlash_fix;
        this.side_of_meridian = side_of_meridian;
    }

    public double get_ha() {
        return this.ha;
    }
    public double get_dec() {
        return this.dec;
    }
    public int get_ra_backlash_fix() {
        return this.ra_backlash_fix;
    }
    public int get_dec_backlash_fix() {
        return this.dec_backlash_fix;
    }
    public String get_side_of_meridian() {
        return this.side_of_meridian;
    }






}
