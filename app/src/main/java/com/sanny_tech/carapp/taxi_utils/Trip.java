package com.sanny_tech.carapp.taxi_utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Trip implements Parcelable {
    private String id;
    private String driver_id;
    private String user_id;
    private String start_time,end_time;
    private String charges;
    private String driverNumber,clientNumber;
    private String pick_up, destination;
    public Trip() {
    }

    public Trip(String id, String driver_id, String user_id, String start_time, String endTime,
                String charges, String driverNumber, String clientNumber, String pick_up, String dest) {
        this.id = id;
        this.driver_id = driver_id;
        this.user_id = user_id;
        this.start_time = start_time;
        this.end_time = endTime;
        this.charges = charges;
        this.driverNumber = driverNumber;
        this.clientNumber = clientNumber;
        this.pick_up = pick_up;
        this.destination = dest;
    }

    protected Trip(Parcel in) {
        id = in.readString();
        driver_id = in.readString();
        user_id = in.readString();
        start_time = in.readString();
        end_time = in.readString();
        charges = in.readString();
        driverNumber = in.readString();
        clientNumber = in.readString();
        pick_up = in.readString();
        destination = in.readString();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCharges() {
        return charges;
    }

    public void setCharges(String charges) {
        this.charges = charges;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
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
        dest.writeString(id);
        dest.writeString(driver_id);
        dest.writeString(user_id);
        dest.writeString(start_time);
        dest.writeString(end_time);
        dest.writeString(charges);
        dest.writeString(driverNumber);
        dest.writeString(clientNumber);
        dest.writeString(pick_up);
        dest.writeString(destination);
    }
    public String generateReceipt() {
        return "Receipt for Trip ID: " + id + "\n" +
                "Driver ID: " + driver_id + "\n" +
                "User ID: " + user_id + "\n" +
                "Start Time: " + start_time + "\n" +
                "End Time: " + end_time + "\n" +
                "Charges: " + charges + "\n" +
                "Pick-up Location: " + pick_up + "\n" +
                "Destination: " + destination + "\n" +
                "Driver Number: " + driverNumber + "\n" +
                "Client Number: " + clientNumber;
    }
}
