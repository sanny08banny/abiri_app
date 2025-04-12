package com.sanny_tech.carapp.entities;

public class UserLoginResponse {
    private Boolean is_driver;
    private String user_id;
    private String user_name;
    private String user_phone;

    public UserLoginResponse() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Boolean getIs_driver() {
        return is_driver;
    }

    public void setIs_driver(Boolean is_driver) {
        this.is_driver = is_driver;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }
}
