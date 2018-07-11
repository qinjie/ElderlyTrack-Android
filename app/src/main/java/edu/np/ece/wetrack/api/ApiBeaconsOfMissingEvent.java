package edu.np.ece.wetrack.api;

import java.util.List;

import edu.np.ece.wetrack.model.NearbyItem;

public class ApiBeaconsOfMissingEvent {
    boolean isResponded = false;
    boolean isSuccessful = false;
    int statusCode;
    List<NearbyItem> missingBeacons = null;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public List<NearbyItem> getMissingBeacons() {
        return missingBeacons;
    }

    public void setMissingBeacons(List<NearbyItem> missingBeacons) {
        this.missingBeacons = missingBeacons;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isResponded() {
        return isResponded;
    }

    public void setResponded(boolean responded) {
        this.isResponded = responded;
    }
}
