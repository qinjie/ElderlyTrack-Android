package edu.np.ece.elderlytrack.api;

import android.util.Log;

public class EventHasGpsLocation {
    private static String TAG = EventHasGpsLocation.class.getSimpleName();

    public double latitude;
    public double longitude;
    public String address;

    public EventHasGpsLocation(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;

        Log.i(TAG, "EventHasGpsLocation(): ");

    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "EventHasGpsLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                '}';
    }
}
