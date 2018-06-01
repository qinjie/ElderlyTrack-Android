package edu.np.ece.wetrack;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.api.idx2qiqap347.ElderlytrackClient;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.util.IOUtils;
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.np.ece.wetrack.api.RetrofitUtils;
import edu.np.ece.wetrack.api.ServerAPI;
import edu.np.ece.wetrack.model.BeaconInfo;
import edu.np.ece.wetrack.model.BeaconLocation;
import edu.np.ece.wetrack.model.Resident;
import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edu.np.ece.wetrack.tasks.SendNotificationTask.sendNotification;
import static edu.np.ece.wetrack.tasks.SendNotificationTask.sendNotificationForDetected;


/**
 * Created by hoanglong on 21-Dec-16.
 */

public class BeaconScanActivation extends Application implements BootstrapNotifier {
    private static final String TAG = ".MyApplicationName";
    private RegionBootstrap regionBootstrap;// set up background launching of an app when a user enters a beacon Region
    private BackgroundPowerSaver backgroundPowerSaver;
    private BeaconManager mBeaconmanager;//a class used for setting up of the beacons from a service/activity.->provide a callback when the BeaconService os ready to use

    private ServerAPI serverAPI;

    private ElderlytrackClient apiClient;
    private static final String LOG_TAG = BeaconScanActivation.class.getSimpleName();
    private Resident resident;

    public static List<Resident> patientList = new ArrayList<>();
    public static List<Resident> detectedPatientList = new ArrayList<>();
    public static List<BeaconInfo> detectedBeaconList = new ArrayList<>();

    Location mLocation;
    LocationManager locationManager;

    ArrayList<Region> regionList = new ArrayList();
    final BootstrapNotifier tmp = this;
    private Handler mHandler;
    MainActivity forDisplay = new MainActivity();

    public void CallCloudLogicMissingResident(){
        final String method = "GET";
        final String path = "/v1/resident/missing";

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

                       // final GsonBuilder gsonBuilder = new GsonBuilder();
                        //final Gson gson = gsonBuilder.create();

                        //resident = gson.fromJson(responseData, Resident.class);

                        JSONObject obj = new JSONObject(responseData);
                        JSONArray beacons = obj.getJSONArray("beacons");
                        JSONArray relatives = obj.getJSONArray("relatives");
                        JSONArray locations = obj.getJSONArray("locations");

                       // Gson g = new Gson();
                        //resident = g.fromJson(responseData, Resident.class);
                        //Gson g2 = new Gson();
                      //  String jsonPatients = resident.toJson(resident);
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                       // editor.putString("patientList-WeTrack", jsonPatients);//put patient list on shared preference
                      //  editor.commit();
                    }
                    Log.d(LOG_TAG, response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(LOG_TAG, exception.getMessage(), exception);
                    exception.printStackTrace();

                }
            }
        }).start();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AWSMobileClient.getInstance().initialize(this).execute();


        //below:initialise the list item for patient
        DrawerImageLoader.init(new AbstractDrawerImageLoader() { //DrawerImageLoader is the loader for patient profile image
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder, String tag) {//when setting image
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {//when nothing to on list to display
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {//for relatives blue means nearby, red means not nearby
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);//set blue icon
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);//set red icon
                }

                //we use the default one for
                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()

                return super.placeholder(ctx, tag);
            }
        });

        Fabric.with(this, new Crashlytics());


        mBeaconmanager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(getBaseContext());

        //Below: Detect the main identifier (UID) frame:
        mBeaconmanager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        mBeaconmanager.setBackgroundMode(true);//run beacon manager in background
        backgroundPowerSaver = new BackgroundPowerSaver(getBaseContext());

        mBeaconmanager.setBackgroundBetweenScanPeriod(25000l);
        mBeaconmanager.setBackgroundScanPeriod(20000l);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        serverAPI = RetrofitUtils.get().create(ServerAPI.class);

        // Create the client for aws api
        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance().getCredentialsProvider())
                .build(ElderlytrackClient.class);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String token = sharedPref.getString("userToken-WeTrack", "");
        CallCloudLogicMissingResident();
       /* serverAPI.getPatientList("Bearer " + token).enqueue(new Callback<List<Resident>>() {//get Patient List from server
            @Override
            public void onResponse(Call<List<Resident>> call, Response<List<Resident>> response) {
                try {
                    patientList = response.body();

                    Gson gson = new Gson();
                    String jsonPatients = gson.toJson(patientList);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("patientList-WeTrack", jsonPatients);//put patient list on shared preference
                    editor.commit();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<Resident>> call, Throwable t) {//no internet connection
                sendNotification(getBaseContext(), "Please turn on internet connection");
                Gson gson = new Gson();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String jsonPatients = sharedPref.getString("patientList-WeTrack", "");
                Type type = new TypeToken<List<Resident>>() {
                }.getType();
                patientList = gson.fromJson(jsonPatients, type);
            }
        });*/

        mHandler = new Handler();
        startRepeatingTask();
    }
    //method below triggered when detect beacon
    @Override
    public void didDetermineStateForRegion(int status, Region region) {

    }

