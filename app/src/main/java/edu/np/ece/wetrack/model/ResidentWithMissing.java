package edu.np.ece.wetrack.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResidentWithMissing extends Resident {

    @SerializedName("missing_active")
    @Expose
    private Missing missingActive;
    private final static long serialVersionUID = 5987127122057379314L;

    public Missing getActiveMissing() {
        return missingActive;
    }

    public void setMissingActive(Missing missingActive) {
        this.missingActive = missingActive;
    }
}