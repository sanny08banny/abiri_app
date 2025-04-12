package com.sanny_tech.carapp.entities;

public class Quickstep {
    private String id;
    private String image;
    private String title,title2,title3;
    private String subtext;

    public Quickstep() {
    }

    public Quickstep(String id) {
        this.id = id;
    }

    public Quickstep(String id, String image, String title, String title2, String title3, String subtext) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.title2 = title2;
        this.title3 = title3;
        this.subtext = subtext;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }

    public String getTitle3() {
        return title3;
    }

    public void setTitle3(String title3) {
        this.title3 = title3;
    }

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
    }
}
