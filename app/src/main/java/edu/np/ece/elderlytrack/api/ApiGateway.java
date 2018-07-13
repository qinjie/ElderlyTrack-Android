package edu.np.ece.elderlytrack.api;

import android.util.Log;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import edu.np.ece.elderlytrack.BeaconApplication;
import edu.np.ece.elderlytrack.MainActivity;
import edu.np.ece.elderlytrack.model.ApiMessage;
import edu.np.ece.elderlytrack.model.AuthToken;
import edu.np.ece.elderlytrack.model.LocationWithBeacon;
import edu.np.ece.elderlytrack.model.Missing;
import edu.np.ece.elderlytrack.model.MissingWithResident;
import edu.np.ece.elderlytrack.model.NearbyItem;
import edu.np.ece.elderlytrack.model.ResidentWithMissing;
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
                    event.setMessage(response.message());
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
                ApiEventLogin event = new ApiEventLogin();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
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
                    event.setMessage(response.message());
                }
                EventBus.getDefault().post(event);
            }

            @Override
            public void onFailure(Call<AuthToken> call, Throwable t) {
                Log.d(TAG, "API Error:" + t.getMessage());
                ApiEventLogin event = new ApiEventLogin();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
            }
        });
    }


    public static void apiForgotPassword(String email) {
        Log.d(TAG, "apiForgotPassword() " + email);
        if (!application.isInternetConnected()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("email", email);

        String contentType = "application/json";
        apiInterface.forgotPassword(contentType, obj).enqueue(new Callback<ApiMessage>() {
            @Override
            public void onResponse(Call<ApiMessage> call, Response<ApiMessage> response) {
                Log.d(TAG, "apiForgotPassword() " + call.request().toString());
                Log.d(TAG, "apiForgotPassword() " + response.toString());

                ApiEventForgotPassword event = new ApiEventForgotPassword();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, response.body().toString());
                    event.setSuccessful(true);
                    event.setMessage(response.body().getMessage());
                } else {
                    Log.d(TAG, response.message());
                    event.setMessage(response.message());
                }
                EventBus.getDefault().post(event);
            }

            @Override
            public void onFailure(Call<ApiMessage> call, Throwable t) {
                Log.d(TAG, "API apiForgotPassword() Error:" + t.getMessage());
                ApiEventForgotPassword event = new ApiEventForgotPassword();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
            }
        });
        Log.d(TAG, "apiForgotPassword() done");
    }

    public static void apiResetPassword(String email, String token, String newPassword) {
        if (!application.isInternetConnected()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("email", email);
        obj.addProperty("token", token);
        obj.addProperty("password", newPassword);

        String contentType = "application/json";
        apiInterface.resetPassword(contentType, obj).enqueue(new Callback<AuthToken>() {
            @Override
            public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiEventResetPassword event = new ApiEventResetPassword();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, response.body().toString());
                    event.setSuccessful(true);
                    event.setAuthToken(response.body());
                } else {
                    Log.d(TAG, response.message());
                    event.setMessage(response.message());
                }
                EventBus.getDefault().post(event);
            }

            @Override
            public void onFailure(Call<AuthToken> call, Throwable t) {
                Log.d(TAG, "API Error:" + t.getMessage());
                ApiEventResetPassword event = new ApiEventResetPassword();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
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
                    event.setMessage(response.message());
                }
                EventBus.getDefault().post(event);
//                EventBus.getDefault().post(new EventInProgress(false));
            }

            @Override
            public void onFailure(Call<List<NearbyItem>> call, Throwable t) {
                Log.d(TAG, "apiListBeaconsOfMissing(): Error loading from API " + t.getMessage());

                ApiEventListBeaconsOfMissing event = new ApiEventListBeaconsOfMissing();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
            }
        });
    }

    public static void apiReportMissing(int residentId, String remark) {
        if (!application.isInternetConnected()) return;
        AuthToken authoToken = application.getAuthToken(true);
        if (authoToken == null || authoToken.getToken() == null) {
            EventBus.getDefault().post(new EventTokenExpired(true));
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
        obj.addProperty("remark", remark);
        obj.addProperty("reported_by", authoToken.getUser().getId());

        Log.d(TAG, obj.toString());
        apiInterface.reportMissingCase(token, contentType, obj).enqueue(new Callback<MissingWithResident>() {
            @Override
            public void onResponse(Call<MissingWithResident> call, Response<MissingWithResident> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiEventReportMissing event = new ApiEventReportMissing();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, response.body().toString());
                    event.setSuccessful(true);
                    event.setMissingWithResident(response.body());
                } else {
                    event.setMessage(response.message());
                    Log.d(TAG, response.message());
                }
                EventBus.getDefault().post(event);
            }

            @Override
            public void onFailure(Call<MissingWithResident> call, Throwable t) {
                Log.d(TAG, "API Error:" + t.getMessage());
                ApiEventReportMissing event = new ApiEventReportMissing();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
            }
        });
    }

    public static void apiCloseMissing(int residentId, String remark) {
        if (!application.isInternetConnected()) return;
        AuthToken authoToken = application.getAuthToken(true);
        if (authoToken == null || authoToken.getToken() == null) {
            EventBus.getDefault().post(new EventTokenExpired(true));
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
                ApiEventCloseMissing event = new ApiEventCloseMissing();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
            }
        });
    }


    public static void apiListLocationByMissingId(int missingId) {
        if (!application.isInternetConnected()) return;
        if (application.getAuthToken(false) == null) {
            EventBus.getDefault().post(new EventTokenExpired(true));
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
                ApiEventListLocationByMissingId event = new ApiEventListLocationByMissingId();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
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

    public static void apiMissingResidents() {
        if (!application.isInternetConnected()) return;
        if (application.getAuthToken(true) == null) {
            EventBus.getDefault().post(new EventTokenExpired(true));
            return;
        }

        String token = application.getAuthToken(true).getToken();

        apiInterface.listMissingResidents(token).enqueue(new Callback<List<ResidentWithMissing>>() {
            @Override
            public void onResponse(Call<List<ResidentWithMissing>> call, Response<List<ResidentWithMissing>> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiEventListMissingResidents event = new ApiEventListMissingResidents();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Downloaded missing residents: " + response.body().toString());
                    event.setSuccessful(true);
                    event.setMissingResidents(response.body());
                } else {
                    if (response.code() == 401) {
                        Log.d(TAG, "Token expired.");
                        EventBus.getDefault().post(new EventTokenExpired(true));
                        return;
                    }
                    if (response.code() == 404) {
                        Log.d(TAG, "No trail for this missing case.");
                    }
                }
                EventBus.getDefault().post(event);
            }

            @Override
            public void onFailure(Call<List<ResidentWithMissing>> call, Throwable t) {
                Log.d(TAG, "apiMissingResidents(): Error loading from API " + t.getMessage());
                ApiEventListMissingResidents event = new ApiEventListMissingResidents();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
            }
        });
    }


    public static boolean apiListRelativeResidents() {
        if (!application.isInternetConnected()) return false;
        AuthToken authToken = application.getAuthToken(true);
        if (authToken == null || authToken.getToken() == null) {
            EventBus.getDefault().post(new EventTokenExpired(true));
            Log.e(TAG, "Token not found");
            return false;
        }
        if (authToken.getUser() == null) {
            Log.e(TAG, "User not authorized");
            return false;
        }

        String token = application.getAuthToken(true).getToken();
        apiInterface.listRelativeResidents(token).enqueue(new Callback<List<ResidentWithMissing>>() {
            @Override
            public void onResponse(Call<List<ResidentWithMissing>> call, Response<List<ResidentWithMissing>> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                ApiEventListRelativeResidents event = new ApiEventListRelativeResidents();
                event.setResponded(true);
                event.setStatusCode(response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Downloaded relative residents: " + response.body().toString());
                    event.setSuccessful(true);
                    event.setRelativeResidents(response.body());
                } else {
                    if (response.code() == 401) {
                        Log.d(TAG, "Token expired.");
                        EventBus.getDefault().post(new EventTokenExpired(true));
                        return;
                    }
                    if (response.code() == 404) {
                        Log.d(TAG, "No relative resident for current user.");
                    }
                }
                EventBus.getDefault().post(event);
            }

            @Override
            public void onFailure(Call<List<ResidentWithMissing>> call, Throwable t) {
                Log.d(TAG, "apiMissingResidents(): Error loading from API " + t.getMessage());
                ApiEventListRelativeResidents event = new ApiEventListRelativeResidents();
                event.setMessage(t.getMessage());
                EventBus.getDefault().post(event);
            }
        });
        return true;
    }

}
