package edu.np.ece.wetrack;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.api.idx2qiqap347.ElderlytrackClient;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import edu.np.ece.wetrack.model.EmailInfo;
import edu.np.ece.wetrack.model.UserAccount;

// This statement imports the model class you download from |AMH|.



public class LoginActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

   // private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private int check;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    //private ServerAPI serverAPI; //refer to serverAPI.java

    private UserAccount userAccount; //refer to userAccount.java

    //for aws api
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private ElderlytrackClient apiClient;
    //below: declared for the aws mobilehub sign in
    private SignInUI signin;

    // Build a GoogleSignInClient with the options specified by gso.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/futurathin.TTF");//set fonts
        TextView tvVolunteer = (TextView) findViewById(R.id.tvVolunteer);
        TextView tvRelative = (TextView) findViewById(R.id.tvRelative);

        if (custom_font != null) {
            tvVolunteer.setTypeface(custom_font);
            tvRelative.setTypeface(custom_font);
        }


        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.btnAnonymous).setOnClickListener(this);
//////////////////////////////////////////////////////////////////////////////////////////////////////
        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]
///////////////////////////////////////////////////////////////////////////////////////////////////////
        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //GoogleSignInClient mGooglesignInClient = GoogleSignIn.getClient(this,gso);
        // [END build_client]


        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);//google sign in button
        signInButton.setSize(SignInButton.SIZE_WIDE);
        //signInButton.setScopes(gso.getScopeArray());
        //serverAPI = RetrofitUtils.get().create(ServerAPI.class);//using api
