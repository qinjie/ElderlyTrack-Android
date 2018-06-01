package edu.np.ece.wetrack.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Relative implements Parcelable {
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;
//
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("email_confirm_token")
    private String emailConfirmToken;

    @SerializedName("role")
    private int role;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("status")
    private int status;

    @SerializedName("allowance")
    private String allowance;

    @SerializedName("timestamp")
    private String timestamp;

    public final static Parcelable.Creator<Relative> CREATOR = new Creator<Relative>() {

        @SuppressWarnings({
                "unchecked"
        })
        public Relative createFromParcel(Parcel in) {
            Relative instance = new Relative();
            instance.id = ((int) in.readValue((Integer.class.getClassLoader())));
            instance.username = ((String) in.readValue((String.class.getClassLoader())));
            instance.email = ((String) in.readValue((String.class.getClassLoader())));

            instance.accessToken = ((String) in.readValue((String.class.getClassLoader())));
            instance.emailConfirmToken = ((String) in.readValue((String.class.getClassLoader())));
            instance.role = ((int) in.readValue((Integer.class.getClassLoader())));
            instance.phoneNumber = ((String) in.readValue((String.class.getClassLoader())));
            instance.status = ((int) in.readValue((Integer.class.getClassLoader())));
            instance.allowance = ((String) in.readValue((String.class.getClassLoader())));
            instance.timestamp = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public Relative[] newArray(int size) {
            return (new Relative[size]);
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

///////
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken= accessToken;
    }

    public String getEmailConfirmToken() {
        return emailConfirmToken;
    }

    public void setEmailConfirmToken(String emailConfirmToken) {
        this.emailConfirmToken = emailConfirmToken;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public String getAllowance() {
        return allowance;
    }

    public void setAllowance(String allowance) {
        this.allowance = allowance;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(username);


        dest.writeValue(accessToken);
        dest.writeValue(emailConfirmToken);
        dest.writeValue(role);
        dest.writeValue(phoneNumber);
        dest.writeValue(status);
        dest.writeValue(allowance);
        dest.writeValue(timestamp);





//        dest.writeValue(status);
    }


    public int describeContents() {
        return 0;
    }

}