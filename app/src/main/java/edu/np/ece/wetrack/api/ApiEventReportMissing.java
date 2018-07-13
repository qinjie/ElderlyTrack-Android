package edu.np.ece.wetrack.api;

import edu.np.ece.wetrack.model.MissingWithResident;

public class ApiEventReportMissing extends ApiEvent {
    MissingWithResident missingWithResident = null;

    public MissingWithResident getMissingWithResident() {
        return missingWithResident;
    }

    public void setMissingWithResident(MissingWithResident missingWithResident) {
        this.missingWithResident = missingWithResident;
    }
}
