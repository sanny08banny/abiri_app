package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class AddressItem implements Parcelable {
    private String address;
    private double distance;
    private double latitude, longitude;

    public AddressItem(String address, double distance, double latitude, double longitude) {
        this.address = address;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected AddressItem(Parcel in) {
        address = in.readString();
        distance = in.readDouble();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<AddressItem> CREATOR = new Creator<AddressItem>() {
        @Override
        public AddressItem createFromParcel(Parcel in) {
            return new AddressItem(in);
        }

        @Override
        public AddressItem[] newArray(int size) {
            return new AddressItem[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public double getDistance() {
        return distance;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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
        dest.writeString(address);
        dest.writeDouble(distance);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}

