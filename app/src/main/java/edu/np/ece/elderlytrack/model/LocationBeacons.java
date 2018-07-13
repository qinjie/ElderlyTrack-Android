package edu.np.ece.elderlytrack.model;

import java.util.ArrayList;

public class LocationBeacons {
    private Gps gps;
    private ArrayList<BeaconProfile> beaconProfiles;

    public Gps getGps() {
        return gps;
    }

    public void setGps(Gps gps) {
        this.gps = gps;
    }

    public ArrayList<BeaconProfile> getBeaconProfiles() {
        return beaconProfiles;
    }

    public void setBeaconProfiles(ArrayList<BeaconProfile> beaconProfiles) {
        this.beaconProfiles = beaconProfiles;
    }

}
