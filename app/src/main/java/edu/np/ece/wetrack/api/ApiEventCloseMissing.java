package edu.np.ece.wetrack.api;

import java.util.List;

import edu.np.ece.wetrack.model.Missing;

public class ApiEventCloseMissing extends ApiEvent {
    List<Missing> closedMissings = null;

    public ApiEventCloseMissing() {
    }

    public ApiEventCloseMissing(List<Missing> closedMissings) {
        this.closedMissings = closedMissings;
    }

    public List<Missing> getClosedMissings() {
        return closedMissings;
    }

    public void setClosedMissings(List<Missing> closedMissings) {
        this.closedMissings = closedMissings;
    }
}
