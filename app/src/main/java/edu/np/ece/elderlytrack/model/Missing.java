package edu.np.ece.elderlytrack.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import edu.np.ece.elderlytrack.utils.Utils;

public class Missing implements Serializable {

    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("closed_by")
    @Expose
    private String closedBy;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("closed_at")
    @Expose
    private String closedAt;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("resident_id")
    @Expose
    private Integer residentId;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("reported_at")
    @Expose
    private String reportedAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("remark")
    @Expose
    private String remark;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("reported_by")
    @Expose
    private Integer reportedBy;
    @SerializedName("closure")
    @Expose
    private String closure;
    private final static long serialVersionUID = -2514903758435027557L;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(String reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Integer reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getClosure() {
        return closure;
    }

    public void setClosure(String closure) {
        this.closure = closure;
    }

    public String getAddressOrGps() {
        if (address == null || address.isEmpty()) {
            if (latitude != null && longitude != null) {
                return "(" + String.valueOf(latitude) + ", " + String.valueOf(longitude) + ")";
            } else {
                return null;
            }
        } else {
            return address;
        }
    }

    public String getAddressHtml() {
        if (getAddressOrGps() != null) {
            String s = "<a href=\"http://maps.google.com/maps?q=%s,%s\">%s</a>";
            return String.format(s, latitude, longitude, getAddressOrGps());
        }
        return null;
    }

    public String getRemarkOrComment() {
        if (this.remark == null || this.remark.isEmpty())
            return "No remark for this missing case.";
        else
            return remark;
    }

    public String getReportedAtLocal(String newDateFormat) {
        if (newDateFormat == null) newDateFormat = "dd MMM, h:mm a";
        return Utils.getLocalDateFromUtc(this.getCreatedAt(), null, newDateFormat);
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