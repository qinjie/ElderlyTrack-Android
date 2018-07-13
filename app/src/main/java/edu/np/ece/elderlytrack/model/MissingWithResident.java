package edu.np.ece.elderlytrack.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MissingWithResident extends Missing {

    @SerializedName("resident")
    @Expose
    private Resident resident;

    private final static long serialVersionUID = 5987127122057379314L;

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }
}