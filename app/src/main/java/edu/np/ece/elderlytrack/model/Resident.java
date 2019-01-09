package edu.np.ece.elderlytrack.model;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.np.ece.elderlytrack.utils.Utils;

public class Resident implements Serializable {
    private static String TAG = Resident.class.getSimpleName();
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 0;
    public static final int STATUS_MISSING = 1;
    public static final int STATUS_PRESENT = 0;

    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("thumbnail_path")
    @Expose
    private String thumbnailPath;
    @SerializedName("dob")
    @Expose
    private String dob;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("image_path")
    @Expose
    private String imagePath;
    @SerializedName("remark")
    @Expose
    private String remark;
    @SerializedName("nric")
    @Expose
    private String nric;
    @SerializedName("gender")
    @Expose
    private Integer gender;
    @SerializedName("hide_photo")
    @Expose
    private Integer hidePhoto;
    @SerializedName("fullname")
    @Expose
    private String fullname;

    private final static long serialVersionUID = -6054967114345030091L;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getNric() {
        return nric;
    }

    public void setNric(String nric) {
        this.nric = nric;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getHidePhoto() {
        return hidePhoto;
    }

    public void setHidePhoto(Integer hidePhoto) {
        this.hidePhoto = hidePhoto;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public int getAge() {
//        LocalDate birthdate = new LocalDate(dob);
//        LocalDate now = new LocalDate();
//        Years age = Years.yearsBetween(birthdate, now);
//        return age.getYears();
        Log.d(TAG, "getAge() from " + dob);
        try {
            Date birthday = new SimpleDateFormat("yyyy-MM-dd").parse(dob);
            return Utils.getAge(birthday);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getGenderString() {
        return (gender == Resident.GENDER_MALE) ? "Male" : "Female";
    }

    public String getGenderAge() {
        return getGenderString() + " (" + getAge() + ")";
    }

    public String getNameAge() {
        return getFullname() + " (" + getAge() + ")";
    }

    public String getStatusString() {
        return (status == Resident.STATUS_MISSING) ? "Missing" : "Present";
    }

    @Override
    public String toString() {
        return "Resident{" +
                "status=" + status +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", dob='" + dob + '\'' +
                ", id=" + id +
                ", updatedAt='" + updatedAt + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", remark='" + remark + '\'' +
                ", nric='" + nric + '\'' +
                ", gender=" + gender +
                ", hidePhoto=" + hidePhoto +
                ", fullname='" + fullname + '\'' +
                '}';
    }

    public String getRemarkOrComment() {
        if (remark == null || remark.isEmpty()) {
            return "";
//            return "No description available.";
        } else {
            return remark;
        }
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