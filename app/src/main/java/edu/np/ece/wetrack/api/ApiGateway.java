package edu.np.ece.wetrack.api;

import android.util.Log;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import edu.np.ece.wetrack.BeaconApplication;
import edu.np.ece.wetrack.MainActivity;
import edu.np.ece.wetrack.model.AuthToken;
import edu.np.ece.wetrack.model.LocationWithBeacon;
import edu.np.ece.wetrack.model.Missing;
import edu.np.ece.wetrack.model.NearbyItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiGateway {
    private static final String TAG = MainActivity.class.getSimpleName();
    // Get a reference to Application for global variables
    private static BeaconApplication application = BeaconApplication.getInstance();
    // Build API client
    private static ApiInterface apiInterface = ApiClient.getApiInterface();

    private static EventBus eventBus = EventBus.getDefault();


    public static void apiLoginAnonymous() {
        if (!application.isInternetConnected()) return;

//        EventBus.getDefault().post(new EventInProgress(true));
        apiInterface.loginAnonymous().enqueue(new Callback<AuthToken>() {
            @Override
            public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiEventLogin event = new ApiEventLogin();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, response.body().toString());
                    event.setSuccessful(true);
                    event.setAuthToken(response.body());
//                    application.session.saveAuthToken(response.body());
                } else {
                    Log.d(TAG, response.message());
//                    application.session.saveAuthToken(null);
//                    if (response.message() != null)
//                        Toast.makeText(context, "Failed to connect to server. " + response.message(), Toast.LENGTH_SHORT).show();
                }
                EventBus.getDefault().post(event);
//                EventBus.getDefault().post(new EventInProgress(false));
            }

            @Override
            public void onFailure(Call<AuthToken> call, Throwable t) {
                Log.d(TAG, "API Error:" + t.getMessage());
                EventBus.getDefault().post(new ApiEventLogin());
//                EventBus.getDefault().post(new EventInProgress(false));
            }
        });
    }

    public static void apiListBeaconsOfMissing() {
        if (!application.isInternetConnected()) return;
        if (application.getAuthToken(false) == null) {
            EventBus.getDefault().post(new EventTokenExpired(true));
            return;
        }

        String token = application.getAuthToken(false).getToken();
//        EventBus.getDefault().post(new EventInProgress(true));
        apiInterface.listMissingBeacons(token).enqueue(new Callback<List<NearbyItem>>() {
            @Override
            public void onResponse(Call<List<NearbyItem>> call, Response<List<NearbyItem>> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiEventListBeaconsOfMissing event = new ApiEventListBeaconsOfMissing();
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
                    if (response.code() == 401) {
                        Log.d(TAG, "Token expired.");
                        EventBus.getDefault().post(new EventTokenExpired(true));
                        return;
                    }
                }
                EventBus.getDefault().post(event);
//                EventBus.getDefault().post(new EventInProgress(false));
            }

            @Override
            public void onFailure(Call<List<NearbyItem>> call, Throwable t) {
                Log.d(TAG, "apiListBeaconsOfMissing(): Error loading from API " + t.getMessage());
                EventBus.getDefault().post(new ApiEventListBeaconsOfMissing());
//                EventBus.getDefault().post(new EventInProgress(false));
            }
        });
    }


    public static void apiLoginWithEmail(String email, String pwd) {
        if (!application.isInternetConnected()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("email", email);
        obj.addProperty("password", pwd);

        String contentType = "application/json";
//        EventBus.getDefault().post(new EventInProgress(true));
        apiInterface.loginWithEmail(contentType, obj).enqueue(new Callback<AuthToken>() {
            @Override
            public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiEventLogin event = new ApiEventLogin();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, response.body().toString());
                    event.setSuccessful(true);
                    event.setAuthToken(response.body());
                } else {
                    Log.d(TAG, response.message());
                }
                EventBus.getDefault().post(event);
//                EventBus.getDefault().post(new EventInProgress(false));
            }

            @Override
            public void onFailure(Call<AuthToken> call, Throwable t) {
                Log.d(TAG, "API Error:" + t.getMessage());
                EventBus.getDefault().post(new ApiEventLogin());
//                EventBus.getDefault().post(new EventInProgress(false));
//                Toast.makeText(getContext(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void apiCloseMissing(int residentId, String remark) {
        if (!application.isInternetConnected()) return;

        AuthToken authoToken = application.getAuthToken(true);
        if (authoToken == null || authoToken.getToken() == null) {
            Log.e(TAG, "Token not found");
            return;
        }
        if (authoToken.getUser() == null) {
            Log.e(TAG, "User not authorized");
            return;
        }

        String token = authoToken.getToken();
        String contentType = "application/json";

        JsonObject obj = new JsonObject();
        obj.addProperty("resident_id", residentId);
        obj.addProperty("closure", remark);
        obj.addProperty("closed_by", authoToken.getUser().getId());

        Log.d(TAG, obj.toString());
//        EventBus.getDefault().post(new EventInProgress(true));
        apiInterface.closeMissingCase(token, contentType, obj).enqueue(new Callback<List<Missing>>() {
            @Override
            public void onResponse(Call<List<Missing>> call, Response<List<Missing>> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiEventCloseMissing event = new ApiEventCloseMissing();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, response.body().toString());
                    event.setSuccessful(true);
                    event.setClosedMissings(response.body());
                    // Go back to Resident Detail screen
//                    Toast.makeText(getContext(), "Close missing successful", Toast.LENGTH_SHORT).show();
//                    getActivity().onBackPressed();
                } else {
                    Log.d(TAG, response.message());
//                    Toast.makeText(getContext(), "Close missing unsuccessful. Status code = " + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                }
                EventBus.getDefault().post(event);
//                EventBus.getDefault().post(new EventInProgress(false));
            }

            @Override
            public void onFailure(Call<List<Missing>> call, Throwable t) {
                Log.d(TAG, "API Error:" + t.getMessage());
                EventBus.getDefault().post(new ApiEventCloseMissing());
//                EventBus.getDefault().post(new EventInProgress(false));
            }
        });
    }


    public static void apiListLocationByMissingId(int missingId) {
        if (!application.isInternetConnected()) return;
        if (application.getAuthToken(false) == null) {
            return;
        }

        String token = application.getAuthToken(false).getToken();
//        EventBus.getDefault().post(new EventInProgress(true));
        apiInterface.listLocationByMissingId(token, missingId).enqueue(new Callback<List<LocationWithBeacon>>() {
            @Override
            public void onResponse(Call<List<LocationWithBeacon>> call, Response<List<LocationWithBeacon>> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiEventListLocationByMissingId event = new ApiEventListLocationByMissingId();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Downloaded location for missing case: " + response.body().toString());
                    event.setSuccessful(true);
                    event.setLocationsWithBeacon(response.body());
//                    locationList = new ArrayList<>(response.body());
//                    mAdapter.updateItems(locationList);
                } else {
                    if (response.code() == 401) {
                        Log.d(TAG, "Token expired.");
                        EventBus.getDefault().post(new EventTokenExpired(true));
                        return;
//                        Toast.makeText(getContext(), "Token expired.", Toast.LENGTH_SHORT).show();
                    }
                    if (response.code() == 404) {
                        Log.d(TAG, "No trail for this missing case.");
//                        Toast.makeText(getContext(), "No trail for this missing case.", Toast.LENGTH_SHORT).show();
                    }
                }
                EventBus.getDefault().post(event);
//                EventBus.getDefault().post(new EventInProgress(false));
            }

            @Override
            public void onFailure(Call<List<LocationWithBeacon>> call, Throwable t) {
                Log.d(TAG, "apiListLocationByMissingId(): Error loading from API " + t.getMessage());
                EventBus.getDefault().post(new ApiEventListLocationByMissingId());
//                EventBus.getDefault().post(new EventInProgress(false));
            }
        });
    }

//    // JsonObject Format { "beacon_id":5, "longitude":2, "latitude":2, "address": "address" }
//    public static void apiCreateLocation(JsonObject location) {
//        if (!application.isInternetConnected()) return;
//        if (application.getAuthToken(false) == null) {
//            return;
//        }
//
//        String token = application.getAuthToken(false).getToken();
//        String contentType = "application/json";
//        EventBus.getDefault().post(new EventInProgress(true));
//
//        apiInterface.createLocation(token, contentType, location).enqueue(new Callback<Location>() {
//            @Override
//            public void onResponse(Call<Location> call, Response<Location> response) {
//                Log.d(TAG, call.request().toString());
//                Log.d(TAG, response.toString());
//            }
//
//            @Override
//            public void onFailure(Call<Location> call, Throwable t) {
//                Log.d(TAG, "Error calling API " + t.getMessage());
//            }
//        });
//    }
}
