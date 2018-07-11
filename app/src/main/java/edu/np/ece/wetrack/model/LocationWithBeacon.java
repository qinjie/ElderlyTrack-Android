package edu.np.ece.wetrack.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationWithBeacon extends Location {

    @SerializedName("beacon")
    @Expose
    private BeaconProfile beacon;

    private final static long serialVersionUID = 5987127122057379314L;

    public BeaconProfile getBeacon() {
        return beacon;
    }

    public void setBeacon(BeaconProfile beacon) {
        this.beacon = beacon;
    }

    public String getAddressOrGps() {
        if (this.getAddress() == null || this.getAddress().isEmpty()) {
            if (this.getLatitude() != null && this.getLongitude() != null) {
                return String.valueOf(this.getLatitude() + ", " + String.valueOf(this.getLongitude()));
            } else {
                return null;
            }
        } else {
            return this.getAddress();
        }
    }


}
