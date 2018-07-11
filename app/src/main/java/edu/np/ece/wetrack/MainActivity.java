package edu.np.ece.wetrack;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import org.altbeacon.beacon.BeaconManager;

import edu.np.ece.wetrack.api.ApiClient;
import edu.np.ece.wetrack.api.ApiGateway;
import edu.np.ece.wetrack.api.ApiInterface;

public class MainActivity extends AppCompatActivity
        implements FragmentListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BeaconApplication application;
    ApiInterface mApiInterface;

//    // Beacon Manager
//    private BeaconManager mBeaconManager;
//
//    // Beacons found nearby the phone
//    Map<String, Beacon> nearbyBeaconMap = new HashMap<String, Beacon>();
//    // Beacons of missing residents downloaded from server
//    Map<String, NearbyItem> allMissingBeaconMap = new HashMap<String, NearbyItem>();
//    // Filtered list to be displayed in ListView
//    Map<String, NearbyItem> nearbyMissingBeaconMap = new HashMap<String, NearbyItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = this.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_relative);

//        //Manually displaying the first fragment - one time only
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.frame_layout, NearbyFragment.newInstance());
////        transaction.replace(R.id.frame_layout, BeaconFragment.newInstance());
//        transaction.commit();

        // Get a reference to Application for global variables
        application = BeaconApplication.getInstance();
        // Build API client
        mApiInterface = ApiClient.getApiInterface();

        // Permission request for Location to use BLE
        requestPermissions();
        // Make sure Bluetooth is ON
        verifyBluetooth();

        if (application.getAuthToken(true) == null) {
            // Login to server as anonymous user
            ApiGateway.apiLoginAnonymous(this);
        }

    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        EventBus.getDefault().unregister(this);
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        mBeaconManager.setBackgroundMode(true);
//        try {
//            mBeaconManager.stopRangingBeaconsInRegion(application.getBeaconRegion());
//            mBeaconManager.startMonitoringBeaconsInRegion(application.getBeaconRegion());
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mBeaconManager.setBackgroundMode(false);
//        try {
//            mBeaconManager.stopMonitoringBeaconsInRegion(application.getBeaconRegion());
//            mBeaconManager.startRangingBeaconsInRegion(application.getBeaconRegion());
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }

    /*
     * Request permission to use BLE
     */
    public static final int PERMISSION_REQUEST_LOCATION = 999;

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    //Implement Fragment Listener

    public ApiInterface getApiInterface() {
        return this.mApiInterface;
    }

    public BeaconApplication getBaseApplication() {
        return this.application;
    }

    public void setActionBarTitle(String title, boolean showBackArrow) {
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayShowHomeEnabled(showBackArrow);
            actionBar.setDisplayHomeAsUpEnabled(showBackArrow);
        }
    }

//    /*
//     * Work with Altbeacon BeaconManager
//     */
//    @Override
//    public void onBeaconServiceConnect() {
//        mBeaconManager.addRangeNotifier(new RangeNotifier() {
//            @Override
//            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
//                // Get all nearby beacons
//                nearbyBeaconMap.clear();
//                for (Beacon beacon : beacons) {
//                    Log.d(TAG, "Detect beacons: " + beacon.toString());
//                    String key = (beacon.getId1().toString() + ',' + beacon.getId2().toString() + ',' + beacon.getId3().toString()).toUpperCase();
//                    nearbyBeaconMap.put(key, beacon);
//                }
////                // Get nearby missing beacons
////                nearbyMissingBeaconMap.clear();
////                SortedSet<String> keys = new TreeSet<>(nearbyBeaconMap.keySet());
////                for (String key1 : keys) {
////                    NearbyItem ni = allMissingBeaconMap.get(key1);
////                    if (ni != null) {
////                        Log.d(TAG, "Matched beacons: " + key1);
////                        nearbyMissingBeaconMap.put(key1, ni);
////                    }
////                }
////
////                if (nearbyMissingBeaconMap.size() > 0) {
////                    // Add all beacons to be reported
////                    Log.d(TAG, "Pass beacons to Application");
////                    application.appendNearbyMissingBeacons(nearbyMissingBeaconMap);
////                }
////                // Update listView
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        Log.d(TAG, "Refresh nearby residents' beacon list = " + nearbyMissingBeaconMap.size());
////                        mAdapter.updateItems(new ArrayList(nearbyMissingBeaconMap.values()));
////                    }
////                });
//            }
//        });
//
//        try {
//            mBeaconManager.startRangingBeaconsInRegion(application.getBeaconRegion());
//        } catch (RemoteException e) {
//            Log.e(TAG, e.getMessage());
//            e.printStackTrace();
//        }
//    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_beacons:
                    selectedFragment = BeaconFragment.newInstance();
                    break;
                case R.id.navigation_nearby:
                    selectedFragment = NearbyFragment.newInstance();
                    break;
                case R.id.navigation_missing:
                    selectedFragment = MissingFragment.newInstance();
                    break;
                case R.id.navigation_relative:
                    selectedFragment = RelativeFragment.newInstance();
                    break;
                case R.id.navigation_setting:
                    selectedFragment = SettingFragment.newInstance();
                    break;
                default:
                    selectedFragment = MissingFragment.newInstance();
                    break;
            }
            // Manually push in first fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, selectedFragment);
            transaction.commit();
            return true;
        }
    };

    /**
     * check for bluetooth enabled or not
     */
    private static final int REQUEST_ENABLE_BT = 110;

    private void verifyBluetooth() {
        Log.d(TAG, "Verify Bluetooth");
        try {
            BeaconManager mBeaconManager = BeaconManager.getInstanceForApplication(this);
            if (!mBeaconManager.checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth Must be Enabled.");
                builder.setMessage("Pls enable bluetooth to detect beacons.");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        //finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setTitle("Bluetooth Not Available");
            builder.setMessage("Bluetooth is not supported on this device.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    //finish();
                }
            });
            builder.show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                verifyBluetooth();
            } else {
                final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                //finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
