package edu.np.ece.wetrack;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import edu.np.ece.wetrack.api.EventInProgress;

/**
 * A fragment representing a list of Items.
 */
public class BeaconFragment extends Fragment
        implements BeaconConsumer, BeaconRecyclerViewAdapter.OnItemClickedListener {

    private static final String TAG = BeaconFragment.class.getSimpleName();
    private FragmentListener mListener;

    // Beacon Manager
    private BeaconManager mBeaconManager;

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    BeaconRecyclerViewAdapter mAdapter;

    // Beacons found nearby the phone
    Map<String, Beacon> nearbyBeaconMap = new TreeMap<String, Beacon>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BeaconFragment() {
    }

    public static BeaconFragment newInstance() {
        Log.d(TAG, "newInstance()");
        BeaconFragment fragment = new BeaconFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() bind Beacon Manager");
        super.onCreate(savedInstanceState);
        mBeaconManager = BeaconManager.getInstanceForApplication(getActivity());
        mBeaconManager.bind(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() unbind Beacon Manager");
        super.onDestroy();
        mBeaconManager.unbind(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_beacon_list, container, false);

        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);

        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new BeaconRecyclerViewAdapter(context, new ArrayList<Beacon>(), this);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach()");
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach()");
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInProgressEvent(EventInProgress event) {
        progressBar.setVisibility(event.isInProgress() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Butter Knife unbind the view to free some memory
        unbinder.unbind();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        mBeaconManager.setBackgroundMode(true);
        mListener.getBaseApplication().monitorRegions(true);
        mListener.getBaseApplication().rangeRegions(false);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        mListener.setActionBarTitle("Nearby Beacons", false);

        mBeaconManager.setBackgroundMode(false);
        mListener.getBaseApplication().monitorRegions(false);
        mListener.getBaseApplication().rangeRegions(true);

        EventBus.getDefault().post(new EventInProgress(true));
    }

    @Override
    public void onItemClick(Beacon item) {

    }

    /*
     * Work with Altbeacon BeaconManager
     */
    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect()");
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                EventBus.getDefault().post(new EventInProgress(true));
                // Get all nearby beacons
                nearbyBeaconMap.clear();
                for (Beacon beacon : beacons) {
                    Log.d(TAG, "Detect beacons: " + beacon.toString());
                    String key = (beacon.getId1().toString() + ',' + beacon.getId2().toString() + ',' + beacon.getId3().toString()).toUpperCase();
                    nearbyBeaconMap.put(key, beacon);
                }

//                // Get nearby missing beacons
//                nearbyMissingBeaconMap.clear();
//                SortedSet<String> keys = new TreeSet<>(nearbyBeaconMap.keySet());
//                for (String key1 : keys) {
//                    NearbyItem ni = allMissingBeaconMap.get(key1);
//                    if (ni != null) {
//                        Log.d(TAG, "Matched beacons: " + key1);
//                        nearbyMissingBeaconMap.put(key1, ni);
//                    }
//                }
//
//                if (nearbyMissingBeaconMap.size() > 0) {
//                    // Add all beacons to be reported
//                    Log.d(TAG, "Pass beacons to Application");
//                    application.appendNearbyMissingBeacons(nearbyMissingBeaconMap);
//                }

                // Update listView
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Range nearby beacon list = " + nearbyBeaconMap.size());
                            mAdapter.updateItems(new ArrayList(nearbyBeaconMap.values()));
                        }
                    });
                }
                EventBus.getDefault().post(new EventInProgress(false));
            }
        });

        mListener.getBaseApplication().monitorRegions(false);
        mListener.getBaseApplication().rangeRegions(true);
    }

    @Override
    public Context getApplicationContext() {
        Log.d(TAG, "getApplicationContext()");
        return getActivity().getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        Log.d(TAG, "unbindService()");
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        Log.d(TAG, "bindService()");
        return getActivity().bindService(intent, serviceConnection, i);

    }

}
