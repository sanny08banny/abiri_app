package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.sanny_tech.carapp.fun_utils.SpaceDest;

import java.sql.Timestamp;
import java.util.List;

public class FunSpace implements Parcelable {
    private String id;
    private String owner_id;
    private List<String> images;
    private String desc;
    private SpaceDest destination;
    private String expiry_date;  //Timestamp
    private int likes,dislikes;

    public FunSpace() {
    }

    public FunSpace(String id, String owner_id, List<String> images, String desc, SpaceDest destination, int likes, int dislikes) {
        this.id = id;
        this.owner_id = owner_id;
        this.images = images;
        this.desc = desc;
        this.destination = destination;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    protected FunSpace(Parcel in) {
        id = in.readString();
        owner_id = in.readString();
        images = in.createStringArrayList();
        desc = in.readString();
        expiry_date = in.readString();
        likes = in.readInt();
        dislikes = in.readInt();
    }

    public static final Creator<FunSpace> CREATOR = new Creator<FunSpace>() {
        @Override
        public FunSpace createFromParcel(Parcel in) {
            return new FunSpace(in);
        }

        @Override
        public FunSpace[] newArray(int size) {
            return new FunSpace[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public SpaceDest getDestination() {
        return destination;
    }

    public void setDestination(SpaceDest destination) {
        this.destination = destination;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
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
        dest.writeStringList(images);
        dest.writeString(desc);
        dest.writeString(expiry_date);
        dest.writeInt(likes);
        dest.writeInt(dislikes);
    }
}
