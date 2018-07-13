package edu.np.ece.wetrack.api;

import java.util.List;

import edu.np.ece.wetrack.model.ResidentWithMissing;

public class ApiEventListMissingResidents extends ApiEvent {
    List<ResidentWithMissing> missingResidents = null;

    public ApiEventListMissingResidents() {
    }

    public ApiEventListMissingResidents(List<ResidentWithMissing> missingBeacons) {
        this.missingResidents = missingBeacons;
    }

    public List<ResidentWithMissing> getMissingResidents() {
        return missingResidents;
    }

    public void setMissingResidents(List<ResidentWithMissing> missingResidents) {
        this.missingResidents = missingResidents;
    }
}
