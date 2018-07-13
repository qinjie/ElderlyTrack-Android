package edu.np.ece.wetrack;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.google.gson.JsonObject;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.np.ece.wetrack.api.ApiEventListBeaconsOfMissing;
import edu.np.ece.wetrack.api.ApiEventLogin;
import edu.np.ece.wetrack.api.ApiGateway;
import edu.np.ece.wetrack.api.BeaconLocationJob;
import edu.np.ece.wetrack.api.EventHasGpsLocation;
import edu.np.ece.wetrack.api.EventNearbyMissingBeaconsFound;
import edu.np.ece.wetrack.model.AuthToken;
import edu.np.ece.wetrack.model.NearbyItem;
import edu.np.ece.wetrack.receiver.NetworkChangedEvent;
import edu.np.ece.wetrack.utils.GeoCoding;
import edu.np.ece.wetrack.utils.UserSession;

public class BeaconApplication extends Application
        implements BootstrapNotifier, RangeNotifier, LocationListener {
    public static final String TAG = BeaconApplication.class.getCanonicalName();

    private static BeaconApplication instance;

    public static BeaconApplication getInstance() {
        return instance;
    }

    public BeaconApplication() {
        instance = this;
    }

    // Store nearby missing beacons before uploading to server
    public Map<String, NearbyItem> nearbyMissingBeaconMap = new HashMap<String, NearbyItem>();
    // Beacons of missing residents downloaded from server
    public Map<String, NearbyItem> allMissingBeaconMap = new HashMap<String, NearbyItem>();

    // For Android priority job queue
    public JobManager jobManager;

    // For saving of user token
    public UserSession session;

    // For BeaconManager
    BeaconManager mBeaconManager;
    RegionBootstrap regionBootstrap;
    BackgroundPowerSaver backgroundPowerSaver;

    // Global variables for user session
    public AuthToken authToken = null;

    public AuthToken getAuthToken(boolean reload) {
        if (authToken == null || reload) {
            authToken = session.loadAuthToken();
        }
        return authToken;
    }

    public void saveAuthToken(AuthToken authToken) {
        this.authToken = authToken;
        session.saveAuthToken(authToken);
    }

    // Global variable for system
    boolean isInternetConnected = true;

    public boolean isInternetConnected() {
        Log.d(TAG, "Internet connection: " + isInternetConnected);
        return isInternetConnected;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        // Setup BeaconManager
        try {
            initBeaconManager();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // Setup LocationManager
        initLocationManager();

        // Subscribe to EventBus
        EventBus.getDefault().register(this);

        // Schedule periodic job to download missing beacons
        runScheduleDownloadMissingBeacons();

        // Make sure JobManager is created
        configureJobManager();

        // For cache of user session
        session = new UserSession(this);

        // Fetch data from server
        ApiGateway.apiListBeaconsOfMissing();
    }

    public ArrayList<Region> getRegions() {
        Region region1 = new Region("ElderlyTrack-Estimote",
                Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
        Region region2 = new Region("ElderlyTrack-Police",
                Identifier.parse("FDA50693-A4E2-4FB1-AFCF-C6EB07647825"), null, null);
        Region region3 = new Region("ElderlyTrack-Sensoro",
                Identifier.parse("4F4C9C21-03F3-457A-8A7F-5E0D09401654"), null, null);
        ArrayList<Region> regions = new ArrayList<Region>();
        regions.add(region2);
        regions.add(region3);
        regions.add(region1);
        return regions;
    }

    public void monitorRegions(boolean start) {
        for (Region region : getRegions()) {
            try {
                if (start) {
                    mBeaconManager.startMonitoringBeaconsInRegion(region);
                } else {
                    mBeaconManager.stopMonitoringBeaconsInRegion(region);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void rangeRegions(boolean start) {
        for (Region region : getRegions()) {
            try {
                if (start) {
                    mBeaconManager.startRangingBeaconsInRegion(region);
                } else {
                    mBeaconManager.stopRangingBeaconsInRegion(region);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void initBeaconManager() throws RemoteException {
        // Setup beacon manager
//        BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);   //https://altbeacon.github.io/android-beacon-library/distance_vs_time.html
//        RunningAverageRssiFilter.setSampleExpirationMilliseconds(5000l);
//        RangedBeacon.setSampleExpirationMilliseconds(5000l);

        mBeaconManager = BeaconManager.getInstanceForApplication(getBaseContext());
        mBeaconManager.getBeaconParsers().add(
                new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        if (mBeaconManager.isBackgroundModeUninitialized()) {
            mBeaconManager.setBackgroundMode(true);
        }
        //When enabled, there will not be an "extra" region entry
        //     * event when the app starts up and a beacon for a monitored region was previously visible
        //     * within the past 15 minutes.
        mBeaconManager.setRegionStatePersistenceEnabled(false);
        mBeaconManager.setBackgroundScanPeriod(5000l);
        mBeaconManager.setBackgroundBetweenScanPeriod(5000l);
        mBeaconManager.setForegroundScanPeriod(5000l);
        mBeaconManager.setForegroundBetweenScanPeriod(5000l);

//        backgroundPowerSaver = new BackgroundPowerSaver(this);
        regionBootstrap = new RegionBootstrap(this, this.getRegions());
//        regionBootstrap = new RegionBootstrap(this, region1);
        Log.i(TAG, "initBeaconManager(): successful");
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.i(TAG, "BootstrapNotifier.didEnterRegion(): " + region.getUniqueId());

        try {
            Log.i(TAG, "Start ranging... ");
            mBeaconManager.addRangeNotifier(this);
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Failed to start ranging");
        }

//        // Start service to range beacon
//        Intent intent_Start = new Intent(this, MyBeaconRangeService.class);
//        Log.i(TAG, "Start service to range beacon");
//        startService(intent_Start);
    }

    @Override
    public void didExitRegion(Region region) {
        Log.i(TAG, "BootstrapNotifier.didExitRegion(): " + region.getUniqueId());
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.i(TAG, "BootstrapNotifier.didDetermineStateForRegion()");
        // Don't care
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
//        mBeaconManager.unbind(this);
        EventBus.getDefault().unregister(this);
    }

    // Setup EventBus

    @Subscribe(sticky = true)
    public void onNetworkChangedEvent(NetworkChangedEvent event) {
        isInternetConnected = event.isInternetConnected();
    }

    @Subscribe()
    public void onApiEventLogin(ApiEventLogin event) {
        this.authToken = event.getAuthToken();
        session.saveAuthToken(event.getAuthToken());
    }

    @Subscribe()
    public void onNearbyMissingBeaconsFoundEvent(EventNearbyMissingBeaconsFound event) {
        Map<String, NearbyItem> map = event.getNearbyMissingBeacons();
        Log.d(TAG, "Pending beacons New = " + nearbyMissingBeaconMap.size());
        // Add newly found beacons to map
        nearbyMissingBeaconMap.putAll(map);
        Log.d(TAG, "Pending beacons Total = " + nearbyMissingBeaconMap.size());
        // Get GPS value before processing beacons
        if (!isGettingGps) {
            requestLocation();
        }
    }


    // Setup Android priority job queue

    public synchronized JobManager getJobManager() {
        if (jobManager == null) {
            configureJobManager();
        }

        return jobManager;
    }

    private void configureJobManager() {
        Configuration.Builder builder = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JobManager";

                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }

                    @Override
                    public void v(String text, Object... args) {

                    }
                })
                .minConsumerCount(1) // always keep at least one consumer alive
                .maxConsumerCount(2) // up to 3 consumers at a time
                .loadFactor(2) //3 jobs per consumer
                .consumerKeepAlive(120); //wait 2 minute

        jobManager = new JobManager(builder.build());
    }

    private void runScheduleDownloadMissingBeacons() {
        int JOB_ID = 12345;
        ComponentName serviceName = new ComponentName(this, ScheduleDownloadMissingBeacons.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
//                .setMinimumLatency(5000)    // 任务最延迟时间为5s
//                .setOverrideDeadline(60000) // 任务deadline，当到期没达到指定条件也会开始执行
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING)// 需要满足网络条件，默认值NETWORK_TYPE_NONE
                .setPeriodic(AlarmManager.INTERVAL_HALF_HOUR) //循环执行，循环时长为一天（最小为15分钟）
                .setRequiresCharging(false)// 需要满足充电状态
                .setRequiresDeviceIdle(false)// 设备处于Idle(Doze)
                .setPersisted(true) //设备重启后是否继续执行
                .setBackoffCriteria(3000, JobInfo.BACKOFF_POLICY_LINEAR) //设置退避/重试策略
                .build();
        JobScheduler scheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "DownloadMissingBeacons Job scheduled successfully !");
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onApiBeaconsOfMissingEvent(ApiEventListBeaconsOfMissing event) {
        Log.i(TAG, "onApiBeaconsOfMissingEvent(): " + event.toString());
        List<NearbyItem> list = event.getMissingBeacons();
        for (NearbyItem b : list) {
            allMissingBeaconMap.put((b.getUuid() + "," + b.getMajor() + "," + b.getMinor()).toUpperCase(), b);
        }
        session.saveAllMissingBeaconMap(allMissingBeaconMap);
    }

    @Subscribe
    public void onEventHasGpsLocation(EventHasGpsLocation event) {
        Log.i(TAG, "onEvent(EventHasGpsLocation): " + event.toString());
        Log.i(TAG, "nearbyMissingBeaconMap size = " + nearbyMissingBeaconMap.size());
        for (Map.Entry<String, NearbyItem> entry : nearbyMissingBeaconMap.entrySet()) {
            NearbyItem item = entry.getValue();
            JsonObject obj = new JsonObject();
            obj.addProperty("beacon_id", item.getId());
            obj.addProperty("latitude", event.getLatitude());
            obj.addProperty("longitude", event.getLongitude());
            obj.addProperty("address", event.getAddress());
            jobManager.addJobInBackground(new BeaconLocationJob(obj));
            Log.i(TAG, "onEvent(EventHasGpsLocation): old size = " + nearbyMissingBeaconMap.size());
            nearbyMissingBeaconMap.remove(entry.getKey());
            Log.i(TAG, "onEvent(EventHasGpsLocation): new size = " + nearbyMissingBeaconMap.size());
        }
    }

//    @Override
//    public void onBeaconServiceConnect() {
//        Log.i(TAG, "BeaconConsumer.onBeaconServiceConnected()");
//
//        mBeaconManager.addMonitorNotifier(this);
//        try {
//            mBeaconManager.startMonitoringBeaconsInRegion(region);
//            if (mBeaconManager.isBackgroundModeUninitialized()) {
//                mBeaconManager.setBackgroundMode(true);
//            }
//        } catch (RemoteException e) {
//            LogManager.e(e, TAG, "Can't set up bootstrap regions");
//        }
//    }

    // +++++++++++++++++++
    // Work to get GPS Location
    // +++++++++++++++++++

    private LocationManager locationManager;
    private String bestLocationProvider;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 10; // 10 ms
    // Flag whether getting GPS is in progress
    boolean isGettingGps = false;

    private void initLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        bestLocationProvider = locationManager.getBestProvider(criteria, true);
        Log.d(TAG, "requestLocation() bestLocationProvider = " + bestLocationProvider);
    }

    public Location requestLocation() {
        Log.d(TAG, "requestLocation()");
        Location lastKnownLocation = null;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "requestLocation(): permission not granted");
                return null;
            }
            if (locationManager == null) {
                Log.d(TAG, "requestLocation(): locationManager = null");
                return null;
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this, Looper.getMainLooper());
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.d(TAG, "requestLocation() NETWORK_PROVIDER");
                isGettingGps = true;
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this, Looper.getMainLooper());
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d(TAG, "requestLocation() GPS_PROVIDER");
                isGettingGps = true;
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        Log.d(TAG, "requestLocation(): failed");
        return lastKnownLocation;
    }

//    private String getAddress(Location location) {
//        Log.d(TAG, "getAddress()");
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        String result = null;
//        try {
//            List<Address> addressList = geocoder.getFromLocation(
//                    location.getLatitude(), location.getLongitude(), 1);
//            Log.d(TAG, "getAddresses(): " + addressList.toString());
//            if (addressList != null && addressList.size() > 0) {
//                Address address = addressList.get(0);
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
//                    sb.append(address.getAddressLine(i));
//                }
//                if (address.getLocality() != null)
//                    sb.append(",").append(address.getLocality());
//                if (address.getPostalCode() != null)
//                    sb.append(",").append(address.getPostalCode()).append("\n");
//                if (address.getCountryName() != null)
//                    sb.append(",").append(address.getCountryName());
//                result = sb.toString();
//                return result;
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Unable connect to Geocoder", e);
//        }
//        return "";
//    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged() latitude = " + String.valueOf(location.getLatitude()));

        GeoCoding locationAddress = new GeoCoding();
        locationAddress.getAddressFromLocation(location.getLatitude(), location.getLongitude(),
                getApplicationContext(), new GeocoderHandler());
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String address = "";
            double latitude = 0f, longitude = 0f;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    address = bundle.getString("address");
                    latitude = bundle.getDouble("latitude");
                    longitude = bundle.getDouble("longitude");
                    break;
                default:
                    address = "";
                    latitude = 0f;
                    longitude = 0f;
            }

            isGettingGps = false;
            Log.i(TAG, "onLocationChanged() address = " + address);
            EventBus.getDefault().post(new EventHasGpsLocation(latitude, longitude, address));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.d(TAG, "didRangeBeaconsInRegion(): " + region.getUniqueId());
        if (beacons.size() <= 0) return;

        // Get all nearby beacons to map of respective region
        String key1 = "";
        for (Beacon beacon : beacons) {
            key1 = (beacon.getId1().toString() + ',' + beacon.getId2().toString() + ',' + beacon.getId3().toString()).toUpperCase();
            Log.d(TAG, "Detect beacons: " + key1);
            NearbyItem ni = allMissingBeaconMap.get(key1);
            if (ni != null) {
                Log.d(TAG, "Matched beacons: " + key1);
                nearbyMissingBeaconMap.put(key1, ni);
            }
        }
        if (!isGettingGps) {
            requestLocation();
        }

        // Stop ranging
        try {
            mBeaconManager.stopRangingBeaconsInRegion(region);
            mBeaconManager.removeRangeNotifier(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}