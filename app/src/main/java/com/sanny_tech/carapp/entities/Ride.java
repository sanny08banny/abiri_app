package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class Ride implements Parcelable {
    private String driver_id;
    private String user_id;
    private String start_time;
    private String driverNumber,clientNumber;
    private float driver_lat, driver_lon, client_lat, client_lon;
    private LatLngCustom destination;
    private String status;

    public Ride() {
    }

    public Ride(String driver_id, String user_id, String start_time,
                String driverNumber, String clientNumber, LatLngCustom destination) {
        this.driver_id = driver_id;
        this.user_id = user_id;
        this.start_time = start_time;
        this.driverNumber = driverNumber;
        this.clientNumber = clientNumber;
        this.destination = destination;
    }


    protected Ride(Parcel in) {
        driver_id = in.readString();
        user_id = in.readString();
        start_time = in.readString();
        driverNumber = in.readString();
        clientNumber = in.readString();
        driver_lat = in.readFloat();
        driver_lon = in.readFloat();
        client_lat = in.readFloat();
        client_lon = in.readFloat();
        status = in.readString();
    }

    public static final Creator<Ride> CREATOR = new Creator<Ride>() {
        @Override
        public Ride createFromParcel(Parcel in) {
            return new Ride(in);
        }

        @Override
        public Ride[] newArray(int size) {
            return new Ride[size];
        }
    };

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

    public float getDriver_lat() {
        return driver_lat;
    }

    public void setDriver_lat(float driver_lat) {
        this.driver_lat = driver_lat;
    }

    public float getDriver_lon() {
        return driver_lon;
    }

    public void setDriver_lon(float driver_lon) {
        this.driver_lon = driver_lon;
    }

    public float getClient_lat() {
        return client_lat;
    }

    public void setClient_lat(float client_lat) {
        this.client_lat = client_lat;
    }

    public float getClient_lon() {
        return client_lon;
    }

    public void setClient_lon(float client_lon) {
        this.client_lon = client_lon;
    }

    public LatLngCustom getDestination() {
        return destination;
    }

    public void setDestination(LatLngCustom destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(driver_id);
        dest.writeString(user_id);
        dest.writeString(start_time);
        dest.writeString(driverNumber);
        dest.writeString(clientNumber);
        dest.writeFloat(driver_lat);
        dest.writeFloat(driver_lon);
        dest.writeFloat(client_lat);
        dest.writeFloat(client_lon);
        dest.writeString(status);
    }
}