//////////////////////////////////////////Enter Region////////////////////////////////////////
    @Override
    public void didEnterRegion(Region region) {//fired simultaneously with didExitRegion when the beacon is still in range and active

        //Clear offline list if user doesn't turn on internet connection after 31 days
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (sharedPref.getLong("ExpiredDate", -1) < System.currentTimeMillis()) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("listPatientsAndLocations-WeTrack2", "");
            editor.putLong("ExpiredDate", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(44640));
            editor.apply();
        }

        //Condition when there is no location permission granted
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);//Retrieve previously detected location

        //Setup to get current date
        Date aDate = new Date();
        SimpleDateFormat curFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateObj = curFormatter.format(aDate);

        //below: condition when there is patients on the list and there is location
        if (patientList != null && !patientList.equals("") && patientList.size() > 0 && mLocation != null) {

            String[] regionInfo = region.getUniqueId().split(";");//getting uniqueID of the beacon;Unique ID in 3 parts: uuid, major & minor

            Geocoder geocoder;//used for the transfering of a street address or other description into a location coordinate (i.e longitude & latitude)
            List<Address> addresses;
            geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            String fullAddress = "";

            for (final Resident patient : patientList) { //storing patientlist on to patient variable
                //for all the residents corresponding to the patientlist for the beacon
                for (final BeaconInfo aBeacon : patient.getBeacons()) {//storing beacon on to aBeacon variable
                    //for all the beacon info that corresponds to the patient that has gotten the beacon info

                    //below: condition to retrieve and check for the information inside the identifier and check if the patient info match the one on the server
                    if (regionInfo[0].equals(patient.getId() + "") && regionInfo[1].equals(aBeacon.getUuid().toLowerCase()) && regionInfo[2].equals(String.valueOf(aBeacon.getMajor())) && regionInfo[3].equals(String.valueOf(aBeacon.getMinor())) && region.getId2().toString().equals(String.valueOf(aBeacon.getMajor())) && patient.getStatus() == 1 && aBeacon.getStatus() == 1) {

                        if (!checkInternetOn()) {//when internet is off
                            String firstBeaconIdentifiers = regionInfo[1] + aBeacon.getMajor() + aBeacon.getMinor();// get beacon identifier: uuid, major and minor
                            SharedPreferences sharedPref2 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = sharedPref2.edit();
                            String oldData = sharedPref.getString("listPatientsAndLocations-WeTrack2", "");//get the previous patient location
                            //below: update and add new beacon identifier, longitude and latitude with the oldData on shared preference
                            editor.putString("listPatientsAndLocations-WeTrack2", firstBeaconIdentifiers + "," + mLocation.getLongitude() + "," + mLocation.getLatitude() + "," + dateObj + ";" + oldData);
                            editor.putLong("ExpiredDate", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(44640));
                            editor.commit();
                        }

                        String userID = sharedPref.getString("userID-WeTrack", "");//get the userID

                        if (!userID.equals("")) {//condition when there is userID present
                            try {
                                //get the longitude and latitude address of the the user
                                addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                                String address = addresses.get(0).getAddressLine(0); // If any additional address line present then only, check with max available address lines by getMaxAddressLineIndex()
                                String country = addresses.get(0).getCountryName();

                                fullAddress = address + ", " + country;//Address in the format of address,country
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //USing BeaconLocation class  to sort out the beaconid, user id, and location info
                            BeaconLocation aLocation = new BeaconLocation(aBeacon.getId(), Integer.parseInt(userID), mLocation.getLongitude(), mLocation.getLatitude(), dateObj, fullAddress);

                            Gson gson = new GsonBuilder()
                                    .setLenient()
                                    .create();
                            JsonObject obj = gson.toJsonTree(aLocation).getAsJsonObject();

                            String token = sharedPref.getString("userToken-WeTrack", "");//getting userToken

                            //below: api call
                            //below: send beacon location to server
                            Call<JsonObject> call = serverAPI.sendBeaconLocation("Bearer " + token, "application/json", obj);
                            call.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                                    try {
                                        //uses the below method to send notification to the phone
                                        sendNotificationForDetected(getBaseContext(), patient, "is nearby.");

                                        detectedPatientList.add(patient);//add patient to list
                                        detectedBeaconList.add(aBeacon);//add beacon info to list
                                        if (MainActivity.beaconListAdapter != null) {
                                            forDisplay.logToDisplay();//Make nearby beacon list become static on MainActivity

                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable error) {
                                }
                            });
                        }
                    }
                }
            }
        } else {
            if (mLocation == null) { //When there is no existing location
                sendNotification(getBaseContext(), "Please turn on location service");//send notification
            }
        }
    }
