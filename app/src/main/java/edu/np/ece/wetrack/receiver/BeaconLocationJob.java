package edu.np.ece.wetrack.receiver;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.gson.JsonObject;

import edu.np.ece.wetrack.BeaconApplication;
import edu.np.ece.wetrack.api.ApiClient;
import edu.np.ece.wetrack.api.ApiInterface;
import edu.np.ece.wetrack.model.Location;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BeaconLocationJob extends Job {
    public static final String TAG = BeaconLocationJob.class.getCanonicalName();
    public static final int PRIORITY_NORMAL = 1;

    private JsonObject location;
    private BeaconApplication application;
    private ApiInterface mApiInterface;

    public BeaconLocationJob(BeaconApplication application, JsonObject location) {
        super(new Params(PRIORITY_NORMAL)
                .requireNetwork()
                .singleInstanceBy(TAG)
                .addTags(TAG)
        );
        this.application = application;
        this.location = location;
        this.mApiInterface = ApiClient.getApiInterface();
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        String token = application.getAuthToken(false).getToken();
        String contentType = "application/json";
        apiCreateLocation(token, contentType, this.location);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Log.d(TAG, "cancelReason：" + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        Log.d(TAG, "runCount: " + runCount + " maxRunCount: " + maxRunCount);
        return RetryConstraint.RETRY;
    }

    private void apiCreateLocation(String token, String contentType, JsonObject location) {
        mApiInterface.createLocation(token, contentType, location).enqueue(new Callback<Location>() {
            @Override
            public void onResponse(Call<Location> call, Response<Location> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());
            }

            @Override
            public void onFailure(Call<Location> call, Throwable t) {
                Log.d(TAG, "Error calling API " + t.getMessage());
            }
        });
    }

    @Override
    protected int getRetryLimit() {
        return 3;
    }
}