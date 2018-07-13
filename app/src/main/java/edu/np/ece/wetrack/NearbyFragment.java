package edu.np.ece.wetrack;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import edu.np.ece.wetrack.api.ApiEventListBeaconsOfMissing;
import edu.np.ece.wetrack.api.ApiGateway;
import edu.np.ece.wetrack.api.EventInProgress;
import edu.np.ece.wetrack.api.EventNearbyMissingBeaconsFound;
import edu.np.ece.wetrack.model.NearbyItem;

/**
 * A fragment representing a list of Items.
 */
public class NearbyFragment extends Fragment
        implements BeaconConsumer, NearbyRecyclerViewAdapter.OnItemClickedListener {

    private static final String TAG = NearbyFragment.class.getSimpleName();
    private FragmentListener mListener;

    // Beacon Manager
    private BeaconManager mBeaconManager;

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    NearbyRecyclerViewAdapter mAdapter;

    // Save regions detected for each monitoring regions
    Map<String, Map<String, Beacon>> nearbyBeaconsRegionMaps = new HashMap<>();
    // Beacons found nearby the phone
    Map<String, Beacon> nearbyBeaconMap = new HashMap<>();
    // Beacons of missing residents downloaded from server
    Map<String, NearbyItem> allMissingBeaconMap = new HashMap<>();
    // Filtered list to be displayed in ListView
    Map<String, NearbyItem> nearbyMissingBeaconMap = new TreeMap<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NearbyFragment() {
    }

    public static NearbyFragment newInstance() {
        NearbyFragment fragment = new NearbyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBeaconManager = BeaconManager.getInstanceForApplication(getActivity());
        mBeaconManager.bind(this);
        Log.d(TAG, "onCreate() bind Beacon Manager");

        ApiGateway.apiListBeaconsOfMissing();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBeaconManager.unbind(this);
        Log.d(TAG, "onDestroy() unbind Beacon Manager");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_nearby_list, container, false);

        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);

        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new NearbyRecyclerViewAdapter(context, new ArrayList<NearbyItem>(), this);
        mRecyclerView.setAdapter(mAdapter);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(getApplicationContext(), "Refreshing", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ApiGateway.apiListBeaconsOfMissing();
                    }
                }, 1000);
            }
        });
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

        allMissingBeaconMap = mListener.getBaseApplication().session.loadAllMissingBeaconMap();
        // Fetch latest copy at background
        ApiGateway.apiListBeaconsOfMissing();
        progressBar.setVisibility(View.VISIBLE);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApiBeaconsOfMissingEvent(ApiEventListBeaconsOfMissing event) {
        allMissingBeaconMap.clear();
        List<NearbyItem> list = event.getMissingBeacons();
        for (NearbyItem b : list) {
            allMissingBeaconMap.put((b.getUuid() + "," + b.getMajor() + "," + b.getMinor()).toUpperCase(), b);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Butter Knife unbind the view to free some memory
        unbinder.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.setBackgroundMode(true);
        mListener.getBaseApplication().monitorRegions(true);
        mListener.getBaseApplication().rangeRegions(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("Nearby Residents", false);

        mBeaconManager.setBackgroundMode(false);
        mListener.getBaseApplication().monitorRegions(false);
        mListener.getBaseApplication().rangeRegions(true);
    }

    @Override
    public void onItemClick(NearbyItem item) {

    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                EventBus.getDefault().post(new EventInProgress(true));
                // clear all beacons of this region
                if (nearbyBeaconsRegionMaps.get(region.getUniqueId()) == null) {
                    nearbyBeaconsRegionMaps.put(region.getUniqueId(), new HashMap<String, Beacon>());
                } else {
                    nearbyBeaconsRegionMaps.get(region.getUniqueId()).clear();
                }
                // Get all nearby beacons to map of respective region
                for (Beacon beacon : beacons) {
                    Log.d(TAG, "Detect beacons: " + beacon.toString());
                    String key = (beacon.getId1().toString() + ',' + beacon.getId2().toString() + ',' + beacon.getId3().toString()).toUpperCase();
                    nearbyBeaconsRegionMaps.get(region.getUniqueId()).put(key, beacon);
                }
                Log.d(TAG, "Detect beacons: size = " + nearbyBeaconsRegionMaps.size());

                // Transfer to nearbyBeaconMap
                nearbyBeaconMap.clear();
                for (Map<String, Beacon> map : nearbyBeaconsRegionMaps.values()) {
                    nearbyBeaconMap.putAll(map);
                }

                // Get nearby missing beacons
                nearbyMissingBeaconMap.clear();
                SortedSet<String> keys = new TreeSet<>(nearbyBeaconMap.keySet());
                for (String key1 : keys) {
                    NearbyItem ni = allMissingBeaconMap.get(key1);
                    if (ni != null) {
                        Log.d(TAG, "Matched beacons: " + key1);
                        ni.setDistance(nearbyBeaconMap.get(key1).getDistance());
                        nearbyMissingBeaconMap.put(key1, ni);
                    }
                }

                Log.d(TAG, "nearbyMissingBeaconMap size = " + nearbyMissingBeaconMap.size());
                // Update listView
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.updateItems(new ArrayList(nearbyMissingBeaconMap.values()));
                        }
                    });
                }

                Log.d(TAG, "new EventNearbyMissingBeaconsFound(): size = " + nearbyMissingBeaconMap.size());
                if (nearbyMissingBeaconMap.size() > 0) {
                    EventBus.getDefault().post(new EventNearbyMissingBeaconsFound(nearbyMissingBeaconMap));
                }
                EventBus.getDefault().post(new EventInProgress(false));
            }
        });

        mListener.getBaseApplication().monitorRegions(false);
        mListener.getBaseApplication().rangeRegions(true);
        EventBus.getDefault().post(new EventInProgress(true));
    }

    @Override
    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return getActivity().bindService(intent, serviceConnection, i);
    }

}
