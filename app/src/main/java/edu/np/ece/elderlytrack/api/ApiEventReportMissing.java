package edu.np.ece.elderlytrack.api;

import edu.np.ece.elderlytrack.model.MissingWithResident;

public class ApiEventReportMissing extends ApiEvent {
    MissingWithResident missingWithResident = null;

    public MissingWithResident getMissingWithResident() {
        return missingWithResident;
    }

    public void setMissingWithResident(MissingWithResident missingWithResident) {
        this.missingWithResident = missingWithResident;
    }
}
