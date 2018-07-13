package edu.np.ece.wetrack.api;

import java.util.List;

import edu.np.ece.wetrack.model.NearbyItem;

public class ApiEventListBeaconsOfMissing extends ApiEvent {
    List<NearbyItem> missingBeacons = null;

    public ApiEventListBeaconsOfMissing() {
    }

    public ApiEventListBeaconsOfMissing(List<NearbyItem> missingBeacons) {
        this.missingBeacons = missingBeacons;
    }

    public List<NearbyItem> getMissingBeacons() {
        return missingBeacons;
    }

    public void setMissingBeacons(List<NearbyItem> missingBeacons) {
        this.missingBeacons = missingBeacons;
    }
}
