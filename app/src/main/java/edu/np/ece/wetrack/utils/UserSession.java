package edu.np.ece.wetrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import edu.np.ece.wetrack.model.AuthToken;

public class UserSession {

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

    // Constructor
    public UserSession(Context context) {
        this.context = context;

        pref = this.context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public void saveAuthToken(AuthToken authToken) {
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
        if (json != null) {
            Gson gson = new Gson();
            AuthToken authToken = gson.fromJson(json, AuthToken.class);
            return authToken;
        } else {
            return null;
        }
    }

    public void loadJsonString(String key) {
        String json = pref.getString(key, null);
    }

    public void saveJsonString(String key, String jsonString) {
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