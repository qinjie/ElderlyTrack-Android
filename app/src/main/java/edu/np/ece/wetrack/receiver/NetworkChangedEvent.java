package edu.np.ece.wetrack.receiver;

public class NetworkChangedEvent {

    private boolean internetConnected;

    public NetworkChangedEvent(boolean internetConnected) {
        this.internetConnected = internetConnected;
    }

    public boolean isInternetConnected() {
        return internetConnected;
    }
}
