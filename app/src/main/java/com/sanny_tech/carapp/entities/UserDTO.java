package com.sanny_tech.carapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class UserDTO implements Parcelable {
    private String email;
    private String password,notification_id;

    public UserDTO(String email, String password, String notification_id) {
        this.email = email;
        this.password = password;
        this.notification_id = notification_id;
    }

    public UserDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    protected UserDTO(Parcel in) {
        email = in.readString();
        password = in.readString();
        notification_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(notification_id);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }
}
