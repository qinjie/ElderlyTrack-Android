package edu.np.ece.elderlytrack.api;

import java.util.List;

import edu.np.ece.elderlytrack.model.ResidentWithMissing;

public class ApiEventListRelativeResidents extends ApiEvent {
    List<ResidentWithMissing> relativeResidents = null;

    public ApiEventListRelativeResidents() {
    }

    public ApiEventListRelativeResidents(List<ResidentWithMissing> missingBeacons) {
        this.relativeResidents = missingBeacons;
    }

    public List<ResidentWithMissing> getRelativeResidents() {
        return relativeResidents;
    }

    public void setRelativeResidents(List<ResidentWithMissing> relativeResidents) {
        this.relativeResidents = relativeResidents;
    }
}