// Create the client for aws api
        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance().getCredentialsProvider())
                .build(ElderlytrackClient.class);

    }

    public boolean isOnline() {
        boolean result;
        ConnectivityManager cm =   (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            result=true;
        }
        else{
            Toast.makeText(getBaseContext(), "Not Online",Toast.LENGTH_SHORT).show();
            result=false;
        }
        return result;
    }
    //method to perform volunteer login

    public void CallCloudLogicAnonymousLogin(){
        final String method = "GET";
        final String path = "/v1/user/login_anonymous";

        //final byte[] content = body.getBytes(StringUtils.UTF8);
        final Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");

        final Map headers = new HashMap<>();

        // Use components to create the api request
        ApiRequest localRequest =
                new ApiRequest(apiClient.getClass().getSimpleName())
                        .withPath(path)
                        .withHttpMethod(HttpMethodName.valueOf(method))
                        .withHeaders(headers)
                        .addHeader("Content-Type", "application/json")
                        .withParameters(parameters);

        //Only set body if it has content.
       // if(body.length() > 0){
          //  localRequest = localRequest
             //       .addHeader("Content-Length", String.valueOf(content.length))
              //      .withBody(content);
        //}


        final ApiRequest request = localRequest;
        // Make network call on background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(LOG_TAG,
                            "Invoking API w/ Request : " +
                                    request.getHttpMethod() + ":" +
                                    request.getPath());

                    final ApiResponse response = apiClient.execute(request);
                    final InputStream responseContentStream = response.getContent();//find a way to convert to json
                    //final InputStream responseContentStream = response.getContent();//find a way to convert to json

                    if (responseContentStream != null) {
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.d(LOG_TAG, "Response : " + responseData);
                        Gson g = new Gson();
                        userAccount = g.fromJson(responseData, UserAccount.class);
                        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        final SharedPreferences.Editor editor = sharedPref.edit();// enable editing of shared preference
                        editor.putString("userRole-WeTrack", "5"); //put user role as 5;so MainActivity can detect it is anonymous signin
                        editor.commit();
                        if (userAccount != null) {
                            //below: save the userID in sharedpreference
                            editor.putString("userID-WeTrack", String.valueOf(userAccount.getId()));
                            //below: save the userToken in shared preference
                            //editor.putString("userToken-WeTrack", userAccount.getToken());
                            //below: set email and name as anonymous and profile pic as nothing
                            EmailInfo account = new EmailInfo("Anonymous", "Anonymous", R.drawable.my_avt + "");
                            Gson gson2 = new Gson();
                            String jsonAccount = gson2.toJson(account);
                            editor.putString("userAccount-WeTrack", jsonAccount);
                            editor.commit();

                            Intent intent = new Intent(getBaseContext(), MainActivity.class);//proceed to MainACtivity
                            startActivity(intent);
                            finish();
                        }
                    }
                    Log.d(LOG_TAG, response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(LOG_TAG, exception.getMessage(), exception);
                    exception.printStackTrace();

                }
            }
        }).start();
    }

    //method to perform login with gmail
    public void callCloudLogicEmailLogin(final JsonObject obj, final GoogleSignInAccount acct) {//added void
        // Create components of api request

        final String method = "POST";
        final String path = "/v1/user/login_with_email";
        final String body = obj.toString();
        final byte[] content = body.getBytes(StringUtils.UTF8);
        final Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");

        final Map headers = new HashMap<>();

        // Use components to create the api request
        ApiRequest localRequest =
                new ApiRequest(apiClient.getClass().getSimpleName())
                        .withPath(path)
                        .withHttpMethod(HttpMethodName.valueOf(method))
                        .withHeaders(headers)
                        .addHeader("Content-Type", "application/json")
                        .withParameters(parameters);

        //Only set body if it has content.
        if(body.length() > 0){
            localRequest = localRequest
                    .addHeader("Content-Length", String.valueOf(content.length))
                    .withBody(content);
        }


        final ApiRequest request = localRequest;
        // Make network call on background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(LOG_TAG,
                            "Invoking API w/ Request : " +
                                    request.getHttpMethod() + ":" +
                                    request.getPath());

                    final ApiResponse response = apiClient.execute(request);
                    final InputStream responseContentStream = response.getContent();//find a way to convert to json
                    //final InputStream responseContentStream = response.getContent();//find a way to convert to json

                    if (responseContentStream != null) {
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.d(LOG_TAG, "Response : " + responseData);

                        Gson g = new Gson();
                        userAccount = g.fromJson(responseData, UserAccount.class);

                        if (userAccount.getRole() != 0 && userAccount.getStatus().equals("10")) {// Condition when user has been registered under admin
                            //Log.i("Email", userAccount.getToken());//show on logcat the email
                            SharedPreferences sharedPref0 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = sharedPref0.edit();
                            //editor.putString("userToken-WeTrack", userAccount.getToken());//save userToken into shared preference
                            editor.putString("userID-WeTrack", userAccount.getId() + "");//save userId into shared preference
                            editor.putString("userRole-WeTrack", String.valueOf(userAccount.getRole()));//save UserRole into sharedPreference
                            editor.commit();
                            EmailInfo account; // refer to EmailInfo.java
                            if (acct.getPhotoUrl() == null) {//condition when there is no profile pic with the account
                                account = new EmailInfo(acct.getEmail(), acct.getDisplayName(), "");
                            } else {
                                account = new EmailInfo(acct.getEmail(), acct.getDisplayName(), acct.getPhotoUrl().toString());
                            }

                            Gson gson2 = new Gson();
                            String jsonAccount = gson2.toJson(account);
                            editor.putString("userAccount-WeTrack", jsonAccount);
                            editor.commit();

                            //signin.login(LoginActivity.this, MainActivity.class).execute();
                            check = 1;


                            /*String deviceToken = sharedPref.getString("deviceToken-WeTrack", "");////get device token from sharepreference, if no device token, used ""
                            JsonParser parser = new JsonParser();
                            JsonObject obj = parser.parse("{\"token\": \"" + deviceToken + "\",\"user_id\": \"" + userAccount.getId() + "\"}").getAsJsonObject();
                            */

                        }
                        else {//condition when user has not been registered by admin
                            SharedPreferences sharedPref1 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = sharedPref1.edit();
                            //editor.putString("userToken-WeTrack", "");//reset usertoken on sharepreference
                            editor.putString("userID-WeTrack", "");//reset userid on sharepreferencce
                            editor.putString("userRole-WeTrack", "");//reset userrole on sharepreferencce
                            editor.commit();
                            signOut();//logged out
                            check = 0;

                        }

                    }
                    Log.d(LOG_TAG, response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(LOG_TAG, exception.getMessage(), exception);
                    exception.printStackTrace();

                }
            }
        }).start();




        //String search  = "BadRequestError";
        //if ( responseData.toLowerCase().indexOf(search.toLowerCase()) == -1 ) {//condition when the user is unregistered

