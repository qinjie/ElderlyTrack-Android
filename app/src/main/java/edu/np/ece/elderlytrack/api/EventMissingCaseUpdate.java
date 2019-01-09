package edu.np.ece.elderlytrack.api;

import android.util.Log;

public class EventMissingCaseUpdate {
    private static String TAG = EventMissingCaseUpdate.class.getSimpleName();

    boolean isMissing;
    String remark;

    public EventMissingCaseUpdate(boolean isMissing, String remark) {
        Log.d(TAG, "EventMissingCaseUpdate(): " + String.valueOf(isMissing) + " " + remark);
        this.isMissing = isMissing;
        this.remark = remark;
    }

    public boolean isMissing() {
        return isMissing;
    }

    public void setMissing(boolean missing) {
        isMissing = missing;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
