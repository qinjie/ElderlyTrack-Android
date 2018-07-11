package edu.np.ece.wetrack;

import android.app.Application;
import android.os.RemoteException;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.np.ece.wetrack.model.AuthToken;
import edu.np.ece.wetrack.model.NearbyItem;
import edu.np.ece.wetrack.receiver.NetworkChangedEvent;
import edu.np.ece.wetrack.utils.UserSession;

public class BeaconApplication extends Application
        implements BootstrapNotifier {
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

    // For Android priority job queue
    public JobManager jobManager;

    // For saving of user token
    public UserSession session;

    // For BeaconManager
    RegionBootstrap regionBootstrap;
    //    BackgroundPowerSaver backgroundPowerSaver;

    BeaconManager mBeaconManager;

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
    boolean hasInternetConnection = true;

    public boolean hasInternetConnection() {
        return hasInternetConnection;
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

        // Subscribe to EventBus
        EventBus.getDefault().register(this);

        session = new UserSession(this);

        //Make sure JobManager is created
//        getJobManager();
    }

    private void requestLocationUpdate() {
        Log.d(TAG, "requestLocationUpdate()");

        //TODO
//        Intent intent_Start = new Intent(this, MyLocationService.class);
//        Log.i(TAG, "Start service to get GPS location");
//        startService(intent_Start);
    }

    public ArrayList<Region> getRegions() {
        Region region1 = new Region("ElderlyTrackEstimote",
                Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
        Region region2 = new Region("ElderlyTrackPolice",
                Identifier.parse("FDA50693-A4E2-4FB1-AFCF-C6EB07647825"), null, null);
        Region region3 = new Region("ElderlyTrackSensoro",
                Identifier.parse("4F4C9C21-03F3-457A-8A7F-5E0D09401654"), null, null);
        ArrayList<Region> regions = new ArrayList<Region>();
        regions.add(region1);
        regions.add(region2);
        regions.add(region3);
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
//        RunningAverageRssiFilter.setSampleExpirationMilliseconds(2000l);
//        RangedBeacon.setSampleExpirationMilliseconds(2000l);

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
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
        mBeaconManager.setForegroundBetweenScanPeriod(2000l);
//        mBeaconManager.bind(this);

        regionBootstrap = new RegionBootstrap(this, this.getRegions());
//        regionBootstrap = new RegionBootstrap(this, region1);
//        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.i(TAG, "BootstrapNotifier.didEnterRegion(): " + region.getUniqueId());

//        try {
//            Log.i(TAG, "Start ranging... ");
//            mBeaconManager.startRangingBeaconsInRegion(region);
//        } catch (RemoteException e) {
//            if (BuildConfig.DEBUG) Log.d(TAG, "Failed to start ranging");
//        }

        //TODO
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

    // Setup EventBus
    @Override
    public void onTerminate() {
        super.onTerminate();
//        mBeaconManager.unbind(this);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true)
    public void onEvent(NetworkChangedEvent event) {
        hasInternetConnection = event.isInternetConnected();
    }

//    // Setup Android priority job queue
//
//    public synchronized JobManager getJobManager() {
//        if (jobManager == null) {
//            configureJobManager();
//        }
//
//        return jobManager;
//    }
//
//    private void configureJobManager() {
//        Configuration.Builder builder = new Configuration.Builder(this)
//                .customLogger(new CustomLogger() {
//                    private static final String TAG = "JobManager";
//
//                    @Override
//                    public boolean isDebugEnabled() {
//                        return true;
//                    }
//
//                    @Override
//                    public void d(String text, Object... args) {
//                        Log.d(TAG, String.format(text, args));
//                    }
//
//                    @Override
//                    public void e(Throwable t, String text, Object... args) {
//                        Log.e(TAG, String.format(text, args), t);
//                    }
//
//                    @Override
//                    public void e(String text, Object... args) {
//                        Log.e(TAG, String.format(text, args));
//                    }
//
//                    @Override
//                    public void v(String text, Object... args) {
//
//                    }
//                })
//                .minConsumerCount(1)//always keep at least one consumer alive
//                .maxConsumerCount(3)//up to 3 consumers at a time
//                .loadFactor(3)//3 jobs per consumer
//                .consumerKeepAlive(120);//wait 2 minute
//
//        jobManager = new JobManager(builder.build());
//    }
//
//    // Handles nearby missing beacons
//    public void appendNearbyMissingBeacons(Map<String, NearbyItem> map) {
//        this.nearbyMissingBeaconMap.putAll(map);
//        // Get current location before submit to server
//        this.requestLocationUpdate();
//    }
//
//    @Subscribe
//    public void onEvent(GpsLocationEvent event) {
//        Log.i(TAG, "onEvent(GpsLocationEvent): " + event.toString());
//
//        for (Map.Entry<String, NearbyItem> entry : nearbyMissingBeaconMap.entrySet()) {
//            NearbyItem item = entry.getValue();
//            JsonObject obj = new JsonObject();
//            obj.addProperty("beacon_id", item.getId());
//            obj.addProperty("latitude", event.getLatitude());
//            obj.addProperty("longitude", event.getLongitude());
//            obj.addProperty("address", event.getAddress());
//            jobManager.addJobInBackground(new BeaconLocationJob(getInstance(), obj));
//            nearbyMissingBeaconMap.remove(entry.getKey());
//        }
//
//    }

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
}