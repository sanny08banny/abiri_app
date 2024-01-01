package com.sanny_tech.carapp.entities;

public class Quickstep {
    private String image;
    private String desc;

    public Quickstep(String image, String desc) {
        this.image = image;
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
