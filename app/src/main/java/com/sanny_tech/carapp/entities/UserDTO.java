package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UserDTO implements Parcelable {
    private String email;
    private String password,name,tel,notification_id;

    public UserDTO(String email, String password, String name, String tel, String notification_id) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.tel = tel;
        this.notification_id = notification_id;
    }

    public UserDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    protected UserDTO(Parcel in) {
        email = in.readString();
        password = in.readString();
        name = in.readString();
        tel = in.readString();
        notification_id = in.readString();
    }

    public static final Creator<UserDTO> CREATOR = new Creator<UserDTO>() {
        @Override
        public UserDTO createFromParcel(Parcel in) {
            return new UserDTO(in);
        }

        @Override
        public UserDTO[] newArray(int size) {
            return new UserDTO[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

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

        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(name);
        dest.writeString(tel);
        dest.writeString(notification_id);
    }

}
