package edu.np.ece.wetrack;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import edu.np.ece.wetrack.api.ApiBeaconsOfMissingEvent;
import edu.np.ece.wetrack.api.ApiGateway;
import edu.np.ece.wetrack.api.InProgressEvent;
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

    // Beacons found nearby the phone
    Map<String, Beacon> nearbyBeaconMap = new HashMap<String, Beacon>();
    // Beacons of missing residents downloaded from server
    Map<String, NearbyItem> allMissingBeaconMap = new HashMap<String, NearbyItem>();
    // Filtered list to be displayed in ListView
    Map<String, NearbyItem> nearbyMissingBeaconMap = new TreeMap<String, NearbyItem>();

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

        ApiGateway.apiBeaconsOfMissing(getActivity());
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

        return view;
    }


    @Override
    public void onAttach(Context context) {
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
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInProgressEvent(InProgressEvent event) {
        progressBar.setVisibility(event.isInProgress() ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApiBeaconsOfMissingEvent(ApiBeaconsOfMissingEvent event) {
        List<NearbyItem> list = event.getMissingBeacons();
        for (NearbyItem b : list) {
            allMissingBeaconMap.put((b.getUuid() + "," + b.getMajor() + "," + b.getMinor()).toUpperCase(), b);
        }
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
                EventBus.getDefault().post(new InProgressEvent(true));
                // Get all nearby beacons
                nearbyBeaconMap.clear();
                for (Beacon beacon : beacons) {
                    Log.d(TAG, "Detect beacons: " + beacon.toString());
                    String key = (beacon.getId1().toString() + ',' + beacon.getId2().toString() + ',' + beacon.getId3().toString()).toUpperCase();
                    nearbyBeaconMap.put(key, beacon);
                }

                // Get nearby missing beacons
                nearbyMissingBeaconMap.clear();
                SortedSet<String> keys = new TreeSet<>(nearbyBeaconMap.keySet());
                for (String key1 : keys) {
                    NearbyItem ni = allMissingBeaconMap.get(key1);
                    if (ni != null) {
                        Log.d(TAG, "Matched beacons: " + key1);
                        nearbyMissingBeaconMap.put(key1, ni);
                    }
                }

                if (nearbyMissingBeaconMap.size() > 0) {
                    // Add all beacons to be reported
                    Log.d(TAG, "Pass beacons to Application");
                    // TODO Send to server
//                    mListener.getBaseApplication().appendNearbyMissingBeacons(nearbyMissingBeaconMap);
                }

                // Update listView
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Range nearby beacon list = " + nearbyBeaconMap.size());
                            mAdapter.updateItems(new ArrayList(nearbyMissingBeaconMap.values()));
                        }
                    });
                }
                EventBus.getDefault().post(new InProgressEvent(false));
            }
        });

        mListener.getBaseApplication().monitorRegions(false);
        mListener.getBaseApplication().rangeRegions(true);
        EventBus.getDefault().post(new InProgressEvent(true));
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
