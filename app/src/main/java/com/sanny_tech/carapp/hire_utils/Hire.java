package com.sanny_tech.carapp.hire_utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.sanny_tech.carapp.entities.Car;

public class Hire implements Parcelable {
    private String id;
    private String owner_id;
    private String client_id;
    private float charges;
    private String carId;
    private String start_date;
    private String end_date;
    private String status;
    private String owner_contact, client_contact;
    private String owner,client;
    private Car car;

    public Hire() {
    }

    public Hire(String id, String owner_id, String client_id, float charges,
                String carId, String status,
                String owner_contact,
                String client_contact, String owner, String client) {
        this.id = id;
        this.owner_id = owner_id;
        this.client_id = client_id;
        this.charges = charges;
        this.carId = carId;
        this.status = status;
        this.owner_contact = owner_contact;
        this.client_contact = client_contact;
        this.owner = owner;
        this.client = client;
    }

    protected Hire(Parcel in) {
        id = in.readString();
        owner_id = in.readString();
        client_id = in.readString();
        charges = in.readFloat();
        carId = in.readString();
        start_date = in.readString();
        end_date = in.readString();
        status = in.readString();
        owner_contact = in.readString();
        client_contact = in.readString();
        owner = in.readString();
        client = in.readString();
        car = in.readParcelable(Car.class.getClassLoader());
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwner_contact() {
        return owner_contact;
    }

    public void setOwner_contact(String owner_contact) {
        this.owner_contact = owner_contact;
    }

    public String getClient_contact() {
        return client_contact;
    }

    public void setClient_contact(String client_contact) {
        this.client_contact = client_contact;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
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
        dest.writeString(owner_id);
        dest.writeString(client_id);
        dest.writeFloat(charges);
        dest.writeString(carId);
        dest.writeString(start_date);
        dest.writeString(end_date);
        dest.writeString(status);
        dest.writeString(owner_contact);
        dest.writeString(client_contact);
        dest.writeString(owner);
        dest.writeString(client);
        dest.writeParcelable(car, flags);
    }
    public String generateReceipt() {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; }" +
                "h2 { color: #333; }" +
                "p { font-size: 14px; line-height: 1.5; }" +
                ".receipt { border: 1px solid #ccc; padding: 20px; border-radius: 10px; max-width: 600px; margin: auto; }" +
                ".receipt p { margin: 5px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='receipt'>" +
                "<h2>Hire Receipt</h2>" +
                "<p><strong>Hire ID:</strong> " + id + "</p>" +
                "<p><strong>Owner:</strong> " + owner + " (" + owner_contact + ")</p>" +
                "<p><strong>Client:</strong> " + client + " (" + client_contact + ")</p>" +
                "<p><strong>Car ID:</strong> " + carId + "</p>" +
                "<p><strong>Start Date:</strong> " + start_date + "</p>" +
                "<p><strong>End Date:</strong> " + end_date + "</p>" +
                "<p><strong>Status:</strong> " + status + "</p>" +
                "<p><strong>Charges:</strong> $" + charges + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
