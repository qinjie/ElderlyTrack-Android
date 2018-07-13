package edu.np.ece.wetrack.api;

import android.util.Log;

public class EventInProgress {
    private static String TAG = EventInProgress.class.getSimpleName();

    boolean inProgress;

    public EventInProgress(boolean inProgress) {
        Log.d(TAG, "EventInProgress(): " + String.valueOf(inProgress));
        this.inProgress = inProgress;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
}
