package edu.np.ece.elderlytrack.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.altbeacon.beacon.Beacon;

import java.io.Serializable;
import java.text.DecimalFormat;

import edu.np.ece.elderlytrack.utils.Utils;

public class BeaconProfile implements Serializable {

    @SerializedName("mac")
    @Expose
    private String mac;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("major")
    @Expose
    private String major;
    @SerializedName("minor")
    @Expose
    private String minor;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("distance")
    @Expose
    private Double distance;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("resident_id")
    @Expose
    private Integer residentId;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    private final static long serialVersionUID = 8600201361630404789L;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getResidentId() {
        return residentId;
    }

    public void setResidentId(Integer residentId) {
        this.residentId = residentId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getCreatedAtLocal(String newDateFormat) {
        if (newDateFormat == null) newDateFormat = "dd MMM, h:mm a";
        return Utils.getLocalDateFromUtc(this.getCreatedAt(), null, newDateFormat);
    }

    public String getUpdatedAtLocal(String newDateFormat) {
        if (newDateFormat == null) newDateFormat = "dd MMM, h:mm a";
        return Utils.getLocalDateFromUtc(this.getUpdatedAt(), null, newDateFormat);
    }

    public String getDistanceString() {
        if (this.distance != null) {
            DecimalFormat df = new DecimalFormat("0.00 m");
            return df.format(this.distance);
        } else {
            return "";
        }
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "BeaconProfile{" +
                "minor=" + minor +
                ", label=" + label +
                ", status=" + status +
                ", residentId=" + residentId +
                ", uuid='" + uuid + '\'' +
                ", id=" + id +
                ", major=" + major +
                '}';
    }

    public static int STATUS_ENABLED = 1;
    public static int STATUS_DISABLED = 0;

    public BeaconProfile() {
    }

    public BeaconProfile(Beacon beacon) {
        this.mac = beacon.getBluetoothAddress();
        this.label = beacon.getBluetoothName();
        this.uuid = String.valueOf(beacon.getId1());
        this.major = String.valueOf(beacon.getId2());
        this.minor = String.valueOf(beacon.getId3());
        this.distance = beacon.getDistance();
    }
}