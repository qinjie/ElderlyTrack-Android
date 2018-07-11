package edu.np.ece.wetrack.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import edu.np.ece.wetrack.utils.Utils;

public class Location implements Serializable {

    @SerializedName("locator_id")
    @Expose
    private Integer locatorId;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("beacon_id")
    @Expose
    private Integer beaconId;
    @SerializedName("resident_id")
    @Expose
    private Integer residentId;
    @SerializedName("missing_id")
    @Expose
    private Integer missingId;
    private final static long serialVersionUID = 8498488868182708098L;

    public Integer getLocatorId() {
        return locatorId;
    }

    public void setLocatorId(Integer locatorId) {
        this.locatorId = locatorId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(Integer beaconId) {
        this.beaconId = beaconId;
    }

    public Integer getResidentId() {
        return residentId;
    }

    public void setResidentId(Integer residentId) {
        this.residentId = residentId;
    }

    public Integer getMissingId() {
        return missingId;
    }

    public void setMissingId(Integer missingId) {
        this.missingId = missingId;
    }


    public String getCreatedAtLocal(String newDateFormat) {
        if (newDateFormat == null) newDateFormat = "dd MMM, hh:mm a";
        return Utils.getLocalDateFromUtc(this.getCreatedAt(), null, newDateFormat);
    }

}