//                        }
                        /*else{//condition when the user is registered
                            Log.i("Email", userAccount.getToken());//show on logcat the email
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("userToken-WeTrack", userAccount.getToken());//save userToken into shared preference
                            editor.putString("userID-WeTrack", userAccount.getId() + "");//save userId into shared preference
                            editor.putString("userRole-WeTrack", String.valueOf(userAccount.getRole()));//save UserRole into sharedPreference
                            editor.commit();
                        }*/


    }


    //////////////////////////////////////////////[onStart]///////////////////////////////////////////////////////////////
    @Override
    public void onStart() { //code in onStart is run whenever you start or resume the app
        super.onStart();




        /////to get email from login////////////might need to movee to other places later////////////////
        /*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null){
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
           if(acct != null){
                String personEmail = acct.getEmail(); //get email from the login
            }
        }*/
        /////////////////////////////////////////////////////////

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
      //  String token = sharedPref.getString("userToken-WeTrack", "");////get user Token from sharepreference, if no user Token, used ""
        String userRole = sharedPref.getString("userRole-WeTrack", "");//get user role from sharepreference, if no user role, used ""
        String userID = sharedPref.getString("userID-WeTrack", "");// //get user ID from sharepreference, if no user id, used ""

        if (!userID.equals("")){
            //auto login
            Intent intent = new Intent(getBaseContext(), MainActivity.class);//go to next activity(logged in)
            startActivity(intent);
            finish();
        }
        else {//refer below

            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
//            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
//                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);//google signinResult not available
                }
            });
        }

        //if (!userID.equals("")) {//auto login in user
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.

           // Intent intent = new Intent(getBaseContext(), MainActivity.class);//go to next activity(logged in)
            //startActivity(intent);
            //finish();

       // } else {//refer below

            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
//            showProgressDialog();
           /* opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
//                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);//google signinResult not available
                }
            });
        }*/

//        mAuth.addAuthStateListener(mAuthListener);
           /* AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
             @Override
                public void onComplete(AWSStartupResult awsStartupResult) {
                SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(LoginActivity.this, SignInUI.class);
                signin.login(LoginActivity.this, MainActivity.class).execute();
                }
            }).execute();*/

       // }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onStop() {
        super.onStop();
    }
    ///////////////////////////////////////////[onActivityResult]////////////////////////////////////////////////////
    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode,final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);//triggered when asking user to select a gmail account



        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {

                @Override
                public void onComplete(AWSStartupResult awsStartupResult) {
                    signin = (SignInUI) AWSMobileClient.getInstance().getClient(LoginActivity.this, SignInUI.class);
                    if(check==1) {
                        signin.login(LoginActivity.this, MainActivity.class).execute();
                    }

                }


            }).execute();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);//get sign in result
            handleSignInResult(result);//do the sign in work*/

        }
    }
    // [END onActivityResult]
