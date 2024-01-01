package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class CarBookRequest implements Parcelable {
    private String client_id;
    private String booking_id;
    private String car_id;
    private  String user_name;
    private String user_phone;

    public CarBookRequest() {
    }

    public CarBookRequest(String client_id, String booking_id, String car_id, String user_name, String user_phone) {
        this.client_id = client_id;
        this.booking_id = booking_id;
        this.car_id = car_id;
        this.user_name = user_name;
        this.user_phone = user_phone;
    }

    protected CarBookRequest(Parcel in) {
        client_id = in.readString();
        booking_id = in.readString();
        car_id = in.readString();
        user_name = in.readString();
        user_phone = in.readString();
    }

    public static final Creator<CarBookRequest> CREATOR = new Creator<CarBookRequest>() {
        @Override
        public CarBookRequest createFromParcel(Parcel in) {
            return new CarBookRequest(in);
        }

        @Override
        public CarBookRequest[] newArray(int size) {
            return new CarBookRequest[size];
        }
    };

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(String booking_id) {
        this.booking_id = booking_id;
    }

    public String getCar_id() {
        return car_id;
    }

    public void setCar_id(String car_id) {
        this.car_id = car_id;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(client_id);
        dest.writeString(booking_id);
        dest.writeString(car_id);
        dest.writeString(user_name);
        dest.writeString(user_phone);
    }
}