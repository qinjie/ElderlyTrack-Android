package edu.np.ece.elderlytrack.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import edu.np.ece.elderlytrack.utils.Utils;

public class UserProfile implements Serializable {

    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("fullname")
    @Expose
    private String fullname;
    @SerializedName("nric")
    @Expose
    private String nric;
    private final static long serialVersionUID = 5602299162055883163L;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getNric() {
        return nric;
    }

    public void setNric(String nric) {
        this.nric = nric;
    }

    public String getCreatedAtLocal(String newDateFormat) {
        if (newDateFormat == null) newDateFormat = "dd MMM, h:mm a";
        return Utils.getLocalDateFromUtc(this.getCreatedAt(), null, newDateFormat);
    }

    public String getUpdatedAtLocal(String newDateFormat) {
        if (newDateFormat == null) newDateFormat = "dd MMM, h:mm a";
        return Utils.getLocalDateFromUtc(this.getUpdatedAt(), null, newDateFormat);
    }
}