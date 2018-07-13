package edu.np.ece.elderlytrack.api;

import java.util.List;

import edu.np.ece.elderlytrack.model.LocationWithBeacon;

public class ApiEventListLocationByMissingId extends ApiEvent {
    List<LocationWithBeacon> locationsWithBeacon = null;

    public ApiEventListLocationByMissingId() {
    }

    public ApiEventListLocationByMissingId(List<LocationWithBeacon> locationsWithBeacon) {
        this.locationsWithBeacon = locationsWithBeacon;
    }

    public List<LocationWithBeacon> getLocationsWithBeacon() {
        return locationsWithBeacon;
    }

    public void setLocationsWithBeacon(List<LocationWithBeacon> locationsWithBeacon) {
        this.locationsWithBeacon = locationsWithBeacon;
    }
}
