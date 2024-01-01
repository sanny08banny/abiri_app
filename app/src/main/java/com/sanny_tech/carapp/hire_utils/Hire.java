package com.sanny_tech.carapp.hire_utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Hire implements Parcelable {
    private String owner_id;
    private String client_id;
    private float charges;
    private String carId;
    private String start_date;
    private String end_date;

    public Hire() {
    }

    public Hire(String owner_id, String client_id, float charges, String carId) {
        this.owner_id = owner_id;
        this.client_id = client_id;
        this.charges = charges;
        this.carId = carId;
    }

    protected Hire(Parcel in) {
        owner_id = in.readString();
        client_id = in.readString();
        charges = in.readFloat();
        carId = in.readString();
        start_date = in.readString();
        end_date = in.readString();
    }

    public static final Creator<Hire> CREATOR = new Creator<Hire>() {
        @Override
        public Hire createFromParcel(Parcel in) {
            return new Hire(in);
        }

        @Override
        public Hire[] newArray(int size) {
            return new Hire[size];
        }
    };

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public float getCharges() {
        return charges;
    }

    public void setCharges(float charges) {
        this.charges = charges;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(owner_id);
        dest.writeString(client_id);
        dest.writeFloat(charges);
        dest.writeString(carId);
        dest.writeString(start_date);
        dest.writeString(end_date);
    }
}
