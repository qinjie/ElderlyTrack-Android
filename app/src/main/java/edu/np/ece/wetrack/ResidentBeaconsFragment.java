package edu.np.ece.wetrack;

import android.content.Context;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import edu.np.ece.wetrack.api.ApiInterface;
import edu.np.ece.wetrack.api.InProgressEvent;
import edu.np.ece.wetrack.model.BeaconProfile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment representing a list of Items.
 */
public class ResidentBeaconsFragment extends Fragment
        implements ResidentBeaconRecyclerViewAdapter.OnItemListener {
    private static final String TAG = ResidentBeaconsFragment.class.getSimpleName();
    private FragmentListener mListener;

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    ArrayList<BeaconProfile> beaconProfileList = new ArrayList<BeaconProfile>();
    ResidentBeaconRecyclerViewAdapter mAdapter;

    int residentId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ResidentBeaconsFragment() {
    }

    public static ResidentBeaconsFragment newInstance(int residentId) {
        ResidentBeaconsFragment fragment = new ResidentBeaconsFragment();
        Bundle args = new Bundle();
        args.putInt("ResidentId", residentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_resident_beacons, container, false);

        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);

        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new ResidentBeaconRecyclerViewAdapter(context, beaconProfileList, this);
        mRecyclerView.setAdapter(mAdapter);

        Bundle bundle = this.getArguments();
        this.residentId = bundle.getInt("ResidentId");

        apiListBeaconsByResidentId();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Butter Knife unbind the view to free some memory
        unbinder.unbind();
    }

    @Override
    public void onToggleButtonChecked(int beaconId, int status, int index) {
        apiUpdateBeaconStatus(beaconId, status, index);
    }

    public void apiListBeaconsByResidentId() {
        BeaconApplication application = mListener.getBaseApplication();
        ApiInterface apiInterface = mListener.getApiInterface();

        if (!application.hasInternetConnection) {
            Log.d(TAG, "No internet connection");
            return;
        }

        String token = application.getAuthToken(false).getToken();
        Log.d(TAG, "token = " + token);
        EventBus.getDefault().post(new InProgressEvent(true));
        apiInterface.listBeaconsByResidentId(token, residentId).enqueue(new Callback<List<BeaconProfile>>() {
            @Override
            public void onResponse(Call<List<BeaconProfile>> call, Response<List<BeaconProfile>> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, "Status code = " + String.valueOf(response.code()));

                if (response.isSuccessful()) {
                    Log.d(TAG, "Downloaded beacon for resident: " + response.body().toString());
                    beaconProfileList = new ArrayList<BeaconProfile>(response.body());
                    mAdapter.updateItems(beaconProfileList);
                } else {
                    if (response.code() == 401) {
                        Log.d(TAG, "Token expired.");
                        Toast.makeText(getContext(), "Token expired.", Toast.LENGTH_SHORT).show();
                    }
                    if (response.code() == 404) {
                        Toast.makeText(getContext(), "No beacon for this resident.", Toast.LENGTH_SHORT).show();
                    }
                }
                EventBus.getDefault().post(new InProgressEvent(false));
            }

            @Override
            public void onFailure(Call<List<BeaconProfile>> call, Throwable t) {
                Log.d(TAG, "API Error:" + t.getMessage());
                Toast.makeText(getContext(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                EventBus.getDefault().post(new InProgressEvent(false));
            }
        });
    }

    public void apiUpdateBeaconStatus(int beaconId, int status, int position) {
        BeaconApplication application = mListener.getBaseApplication();
        ApiInterface apiInterface = mListener.getApiInterface();

        if (!application.hasInternetConnection) {
            Log.d(TAG, "No internet connection");
            return;
        }

        String token = application.getAuthToken(false).getToken();
        Log.d(TAG, "token = " + token);
        EventBus.getDefault().post(new InProgressEvent(true));
        apiInterface.updateBeaconStatus(token, beaconId, status).enqueue(new Callback<BeaconProfile>() {
            @Override
            public void onResponse(Call<BeaconProfile> call, Response<BeaconProfile> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, "Status code = " + String.valueOf(response.code()));

                if (response.isSuccessful()) {
                    Log.d(TAG, "Update beaon status: " + response.body().toString());
                    BeaconProfile updated = response.body();
                    mAdapter.updateItem(updated, position);
                } else {
                    if (response.code() == 401) {
                        Log.d(TAG, "Token expired.");
                        Toast.makeText(getContext(), "Token expired.", Toast.LENGTH_SHORT).show();
                    }
                    if (response.code() == 404) {
                        Toast.makeText(getContext(), "Beacon not found.", Toast.LENGTH_SHORT).show();
                    }
                }
                EventBus.getDefault().post(new InProgressEvent(false));
            }

            @Override
            public void onFailure(Call<BeaconProfile> call, Throwable t) {
                Log.d(TAG, "API Error:" + t.getMessage());
                Toast.makeText(getContext(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                EventBus.getDefault().post(new InProgressEvent(false));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("Registered Beacons", true);
    }
}