//////////////////////////////ENd of enter region method///////////////////////////////////
/////////////////////////////Start of exit Region///////////////////////////////////////////////
    @Override
    public void didExitRegion(Region region) {//(code in method similar to didEnterRegion)fire simultaneously with didEnterRegion method when the beacon is in range and active
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //not granted permission for location
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);//get the location

        //below: get date and time
        Date aDate = new Date();
        SimpleDateFormat curFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateObj = curFormatter.format(aDate);
////////////////////////////////////////////
        //below: condition when there is patients on the list and there is location (similar to Enter Region)
        if (patientList != null && !patientList.equals("") && patientList.size() > 0 && mLocation != null) {

            //below: BeaconID are split into uuid, major and minor; below is to get the beacon unique id in the region
            String[] regionInfo = region.getUniqueId().split(";"); // split the beacon id into separate strings i.e patientid, uuid, major,minor

            for (final Resident patient : patientList) {//storing patient list and all the beacon, location info from server into the variable patient
                //for all the residents corresponding to the patientlist for the beacon
                for (final BeaconInfo aBeacon : patient.getBeacons()) {//storing the beacon info to aBeacon variable
                    if (regionInfo[0].equals(patient.getId() + "") && regionInfo[1].equals(aBeacon.getUuid().toLowerCase()) && regionInfo[2].equals(String.valueOf(aBeacon.getMajor())) && regionInfo[3].equals(String.valueOf(aBeacon.getMinor())) && region.getId2().toString().equals(String.valueOf(aBeacon.getMajor())) && patient.getStatus() == 1 && aBeacon.getStatus() == 1) {

                        if (!checkInternetOn()) {//if no internet
                            String firstBeaconIdentifiers = regionInfo[1] + aBeacon.getMajor() + aBeacon.getMinor();
                            SharedPreferences sharedPref2 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = sharedPref2.edit();
                            String oldData = sharedPref.getString("listPatientsAndLocations-WeTrack2", "");
                            editor.putString("listPatientsAndLocations-WeTrack2", firstBeaconIdentifiers + "," + mLocation.getLongitude() + "," + mLocation.getLatitude() + "," + dateObj + ";" + oldData);
                            editor.putLong("ExpiredDate", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(44640));
                            editor.commit();
                        }

                        List<Resident> residentToRemove = new ArrayList<>();
                        List<BeaconInfo> beaconToRemove = new ArrayList<>();

                        for (Resident aResident : detectedPatientList) {
                            if (aResident.getId() == patient.getId()) {
                                residentToRemove.add(aResident);
                                break;
                            }
                        }

                        for (BeaconInfo removeBeacon : detectedBeaconList) {
                            if (removeBeacon.getId() == aBeacon.getId()) {
                                beaconToRemove.add(removeBeacon);
                                break;
                            }
                        }

                        detectedPatientList.removeAll(residentToRemove);
                        detectedBeaconList.removeAll(beaconToRemove);

                        residentToRemove.clear();
                        beaconToRemove.clear();

                        if (MainActivity.beaconListAdapter != null) {
                            forDisplay.logToDisplay();
                        }
                    }
                }
            }
        }
