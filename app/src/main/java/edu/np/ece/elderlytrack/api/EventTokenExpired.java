package edu.np.ece.elderlytrack.api;

public class EventTokenExpired {
    private static String TAG = EventTokenExpired.class.getSimpleName();

    boolean tokenExpired;

    public EventTokenExpired(boolean tokenExpired) {
        this.tokenExpired = tokenExpired;
    }

    public boolean isTokenExpired() {
        return tokenExpired;
    }

    public void setTokenExpired(boolean tokenExpired) {
        this.tokenExpired = tokenExpired;
    }
}
