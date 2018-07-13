package edu.np.ece.elderlytrack.api;

import android.util.Log;

import java.util.Map;

import edu.np.ece.elderlytrack.model.NearbyItem;

public class EventNearbyMissingBeaconsFound extends ApiEvent {
    private static String TAG = EventNearbyMissingBeaconsFound.class.getSimpleName();

    Map<String, NearbyItem> nearbyMissingBeacons = null;

    public EventNearbyMissingBeaconsFound(Map<String, NearbyItem> nearbyMissingBeacons) {
        Log.d(TAG, "EventNearbyMissingBeaconsFound(): " + nearbyMissingBeacons.size());
        this.nearbyMissingBeacons = nearbyMissingBeacons;
    }

    public Map<String, NearbyItem> getNearbyMissingBeacons() {
        return nearbyMissingBeacons;
    }

    public void setNearbyMissingBeacons(Map<String, NearbyItem> nearbyMissingBeacons) {
        this.nearbyMissingBeacons = nearbyMissingBeacons;
    }
}
