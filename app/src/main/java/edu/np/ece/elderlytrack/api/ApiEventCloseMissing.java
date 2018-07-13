package edu.np.ece.elderlytrack.api;

import java.util.List;

import edu.np.ece.elderlytrack.model.Missing;

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
