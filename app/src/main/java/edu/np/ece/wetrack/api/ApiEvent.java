package edu.np.ece.wetrack.api;

public class ApiEvent {
    protected boolean isResponded = false;
    protected boolean isSuccessful = false;
    protected int statusCode;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isResponded() {
        return isResponded;
    }

    public void setResponded(boolean responded) {
        this.isResponded = responded;
    }

}