//////////////////////////////////////////////////////////////////////////////////////////////////////
    // [START handleSignInResult]



    private void handleSignInResult(GoogleSignInResult result) {// used in onActivityResult above
        //TODO
        //for intenet fail
            /*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            //editor.putString("userToken-WeTrack", "");//reset usertoken on shared preference
            editor.putString("userID-WeTrack", "");//reset userid on sharedpreference
            editor.putString("userRole-WeTrack", "");//reset user role onn sharedpreference
            editor.commit();
            signOut();


            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("Login Failed");
            alertDialog.setMessage("Please turn on internet connection");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();*/

        if (result.isSuccess()) {//when there is sign in result
            final GoogleSignInAccount acct = result.getSignInAccount(); //get account
            EmailInfo email = new EmailInfo(acct.getEmail());//obtained email of the account
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            JsonObject obj = gson.toJsonTree(email).getAsJsonObject();//java script



            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String id = sharedPref.getString("userID-WeTrack", "");////get user ID from sharepreference, if no user id, used ""

            if (id.equals("")) {//used to trigger login user through email from google account

                callCloudLogicEmailLogin(obj,acct);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (check == 0){

                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(LoginActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(LoginActivity.this);
                            }
                            AlertDialog alertDialog = builder.create();
                            alertDialog.setTitle("Login Failed");
                            alertDialog.setMessage("This function can only be used by registered user. You can go to nearest police station for registration or use Anonymous login");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();

                        } //Do something after 5000ms
                    }
                },3000);



                /*serverAPI.loginViaEmail(obj).enqueue(new Callback<UserAccount>() {
                    @Override
                    public void onResponse(Call<UserAccount> call, Response<UserAccount> response) {
                        userAccount = response.body();//retrieve response from the serverAPI

                        if (userAccount != null && userAccount.getResult().equals("correct")) {// Condition when user has been registered under admin
                            Log.i("Email", userAccount.getToken());//show on logcat the email
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("userToken-WeTrack", userAccount.getToken());//save userToken into shared preference
                            editor.putString("userID-WeTrack", userAccount.getId() + "");//save userId into shared preference
                            editor.putString("userRole-WeTrack", String.valueOf(userAccount.getRole()));//save UserRole into sharedPreference
                            editor.commit();

                            EmailInfo account; // refer to EmailInfo.java
                            if (acct.getPhotoUrl() == null) {//condition when there is no profile pic with the account
                                account = new EmailInfo(acct.getEmail(), acct.getDisplayName(), "");
                            } else {
                                account = new EmailInfo(acct.getEmail(), acct.getDisplayName(), acct.getPhotoUrl().toString());
                            }

                            Gson gson2 = new Gson();
                            String jsonAccount = gson2.toJson(account);
                            editor.putString("userAccount-WeTrack", jsonAccount);
                            editor.commit();

                            String deviceToken = sharedPref.getString("deviceToken-WeTrack", "");////get device token from sharepreference, if no device token, used ""
                            JsonParser parser = new JsonParser();
                            JsonObject obj = parser.parse("{\"token\": \"" + deviceToken + "\",\"user_id\": \"" + userAccount.getId() + "\"}").getAsJsonObject();

                            serverAPI.sendToken(obj).enqueue(new Callback<UserAccount>() {
                                @Override
                                public void onResponse(Call<UserAccount> call, Response<UserAccount> response) {

                                }

                                @Override
                                public void onFailure(Call<UserAccount> call, Throwable t) {

                                }
                            });
                            signin.login(LoginActivity.this, MainActivity.class).execute();
                            //Intent intent = new Intent(getBaseContext(), MainActivity.class);
                            //startActivity(intent);
                            finish();

                        } else {//condition when user has not been registered by admin
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("userToken-WeTrack", "");//reset usertoken on sharepreference
                            editor.putString("userID-WeTrack", "");//reset userid on sharepreferencce
                            editor.putString("userRole-WeTrack", "");//reset userrole on sharepreferencce
                            editor.commit();
                            signOut();//logged out

                            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                            alertDialog.setTitle("Login Failed");
                            alertDialog.setMessage("This function can only be used by registered user. You can go to nearest police station for registration or use Anonymous login");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();


                        }
                    }

                    @Override
                    public void onFailure(Call<UserAccount> call, Throwable t) {//triggered when there is no internet
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("userToken-WeTrack", "");//reset usertoken on shared preference
                        editor.putString("userID-WeTrack", "");//reset userid on sharedpreference
                        editor.putString("userRole-WeTrack", "");//reset user role onn sharedpreference
                        editor.commit();
                        signOut();


                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                        alertDialog.setTitle("Login Failed");
                        alertDialog.setMessage("Please turn on internet connection");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();

                    }
                });*/
            }


        }
    }
    // [END handleSignInResult]
//////////////////////////////////////////////////////////////////////////////////////////////////
    // [START signIn]
    private void signIn() {//method to intent to google log inpage
        // Add a call to initialize AWSMobileClient
       /* AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(LoginActivity.this, SignInUI.class);

                signin.login(LoginActivity.this, MainActivity.class).execute();
            }
        }).execute();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
        if(account!=null){

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
            if(acct != null){
                String personEmail = acct.getEmail(); //get email from the login
            }
        }*/

       Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);//start the onActivityForResult
    }
    // [END signIn]

    // [START signOut]
    public void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                       //updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.

       // Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }


    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }
