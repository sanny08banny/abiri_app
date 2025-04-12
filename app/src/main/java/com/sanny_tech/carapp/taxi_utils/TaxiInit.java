package com.sanny_tech.carapp.taxi_utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class TaxiInit implements Parcelable {
    private String id;
    private String driver_id;
    private String model,color;
    private String manufacturer,plate_number;
    private String category,taxi_id;
    private List<String> taxi_images;

    public TaxiInit() {
    }

    public TaxiInit(String driver_id, String model, String color, String manufacturer,
                    String plate_number, String category) {
        this.driver_id = driver_id;
        this.model = model;
        this.color = color;
        this.manufacturer = manufacturer;
        this.plate_number = plate_number;
        this.category = category;
    }

    public TaxiInit(String category, String driver_id,
                    String manufacturer, String model, String color,
                    String plate_number, String taxi_id) {
        this.category = category;
        this.driver_id = driver_id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.color = color;
        this.plate_number = plate_number;
        this.taxi_id = taxi_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
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

    public String getPlate_number() {
        return plate_number;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public String getTaxi_id() {
        return taxi_id;
    }

    public void setTaxi_id(String taxi_id) {
        this.taxi_id = taxi_id;
    }

    public List<String> getTaxi_images() {
        return taxi_images;
    }

    public void setTaxi_images(List<String> taxi_images) {
        this.taxi_images = taxi_images;
    }

    protected TaxiInit(Parcel in) {
        id = in.readString();
        category = in.readString();
        driver_id = in.readString();
        manufacturer = in.readString();
        model = in.readString();
        color = in.readString();
        plate_number = in.readString();
        taxi_id = in.readString();
        taxi_images = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(category);
        dest.writeString(driver_id);
        dest.writeString(manufacturer);
        dest.writeString(model);
        dest.writeString(color);
        dest.writeString(plate_number);
        dest.writeString(taxi_id);
        dest.writeStringList(taxi_images);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TaxiInit> CREATOR = new Creator<TaxiInit>() {
        @Override
        public TaxiInit createFromParcel(Parcel in) {
            return new TaxiInit(in);
        }

        @Override
        public TaxiInit[] newArray(int size) {
            return new TaxiInit[size];
        }
    };

}
