package com.sanny_tech.carapp.entities;
// MyItem.java
public class OptionItem {
    private String title;
    private String miniTitle;
    private String title3;

    public OptionItem(String title, String miniTitle) {
        this.title = title;
        this.miniTitle = miniTitle;
    }

    public OptionItem(String title, String miniTitle, String title3) {
        this.title = title;
        this.miniTitle = miniTitle;
        this.title3 = title3;
    }

    public String getTitle() {
        return title;
    }

    public String getMiniTitle() {
        return miniTitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMiniTitle(String miniTitle) {
        this.miniTitle = miniTitle;
    }

    public String getTitle3() {
        return title3;
    }

    public void setTitle3(String title3) {
        this.title3 = title3;
    }
}