////////////////////////////////////////////[onclick listener]/////////////////////////////////////////////////////////////
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                if(isOnline()==false){
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    //editor.putString("userToken-WeTrack", "");//reset usertoken on shared preference
                    editor.putString("userID-WeTrack", "");//reset userid on sharedpreference
                    editor.putString("userRole-WeTrack", "");//reset user role onn sharedpreference
                    editor.commit();
                    signOut();


                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                    alertDialog.setTitle("Login Failed");
                    alertDialog.setMessage("Please turn on internet connection");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                }
                            });
                    alertDialog.show();
                }
                else {
                    signIn();//use method above

                }

                break;
            case R.id.btnAnonymous:
                if(isOnline()==false){
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    //editor.putString("userToken-WeTrack", "");//reset sharedpreference
                    editor.putString("userID-WeTrack", "");
                    editor.putString("userRole-WeTrack", "");
                    editor.commit();
//                        signOut();

                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                    alertDialog.setTitle("Login Failed");
                    alertDialog.setMessage("Please turn on internet connection");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    CallCloudLogicAnonymousLogin();
                   // final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                   // final SharedPreferences.Editor editor = sharedPref.edit();// enable editing of shared preference

                    //JsonParser parser = new JsonParser();
                    //JsonObject obj = parser.parse("{\"user_id\": \"0\"}").getAsJsonObject();//get token as json object

                   // editor.putString("userRole-WeTrack", "5"); //put user role as 5;so MainActivity can detect it is anonymous signin
                    //editor.putString("userID-WeTrack", "0");
                  //  editor.commit();

                }

                              ///continue/...

                /*final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                final SharedPreferences.Editor editor = sharedPref.edit();// enable editing of shared preference

                String deviceToken = sharedPref.getString("deviceToken-WeTrack", ""); //get device token from sharepreference, if no device token, used ""
                JsonParser parser = new JsonParser();
                JsonObject obj = parser.parse("{\"token\": \"" + deviceToken + "\",\"user_id\": \"0\"}").getAsJsonObject();//get token as json object

                editor.putString("userRole-WeTrack", "5"); //put user role as 5;so MainActivity can detect it is anonymous signin
                editor.commit();

                serverAPI.sendToken(obj).enqueue(new Callback<UserAccount>() {
                    @Override
                    public void onResponse(Call<UserAccount> call, Response<UserAccount> response) {
                        userAccount = response.body();

                        if (userAccount != null) {
                            //below: save the userID in sharedpreference
                            editor.putString("userID-WeTrack", String.valueOf(userAccount.getId()));
                            //below: save the userToken in shared preference
                            editor.putString("userToken-WeTrack", userAccount.getToken());
                            //below: set email and name as anonymous and profile pic as nothing
                            EmailInfo account = new EmailInfo("Anonymous", "Anonymous", R.drawable.my_avt + "");
                            Gson gson2 = new Gson();
                            String jsonAccount = gson2.toJson(account);
                            editor.putString("userAccount-WeTrack", jsonAccount);
                            editor.commit();

                            Intent intent = new Intent(getBaseContext(), MainActivity.class);//proceed to MainACtivity
                            startActivity(intent);
                            finish();
                        }


                    }

                    @Override
                    public void onFailure(Call<UserAccount> call, Throwable t) {//when there is no internet connection
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("userToken-WeTrack", "");//reset sharedpreference
                        editor.putString("userID-WeTrack", "");
                        editor.putString("userRole-WeTrack", "");
                        editor.commit();
//                        signOut();

                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                        alertDialog.setTitle("Login Failed");
                        alertDialog.setMessage("Please turn on internet connection");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                });
                break;*/

        }
    }

    @Override
    public void onBackPressed() {//when click on the back button
       // SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //String userToken = sharedPref.getString("userToken-WeTrack", "");//reset token
        //if (!userToken.equals("")) {
            super.onBackPressed();//perform on back press operation
        //}
        finish();
    }
}