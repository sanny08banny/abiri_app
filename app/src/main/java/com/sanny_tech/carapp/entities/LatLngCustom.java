package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class LatLngCustom implements Parcelable {
    private double latitude;
    private double longitude;
    private String name;

    public LatLngCustom() {
    }

    public LatLngCustom(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected LatLngCustom(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        name = in.readString();
    }

    public static final Creator<LatLngCustom> CREATOR = new Creator<LatLngCustom>() {
        @Override
        public LatLngCustom createFromParcel(Parcel in) {
            return new LatLngCustom(in);
        }

        @Override
        public LatLngCustom[] newArray(int size) {
            return new LatLngCustom[size];
        }
    };

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

    public LatLng toLatLng() {

        return new LatLng(latitude,longitude);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(name);
    }
}
