package com.sanny_tech.carapp.taxi_utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TaxiInitRequest implements Parcelable {
    private String driver_id;
    private String model,color;
    private String manufacturer,plate_number;
    private String category;

    public TaxiInitRequest() {
    }
    public TaxiInitRequest(String driver_id, String model, String color, String manufacturer, String plate_number, String category) {
        this.driver_id = driver_id;
        this.model = model;
        this.color = color;
        this.manufacturer = manufacturer;
        this.plate_number = plate_number;
        this.category = category;
    }

    protected TaxiInitRequest(Parcel in) {
        driver_id = in.readString();
        model = in.readString();
        color = in.readString();
        manufacturer = in.readString();
        plate_number = in.readString();
        category = in.readString();
    }

    public static final Creator<TaxiInitRequest> CREATOR = new Creator<TaxiInitRequest>() {
        @Override
        public TaxiInitRequest createFromParcel(Parcel in) {
            return new TaxiInitRequest(in);
        }

        @Override
        public TaxiInitRequest[] newArray(int size) {
            return new TaxiInitRequest[size];
        }
    };

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
        dest.writeString(model);
        dest.writeString(color);
        dest.writeString(manufacturer);
        dest.writeString(plate_number);
        dest.writeString(category);
    }
}
