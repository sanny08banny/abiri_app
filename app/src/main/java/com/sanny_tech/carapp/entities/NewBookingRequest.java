package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class NewBookingRequest implements Parcelable {
    private String user_id;
    private String car_id;
    private String owner_id;
    private String description;

    public NewBookingRequest(String description) {
        this.description = description;
    }

    public NewBookingRequest(String user_id, String car_id, String owner_id, String description) {
        this.user_id = user_id;
        this.car_id = car_id;
        this.owner_id = owner_id;
        this.description = description;
    }

    protected NewBookingRequest(Parcel in) {
        user_id = in.readString();
        owner_id = in.readString();
        car_id = in.readString();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(owner_id);
        dest.writeString(car_id);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCar_id() {
        return car_id;
    }

    public void setCar_id(String car_id) {
        this.car_id = car_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }
}
