package com.sanny_tech.carapp.fun_utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.sanny_tech.carapp.entities.LatLngCustom;

import java.util.List;

public class SpaceDest implements Parcelable {
    private String id;
    private String owner_id;
    private List<String> images_urls;
    private String name,owner_name,phone_number,alternative_number;
    private String category;
    private LatLngCustom location;
    private List<String> selectedServices;
    private List<String> selectedActivities;
    private OperatingHours operatingHours;

    public SpaceDest() {
    }

    public SpaceDest(String id, String owner_id, List<String> images_urls, String name,
                     String owner_name, String phone_number, String alternative_number) {
        this.id = id;
        this.owner_id = owner_id;
        this.images_urls = images_urls;
        this.name = name;
        this.owner_name = owner_name;
        this.phone_number = phone_number;
        this.alternative_number = alternative_number;
    }


    protected SpaceDest(Parcel in) {
        id = in.readString();
        owner_id = in.readString();
        images_urls = in.createStringArrayList();
        name = in.readString();
        owner_name = in.readString();
        phone_number = in.readString();
        alternative_number = in.readString();
        category = in.readString();
        location = in.readParcelable(LatLngCustom.class.getClassLoader());
        selectedServices = in.createStringArrayList();
        selectedActivities = in.createStringArrayList();
        operatingHours = in.readParcelable(OperatingHours.class.getClassLoader());
    }

    public static final Creator<SpaceDest> CREATOR = new Creator<SpaceDest>() {
        @Override
        public SpaceDest createFromParcel(Parcel in) {
            return new SpaceDest(in);
        }

        @Override
        public SpaceDest[] newArray(int size) {
            return new SpaceDest[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getAlternative_number() {
        return alternative_number;
    }

    public void setAlternative_number(String alternative_number) {
        this.alternative_number = alternative_number;
    }

    public List<String> getSelectedServices() {
        return selectedServices;
    }

    public void setSelectedServices(List<String> selectedServices) {
        this.selectedServices = selectedServices;
    }

    public List<String> getSelectedActivities() {
        return selectedActivities;
    }

    public void setSelectedActivities(List<String> selectedActivities) {
        this.selectedActivities = selectedActivities;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LatLngCustom getLocation() {
        return location;
    }

    public void setLocation(LatLngCustom location) {
        this.location = location;
    }

    public OperatingHours getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(OperatingHours operatingHours) {
        this.operatingHours = operatingHours;
    }

    public List<String> getImages_urls() {
        return images_urls;
    }

    public void setImages_urls(List<String> images_urls) {
        this.images_urls = images_urls;
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
        dest.writeStringList(images_urls);
        dest.writeString(name);
        dest.writeString(owner_name);
        dest.writeString(phone_number);
        dest.writeString(alternative_number);
        dest.writeString(category);
        dest.writeParcelable(location, flags);
        dest.writeStringList(selectedServices);
        dest.writeStringList(selectedActivities);
        dest.writeParcelable(operatingHours, flags);
    }
}
