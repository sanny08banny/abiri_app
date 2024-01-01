package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class NewBookingRequest implements Parcelable {
    private String client_id;
    private String recepient_id;
    private String car_id;

    public NewBookingRequest() {
    }

    public NewBookingRequest(String client_id, String recepient_id, String carId) {
        this.client_id = client_id;
        this.recepient_id = recepient_id;
        car_id = carId;
    }

    protected NewBookingRequest(Parcel in) {
        client_id = in.readString();
        recepient_id = in.readString();
        car_id = in.readString();
    }

    public static final Creator<NewBookingRequest> CREATOR = new Creator<NewBookingRequest>() {
        @Override
        public NewBookingRequest createFromParcel(Parcel in) {
            return new NewBookingRequest(in);
        }

        @Override
        public NewBookingRequest[] newArray(int size) {
            return new NewBookingRequest[size];
        }
    };

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getRecepient_id() {
        return recepient_id;
    }

    public void setRecepient_id(String recepient_id) {
        this.recepient_id = recepient_id;
    }

    public String getCar_id() {
        return car_id;
    }

    public void setCar_id(String car_id) {
        this.car_id = car_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(client_id);
        dest.writeString(recepient_id);
        dest.writeString(car_id);
    }
}
