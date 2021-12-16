package com.my_project.telescopecontrol;

import com.google.gson.annotations.SerializedName;

public class constellationObject {
    @SerializedName("name")
    private String name;
    @SerializedName("genitive")
    private String genitive;

    @SerializedName("en")
    private String en;


    public constellationObject(String name, String genitive, String en) {

        this.name = name;

        this.genitive = genitive;

        this.en = en;


    }


    public String getname() {
        return name;
    }

    public void setname(String con) {
        this.name = name;
    }


    public String getgenitive() {
        return genitive;
    }

    public void setgenitive(String genitive) {
        this.genitive = genitive;
    }

    public String geten() {
        return en;
    }

    public void seten(String en) {
        this.en = en;
    }


}
