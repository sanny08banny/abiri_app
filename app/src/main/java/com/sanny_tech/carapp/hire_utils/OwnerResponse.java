package com.sanny_tech.carapp.hire_utils;

public class OwnerResponse {
    private String owner_id;
    private String user_id;
    private String car_id;
    private String description;

    public OwnerResponse(String ownerId, String user_id, String car_id, String description) {
        owner_id = ownerId;
        this.user_id = user_id;
        this.car_id = car_id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }
}