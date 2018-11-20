package edu.np.ece.elderlytrack.api;


import com.google.gson.JsonObject;

import java.util.List;

import edu.np.ece.elderlytrack.model.ApiMessage;
import edu.np.ece.elderlytrack.model.AuthToken;
import edu.np.ece.elderlytrack.model.BeaconProfile;
import edu.np.ece.elderlytrack.model.Location;
import edu.np.ece.elderlytrack.model.LocationWithBeacon;
import edu.np.ece.elderlytrack.model.Missing;
import edu.np.ece.elderlytrack.model.MissingWithResident;
import edu.np.ece.elderlytrack.model.NearbyItem;
import edu.np.ece.elderlytrack.model.ResidentWithMissing;
import edu.np.ece.elderlytrack.model.Setting;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiInterface {

    @GET("api/v1/user/login_anonymous")
    Call<AuthToken> loginAnonymous();

    @POST("api/v1/user/login_with_email")
    Call<AuthToken> loginWithEmail(@Header("Content-Type") String contentType, @Body JsonObject user);

    @POST("api/v1/user/forgot_password")
    Call<ApiMessage> forgotPassword(@Header("Content-Type") String contentType, @Body JsonObject email);

    @POST("api/v1/user/reset_password")
    Call<AuthToken> resetPassword(@Header("Content-Type") String contentType, @Body JsonObject obj);

    @GET("api/v1/beacon/missing")
    Call<List<NearbyItem>> listMissingBeacons(@Header("Authorization") String token);

    @GET("api/v1/beacon/resident/{resident_id}")
    Call<List<BeaconProfile>> listBeaconsByResidentId(@Header("Authorization") String token, @Path("resident_id") int resident_id);

    @PUT("api/v1/beacon/{id}/status/{status}")
    Call<BeaconProfile> updateBeaconStatus(@Header("Authorization") String token, @Path("id") int beacon_id, @Path("status") int status);

    @GET("api/v1/resident/missing")
    Call<List<ResidentWithMissing>> listMissingResidents(@Header("Authorization") String token);

    @GET("api/v1/resident/relative")
    Call<List<ResidentWithMissing>> listRelativeResidents(@Header("Authorization") String token);

    @GET("api/v1/resident/{id}")
    Call<ResidentWithMissing> getResident(@Header("Authorization") String token, @Path("id") int resident_id);

    @POST("api/v1/location")
    Call<Location> createLocation(@Header("Authorization") String token, @Header("Content-Type") String type, @Body JsonObject location);

    @GET("api/v1/location/missing/{missing_id}")
    Call<List<LocationWithBeacon>> listLocationByMissingId(@Header("Authorization") String token, @Path("missing_id") int missing_id);

    @PUT("api/v1/missing/close2")
    Call<List<Missing>> closeMissingCase(@Header("Authorization") String token, @Header("Content-Type") String type, @Body JsonObject obj);

    @POST("api/v1/missing2")
    Call<MissingWithResident> reportMissingCase(@Header("Authorization") String token, @Header("Content-Type") String type, @Body JsonObject obj);

    @GET("api/v1/setting")
    Call<List<Setting>> listSetting(@Header("Authorization") String token);
}

