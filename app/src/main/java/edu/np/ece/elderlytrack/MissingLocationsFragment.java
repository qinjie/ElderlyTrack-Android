package edu.np.ece.elderlytrack;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import edu.np.ece.elderlytrack.api.ApiEventListLocationByMissingId;
import edu.np.ece.elderlytrack.api.ApiGateway;
import edu.np.ece.elderlytrack.api.EventInProgress;
import edu.np.ece.elderlytrack.model.LocationWithBeacon;

/**
 * A fragment representing a list of Items.
 */
public class MissingLocationsFragment extends Fragment
        implements MissingLocationRecyclerViewAdapter.OnItemListener {
    private static final String TAG = MissingLocationsFragment.class.getSimpleName();
    private FragmentListener mListener;

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    MissingLocationRecyclerViewAdapter mAdapter;

    int missingId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MissingLocationsFragment() {
    }

    public static MissingLocationsFragment newInstance(int residentId) {
        MissingLocationsFragment fragment = new MissingLocationsFragment();
        Bundle args = new Bundle();
        args.putInt("ResidentId", residentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_missing_locations, container, false);

        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);

        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new MissingLocationRecyclerViewAdapter(context, new ArrayList<>(), this);
        mRecyclerView.setAdapter(mAdapter);

        Bundle bundle = this.getArguments();
        this.missingId = bundle.getInt("ResidentId");

        ApiGateway.apiListLocationByMissingId(missingId);
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
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("Trail Locations", true);
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
    public void onInProgressEvent(EventInProgress event) {
        progressBar.setVisibility(event.isInProgress() ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApiEventListLocationByMissingId(ApiEventListLocationByMissingId event) {
        Log.d(TAG, "onApiEventListLocationByMissingId()");
        mAdapter.updateItems(event.getLocationsWithBeacon());
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Butter Knife unbind the view to free some memory
        unbinder.unbind();
    }

    @Override
    public void onItemClicked(LocationWithBeacon item) {
        String uri = String.format("geo:0,0?q=%f,%f(%s)", item.getLatitude(), item.getLongitude(), item.getCreatedAtLocal(null));
        Uri gmmIntentUri = Uri.parse(uri);
        Log.d(TAG, uri);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(getActivity(), "Google map is not installed", Toast.LENGTH_SHORT).show();
        }
    }

//    public void apiListLocationByMissingId() {
//        BeaconApplication application = mListener.getBaseApplication();
//        ApiInterface apiInterface = mListener.getApiInterface();
//
//        if (!application.hasInternetConnection) {
//            Log.d(TAG, "No internet connection");
//            return;
//        }
//
//        String token = application.getAuthToken(false).getToken();
//        Log.d(TAG, "token = " + token);
//        EventBus.getDefault().post(new EventInProgress(true));
//        apiInterface.listLocationByMissingId(token, missingId).enqueue(new Callback<List<LocationWithBeacon>>() {
//            @Override
//            public void onResponse(Call<List<LocationWithBeacon>> call, Response<List<LocationWithBeacon>> response) {
//                Log.d(TAG, call.request().toString());
//                Log.d(TAG, "Status code = " + String.valueOf(response.code()));
//
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "Downloaded location for missing case: " + response.body().toString());
//                    locationList = new ArrayList<>(response.body());
//                    mAdapter.updateItems(locationList);
//                } else {
//                    if (response.code() == 401) {
//                        Log.d(TAG, "Token expired.");
//                        Toast.makeText(getContext(), "Token expired.", Toast.LENGTH_SHORT).show();
//                    }
//                    if (response.code() == 404) {
//                        Toast.makeText(getContext(), "No trail for this missing case.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                EventBus.getDefault().post(new EventInProgress(false));
//            }
//
//            @Override
//            public void onFailure(Call<List<LocationWithBeacon>> call, Throwable t) {
//                Log.d(TAG, "API Error:" + t.getMessage());
//                Toast.makeText(getContext(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//
//                EventBus.getDefault().post(new EventInProgress(false));
//            }
//        });
//    }
}
