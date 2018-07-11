package edu.np.ece.wetrack.api;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import edu.np.ece.wetrack.BeaconApplication;
import edu.np.ece.wetrack.MainActivity;
import edu.np.ece.wetrack.model.AuthToken;
import edu.np.ece.wetrack.model.NearbyItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiGateway {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static void apiLoginAnonymous(Context context) {
        // Get a reference to Application for global variables
        BeaconApplication application = BeaconApplication.getInstance();
        // Build API client
        ApiInterface mApiInterface = ApiClient.getApiInterface();

        if (!application.hasInternetConnection()) {
            Log.d(TAG, "No internet connection");
            return;
        }

        EventBus.getDefault().post(new InProgressEvent(true));
        mApiInterface.loginAnonymous().enqueue(new Callback<AuthToken>() {
            @Override
            public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                Log.d(TAG, call.request().toString());
                int statusCode = response.code();
                Log.d(TAG, response.toString());
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Connected to server", Toast.LENGTH_LONG).show();
                    application.session.saveAuthToken(response.body());
                    EventBus.getDefault().post(new InProgressEvent(false));
                } else {
                    application.session.saveAuthToken(null);
                    if (response.message() != null)
                        Toast.makeText(context, "Failed to connect to server. " + response.message(), Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new InProgressEvent(false));
                }
            }

            @Override
            public void onFailure(Call<AuthToken> call, Throwable t) {
                Log.d(TAG, "apiLoginAnonymous(): Error loading from API " + t.getMessage());
                application.session.saveAuthToken(null);
                EventBus.getDefault().post(new InProgressEvent(false));
            }
        });
    }

    public static void apiBeaconsOfMissing(Context context) {
        // Get a reference to Application for global variables
        BeaconApplication application = BeaconApplication.getInstance();
        // Build API client
        ApiInterface mApiInterface = ApiClient.getApiInterface();

        if (!application.hasInternetConnection()) {
            Log.d(TAG, "No internet connection");
            return;
        }

        if (application.getAuthToken(false) == null) {
            ApiGateway.apiLoginAnonymous(context);
            return;
        }
        String token = application.getAuthToken(false).getToken();

        EventBus.getDefault().post(new InProgressEvent(true));
        mApiInterface.listMissingBeacons(token).enqueue(new Callback<List<NearbyItem>>() {
            @Override
            public void onResponse(Call<List<NearbyItem>> call, Response<List<NearbyItem>> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiBeaconsOfMissingEvent event = new ApiBeaconsOfMissingEvent();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Downloaded missing beacons: " + response.body().toString());
                    event.setSuccessful(true);
                    event.setMissingBeacons(response.body());
//                    for (NearbyItem b : response.body()) {
//                        allMissingBeaconMap.put((b.getUuid() + "," + b.getMajor() + "," + b.getMinor()).toUpperCase(), b);
//                    }
                } else {
                    event.setSuccessful(false);
                    if (response.code() == 401) {
                        apiLoginAnonymous(context);
                        return;
                    }
                }
                EventBus.getDefault().post(event);
                EventBus.getDefault().post(new InProgressEvent(false));
            }

            @Override
            public void onFailure(Call<List<NearbyItem>> call, Throwable t) {
                Log.d(TAG, "apiBeaconsOfMissing(): Error loading from API " + t.getMessage());
                ApiBeaconsOfMissingEvent event = new ApiBeaconsOfMissingEvent();
                event.setResponded(false);
                EventBus.getDefault().post(event);
                EventBus.getDefault().post(new InProgressEvent(false));
            }
        });
    }
}
