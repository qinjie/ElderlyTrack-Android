package edu.np.ece.wetrack.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import edu.np.ece.wetrack.model.AuthToken;
import edu.np.ece.wetrack.model.NearbyItem;

public class UserSession {
    private static String TAG = UserSession.class.getSimpleName();

    // Shared Preferences reference
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    // Sharedpref file name
    private static final String PREFER_NAME = "ElderlyTrackPref";
    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Context
    Context context;

    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_ALL_MISSING_BEACON_MAP = "all_missing_beacon_map";

    // Constructor
    public UserSession(Context context) {
        Log.d(TAG, "UserSession()");
        this.context = context;

        pref = this.context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public void saveAuthToken(AuthToken authToken) {
        Log.d(TAG, "saveAuthToken()");
        if (authToken != null) {
            Gson gson = new Gson();
            String json = gson.toJson(authToken);
            editor.putString(KEY_ACCOUNT, json);
        } else {
            editor.putString(KEY_ACCOUNT, null);
        }
        // commit changes
        editor.commit();
    }

    //Create login session
    public AuthToken loadAuthToken() {
        String json = pref.getString(KEY_ACCOUNT, null);
        Log.d(TAG, "loadAuthToken(): " + json);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, AuthToken.class);
        } else {
            return null;
        }
    }

    //Create login session
    public void saveAllMissingBeaconMap(Map<String, NearbyItem> allMissingBeaconMap) {
        Log.d(TAG, "saveAllMissingBeaconMap(): " + allMissingBeaconMap.toString());
        if (allMissingBeaconMap != null) {
            Gson gson = new Gson();
            String json = gson.toJson(allMissingBeaconMap);
            editor.putString(KEY_ALL_MISSING_BEACON_MAP, json);
        } else {
            editor.putString(KEY_ALL_MISSING_BEACON_MAP, null);
        }
        // commit changes
        editor.commit();
    }

    //Create login session
    public Map<String, NearbyItem> loadAllMissingBeaconMap() {
        String json = pref.getString(KEY_ALL_MISSING_BEACON_MAP, null);
        Log.d(TAG, "loadAllMissingBeaconMap(): " + json);
        if (json != null) {
            Gson gson = new Gson();
            java.lang.reflect.Type type = new TypeToken<HashMap<String, NearbyItem>>() {
            }.getType();
            return gson.fromJson(json, type);
        } else {
            return null;
        }
    }

    public String loadJsonString(String key) {
        Log.d(TAG, "loadJsonString(): key=" + key);
        return pref.getString(key, null);
    }

    public void saveJsonString(String key, String jsonString) {
        Log.d(TAG, "loadJsonString(): key=" + key + ", value=" + jsonString);
        editor.putString(key, jsonString);
        // commit changes
        editor.commit();
    }

    /**
     * Clear session details
     */
    public void clearSession() {
        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    public interface SessionChangedListener {
        void onUserSessionChange(AuthToken authToken);
    }

}