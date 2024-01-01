package com.sanny_tech.carapp.taxi_utils;

public class Trip {
    private String id;
    private String driver_id;
    private String user_id;
    private String start_time;
    private String charges;
    private String driverNumber,clientNumber;
    private String pick_up, dest;
    public Trip() {
    }

    public Trip(String id, String driver_id, String user_id, String start_time,
                String charges, String driverNumber, String clientNumber, String pick_up, String dest) {
        this.id = id;
        this.driver_id = driver_id;
        this.user_id = user_id;
        this.start_time = start_time;
        this.charges = charges;
        this.driverNumber = driverNumber;
        this.clientNumber = clientNumber;
        this.pick_up = pick_up;
        this.dest = dest;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getDriverNumber() {
        return driverNumber;
    }

    public void setDriverNumber(String driverNumber) {
        this.driverNumber = driverNumber;
    }

    public String getClientNumber() {
        return clientNumber;
    }

    public void setClientNumber(String clientNumber) {
        this.clientNumber = clientNumber;
    }

    public String getPick_up() {
        return pick_up;
    }

    public void setPick_up(String pick_up) {
        this.pick_up = pick_up;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getCharges() {
        return charges;
    }

    public void setCharges(String charges) {
        this.charges = charges;
    }
}
