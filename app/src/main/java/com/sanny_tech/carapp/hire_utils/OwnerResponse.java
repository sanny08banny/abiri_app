package com.sanny_tech.carapp.hire_utils;

public class OwnerResponse {
    private String description;
    private String user_id;
    private String car_id;

    public OwnerResponse(String description, String userid, String car_id) {
        this.description = description;
        this.user_id = userid;
        this.car_id = car_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCar_id() {
        return car_id;
    }

    public void setCar_id(String car_id) {
        this.car_id = car_id;
    }
}
