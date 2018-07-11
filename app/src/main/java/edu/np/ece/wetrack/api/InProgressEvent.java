package edu.np.ece.wetrack.api;

public class InProgressEvent {

    boolean inProgress;

    public InProgressEvent(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
}
