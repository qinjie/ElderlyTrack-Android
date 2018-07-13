package edu.np.ece.wetrack;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import edu.np.ece.wetrack.api.ApiGateway;

public class ScheduleDownloadMissingBeacons extends JobService {
    private static String TAG = ScheduleDownloadMissingBeacons.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob()");
        ApiGateway.apiListBeaconsOfMissing();
        return false;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob()");
        return false;
    }
}