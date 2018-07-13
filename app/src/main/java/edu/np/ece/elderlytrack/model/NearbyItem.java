package edu.np.ece.elderlytrack.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NearbyItem extends BeaconProfile {

    @SerializedName("resident")
    @Expose
    private ResidentWithMissing resident;

    public ResidentWithMissing getResident() {
        return resident;
    }

    public void setResident(ResidentWithMissing resident) {
        this.resident = resident;
    }

    @Override
    public String toString() {
        return "NearbyItem{" +
                "resident=" + resident +
                "beaconProfile=" + super.toString() +
                '}';
    }

    public String getLabelWithMinor() {
        return getLabel() + " (" + getMinor() + ")";
    }

}

