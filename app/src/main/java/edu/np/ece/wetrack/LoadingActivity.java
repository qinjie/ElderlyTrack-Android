package edu.np.ece.wetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;

import butterknife.BindView;

/**
 * Created by hoanglong on 17-Nov-16.
 */

public class LoadingActivity extends AppCompatActivity {
    @BindView(R.id.textView3)
    TextView textView;

    @BindView(R.id.ivLoading)
    ImageView imageView;

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String userID = sharedPref.getString("userID-WeTrack", "");//get user ID from sharepreference, if no user id, used ""
            if (userID.equals("")) {//when there is no logins yet
                intent.setClass(LoadingActivity.this, LoginActivity.class);

            } else {//already logged in
                intent.setClass(LoadingActivity.this, MainActivity.class);

            }
            startActivity(intent);
            LoadingActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        //below: connect to aws mobile client
        AWSMobileClient.getInstance().initialize(this).execute();


        mHandler.sendEmptyMessageDelayed(1, 2000);//delay 2secs
    }
}