//////////////////////////////////////below: opposite of patientList != null && !patientList.equals("") && patientList.size() > 0 && mLocation != null
        else {
            if (mLocation == null) { //when there is no location service
                sendNotification(getBaseContext(), "Please turn on location service");
            }
        }
    }
////////////////////////////////////////Start of runnable/ handler
    //this will re-run after every 20 seconds
    private int mInterval = 20000;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String token = sharedPref.getString("userToken-WeTrack", ""); //obtain user token

            String isScanning = sharedPref.getString("isScanning-WeTrack", "true"); //check if settings scanning is on or off
            String isLogin = sharedPref.getString("userToken-WeTrack", "");
/////////////////////////////////////////////////////
            //condition when scanning settings is on and there is user token
            if (isScanning.equals("true") && !isLogin.equals("")) {
                //below: api call to get send resident to server
                //then retrieve on the app
                serverAPI.getPatientList("Bearer " + token).enqueue(new Callback<List<Resident>>() {
                    @Override
                    public void onResponse(Call<List<Resident>> call, Response<List<Resident>> response) {
                        try {
                            patientList = response.body();//get patientlist from the server

                            Gson gson = new Gson();
                            String jsonPatients = gson.toJson(patientList);//get updated patients
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("patientList-WeTrack", jsonPatients);
                            editor.commit();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Resident>> call, Throwable t) {

                        t.printStackTrace();
                        Gson gson = new Gson();
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        //below: get patients from sharedPreference (existing)
                        String jsonPatients = sharedPref.getString("patientList-WeTrack", "");
                        Type type = new TypeToken<List<Resident>>() {
                        }.getType();
                        patientList = gson.fromJson(jsonPatients, type);
                    }
                });
                //below: condition when there is patient on the list
                if (patientList != null && !patientList.equals("") && patientList.size() > 0 && tmp != null) {

                    //below: for the resident of the beacons in correspondence to the list obtained from the server
                    for (Resident aPatient : patientList) {

                        //below: for the BeaconInfo from the beacons in correspondence to the beacon of the patient obtained from the server
                        for (BeaconInfo aBeacon : aPatient.getBeacons()) {
                            String uuid = aBeacon.getUuid();//get uuid from th beacon
                            Identifier identifier = Identifier.parse(uuid);//get uuid identifier
                            Identifier identifier2 = Identifier.parse(String.valueOf(aBeacon.getMajor()));//get major
                            Identifier identifier3 = Identifier.parse(String.valueOf(aBeacon.getMinor()));//get minor
                            //below: create a new region object
                            Region region = new Region(aPatient.getId() + ";" + identifier + ";" + identifier2 + ";" + identifier3, identifier, identifier2, identifier3);

                            //when there is patient, beacon
                            if (aPatient.getStatus() == 1 && aBeacon.getStatus() == 1 && aPatient.getBeacons() != null && aPatient.getBeacons().size() > 0) {
                                if (!regionList.contains(region)) {//condition when there is no existing region
                                    regionList.add(region);//add region to the region list
                                }

                            } else {//arraylist below
                                List<Resident> residentToRemove = new ArrayList<>();
                                List<BeaconInfo> beaconToRemove = new ArrayList<>();

                                for (Resident aResident : detectedPatientList) {//for the residents of the beacon in correspondence to the detected patient
                                    if (aResident.getId() == aPatient.getId()) {//condition when they have the same id
                                        residentToRemove.add(aResident);
                                        break;
                                    }
                                }

                                for (BeaconInfo removeBeacon : detectedBeaconList) {//for the beaconinfo of the beacon in correspondence to the detected Beacon
                                    if (removeBeacon.getId() == aBeacon.getId()) {//condition when they have the same beacon id
                                        beaconToRemove.add(removeBeacon);
                                        break;
                                    }
                                }

                                detectedPatientList.removeAll(residentToRemove);//remove duplicated patients list from residentToRemove list
                                detectedBeaconList.removeAll(beaconToRemove);//remove duplicated beacon from list from beaconToRemove list

                                residentToRemove.clear();// remove all elements of an arraylist and set to null
                                beaconToRemove.clear();//remove all elements of an arrat lust and set to null

                                if (regionList.contains(region)) {//condition if there are existing region in the list
                                    regionList.remove(region);
                                    try {
                                        mBeaconmanager.stopMonitoringBeaconsInRegion(region);//stop monitoring the beacons in region
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }

                //allow user to set up background launching of an app when a user enter
                //a beacon region
                regionBootstrap = new RegionBootstrap(tmp, regionList);

                if (checkInternetOn()) {//when there is internet
                    SharedPreferences sharedPref3 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = sharedPref3.edit();
                    //below: obtain saved patient list and location and beacon identifier
                    String savedData = sharedPref3.getString("listPatientsAndLocations-WeTrack2", "");

                    Geocoder geocoder;// for the location in latitude and longitude
                    List<Address> addresses;
                    geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                    String fullAddress = "";

                    if (!savedData.equals("") && patientList != null) {//when there is saved data and existing patientList
                        final String[] patientOffline = savedData.split(";");//split the data when there is ";"
                        for (int i = 0; i < patientOffline.length; i++) {
                            String[] patientInfoOffline = patientOffline[i].split(",");//Saving patient info in arrays
                            if (savedData.contains(patientOffline[i] + ";")) {//condition when it is the last on the list
                                savedData = savedData.replace(patientOffline[i] + ";", "");//remove the ";"
                            }

                            if (patientList.size() > 0) {//when there is existing patients
                                //for the
                                for (final Resident patient : patientList) {//for all the residents corresponding to the patientlist on the server
                                    for (BeaconInfo aBeacon : patient.getBeacons()) {//for all the BeaconInfo that is in corresponding to the paptient beacon on server
                                        if (patient.getBeacons() != null && patient.getBeacons().size() > 0) {//condition when there is beacon and patient
                                            //below: get the identifier of the beacon
                                            String patientBeaconIdentifiers = aBeacon.getUuid() + aBeacon.getMajor() + aBeacon.getMinor();
                                            //below: When the offline patient matches the  beacon identities and becaon is in range with patient
                                            if (patientInfoOffline[0].equals(patientBeaconIdentifiers) && patient.getStatus() == 1 && aBeacon.getStatus() == 1) {
                                                String userID = sharedPref.getString("userID-WeTrack", "");//obtiain the userId
                                                if (!userID.equals("")) {//If userID is obtained,
                                                    try {
                                                        //below: get the location and retrieve the address
                                                        addresses = geocoder.getFromLocation(Double.parseDouble(patientInfoOffline[2]), Double.parseDouble(patientInfoOffline[1]), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                                        String city = addresses.get(0).getLocality();
                                                        fullAddress = address + ", " + city;

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    BeaconLocation aLocation = new BeaconLocation(aBeacon.getId(), Integer.parseInt(userID), Double.parseDouble(patientInfoOffline[1]), Double.parseDouble(patientInfoOffline[2]), patientInfoOffline[3], fullAddress);

                                                    Gson gson = new GsonBuilder()
                                                            .setLenient()
                                                            .create();
                                                    JsonObject obj = gson.toJsonTree(aLocation).getAsJsonObject();
                                                    //Send beacon location to the server
                                                    Call<JsonObject> call = serverAPI.sendBeaconLocation("Bearer " + token, "application/json", obj);
                                                    call.enqueue(new Callback<JsonObject>() {
                                                        @Override
                                                        public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                                                            try {

                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<JsonObject> call, Throwable error) {
                                                        }
                                                    });

                                                }

                                            }

                                        }
                                    }

                                }
                            }
                        }
                        editor.putString("listPatientsAndLocations-WeTrack2", savedData);//save data offline to sp
                        editor.commit();
                    }
                }

                if (MainActivity.beaconListAdapter != null) {
                    forDisplay.logToDisplay();
                }

            }
/////////////////////////////////////////////////////////////
            else {//condition when scanning settings is off or/and there is no user token
                if (regionList != null && regionList.size() > 0) {
                    for (Region tmp : regionList) {
                        try {
                            mBeaconmanager.stopMonitoringBeaconsInRegion(tmp);//stop monitoring of beacon
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            mHandler.postDelayed(mStatusChecker, mInterval);//Delay 20 seconds
        }
    };

    void startRepeatingTask() { //Repeat task
        mStatusChecker.run();
    }
/////////////////////////////////end of handler
    //Check the internet activity on or not
    //Return true if ON. False if OFF
    public boolean checkInternetOn() {//Method to check of internet connection
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null) {
            return false; // means no internet
        } else {
            return true;//means have internet
        }
    }
}