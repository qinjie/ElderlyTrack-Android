package edu.np.ece.wetrack.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import edu.np.ece.wetrack.BeaconApplication;
import edu.np.ece.wetrack.model.Location;
import retrofit2.Response;

public class BeaconLocationJob extends Job {
    public static final String TAG = BeaconLocationJob.class.getCanonicalName();

    // Get a reference to Application for global variables
    private static BeaconApplication application = BeaconApplication.getInstance();
    // Build API client
    private static ApiInterface apiInterface = ApiClient.getApiInterface();

    private static EventBus eventBus = EventBus.getDefault();

    public static final int PRIORITY_NORMAL = 1;
    private JsonObject data;

    // JsonObject Format { "beacon_id":5, "longitude":2, "latitude":2, "address": "address" }
    public BeaconLocationJob(JsonObject data) {
        super(new Params(PRIORITY_NORMAL)
                .requireNetwork()
                .singleInstanceBy(TAG)
                .addTags(TAG)
                .groupBy(TAG)
//                .persist()
                .delayInMs(500)
        );
        this.data = data;
        Log.i(TAG, "BeaconLocationJob(): " + data.toString());
    }

    @Override
    public void onRun() throws Throwable {
        String token = application.getAuthToken(false).getToken();
        Location location = apiCreateLocation(token, this.data);
    }

    private Location apiCreateLocation(String token, JsonObject location) throws IOException {
        String contentType = "application/json";
        Response<Location> response = apiInterface.createLocation(token, contentType, location).execute();
        return response.body();
    }

    @Override
    public void onAdded() {
        Log.i(TAG, "onAdded(): " + this.data.toString() );
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Log.d(TAG, "onCancel()：" + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        Log.d(TAG, "shouldReRunOnThrowable(): runCount=" + runCount + " maxRunCount=" + maxRunCount);
        return RetryConstraint.RETRY;
    }

    @Override
    protected int getRetryLimit() {
        return 3;
    }
}