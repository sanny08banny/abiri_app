package com.sanny_tech.carapp.taxi_utils;

import android.os.Parcel;
import android.os.Parcelable;

public class ClientRequest implements Parcelable {
    private String sender_id;
    private double price;
    private String user_name;
    private double current_lat;
    private double current_lon;
    private double dest_lat;
    private double dest_lon;
    private String ride_id;
    private String user_phone;
    private String dest_name;
    private String status;

    public ClientRequest() {
    }

    public ClientRequest(String sender_id, double price, String user_name, double current_lat, double current_lon, double dest_lat,
                         double dest_lon, String ride_id, String user_phone, String dest_name) {
        this.sender_id = sender_id;
        this.price = price;
        this.user_name = user_name;
        this.current_lat = current_lat;
        this.current_lon = current_lon;
        this.dest_lat = dest_lat;
        this.dest_lon = dest_lon;
        this.ride_id = ride_id;
        this.user_phone = user_phone;
        this.dest_name = dest_name;
    }

    // Parcelable implementation
    protected ClientRequest(Parcel in) {
        sender_id = in.readString();
        price = in.readDouble();
        user_name = in.readString();
        current_lat = in.readDouble();
        current_lon = in.readDouble();
        dest_lat = in.readDouble();
        dest_lon = in.readDouble();
        ride_id = in.readString();
        user_phone = in.readString();
        dest_name = in.readString();
        status = in.readString();
    }

    public static final Creator<ClientRequest> CREATOR = new Creator<ClientRequest>() {
        @Override
        public ClientRequest createFromParcel(Parcel in) {
            return new ClientRequest(in);
        }

        @Override
        public ClientRequest[] newArray(int size) {
            return new ClientRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender_id);
        dest.writeDouble(price);
        dest.writeString(user_name);
        dest.writeDouble(current_lat);
        dest.writeDouble(current_lon);
        dest.writeDouble(dest_lat);
        dest.writeDouble(dest_lon);
        dest.writeString(ride_id);
        dest.writeString(user_phone);
        dest.writeString(dest_name);
        dest.writeString(status);
    }

    // Optional: Override toString() for easier logging
    @Override
    public String toString() {
        return "ClientRequest{" +
                "senderId='" + sender_id + '\'' +
                ", price=" + price +
                ", userName='" + user_name + '\'' +
                ", currentLat=" + current_lat +
                ", currentLon=" + current_lon +
                ", destLat=" + dest_lat +
                ", destLon=" + dest_lon +
                ", rideId='" + ride_id + '\'' +
                ", userPhone='" + user_phone + '\'' +
                ", destName='" + dest_name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public double getCurrent_lat() {
        return current_lat;
    }

    public void setCurrent_lat(double current_lat) {
        this.current_lat = current_lat;
    }

    public double getCurrent_lon() {
        return current_lon;
    }

    public void setCurrent_lon(double current_lon) {
        this.current_lon = current_lon;
    }

    public double getDest_lat() {
        return dest_lat;
    }

    public void setDest_lat(double dest_lat) {
        this.dest_lat = dest_lat;
    }

    public double getDest_lon() {
        return dest_lon;
    }

    public void setDest_lon(double dest_lon) {
        this.dest_lon = dest_lon;
    }

    public String getRide_id() {
        return ride_id;
    }

    public void setRide_id(String ride_id) {
        this.ride_id = ride_id;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getDest_name() {
        return dest_name;
    }

    public void setDest_name(String dest_name) {
        this.dest_name = dest_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
