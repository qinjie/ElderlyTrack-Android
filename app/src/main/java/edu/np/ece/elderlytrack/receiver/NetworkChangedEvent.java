package edu.np.ece.elderlytrack.receiver;

public class NetworkChangedEvent {

    private boolean internetConnected;

    public NetworkChangedEvent(boolean internetConnected) {
        this.internetConnected = internetConnected;
    }

    public boolean isInternetConnected() {
        return internetConnected;
    }
}
