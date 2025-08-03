package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class NewBookingRequest implements Parcelable {
    private String user_id;
    private String car_id;
    private String owner_id;
    private String description;
    private String start_date;
    private String end_date;

    public NewBookingRequest(String description) {
        this.description = description;
    }

    public NewBookingRequest(String user_id, String car_id, String owner_id, String description, String start_date, String end_date) {
        this.user_id = user_id;
        this.car_id = car_id;
        this.owner_id = owner_id;
        this.description = description;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    protected NewBookingRequest(Parcel in) {
        user_id = in.readString();
        car_id = in.readString();
        owner_id = in.readString();
        description = in.readString();
        start_date = in.readString();
        end_date = in.readString();
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

    /**
     * @return 
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @param parcel 
     * @param i
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(user_id);
        parcel.writeString(car_id);
        parcel.writeString(owner_id);
        parcel.writeString(description);
        parcel.writeString(start_date);
        parcel.writeString(end_date);
    }
}
