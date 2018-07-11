package edu.np.ece.wetrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import edu.np.ece.wetrack.model.ResidentWithMissing;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment representing a list of Items.
 */
public class MissingFragment extends Fragment
        implements MissingRecyclerViewAdapter.OnItemClickedListener {
    private static final String TAG = MissingFragment.class.getSimpleName();
    private FragmentListener mListener;

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    ArrayList<ResidentWithMissing> residentList = new ArrayList<ResidentWithMissing>();
    MissingRecyclerViewAdapter mAdapter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MissingFragment() {
    }

    public static MissingFragment newInstance() {
        MissingFragment fragment = new MissingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void apiMissingResidents() {
        BeaconApplication application = mListener.getBaseApplication();
        ApiInterface apiInterface = mListener.getApiInterface();

        if (!application.hasInternetConnection) {
            Log.d(TAG, "No internet connection");
            return;
        }

        String token = application.getAuthToken(false).getToken();
        Log.d(TAG, "token = " + token);
        EventBus.getDefault().post(new InProgressEvent(true));
        apiInterface.listMissingResidents(token).enqueue(new Callback<List<ResidentWithMissing>>() {
            @Override
            public void onResponse(Call<List<ResidentWithMissing>> call, Response<List<ResidentWithMissing>> response) {
                Log.d(TAG, call.request().toString());
                Log.d(TAG, response.toString());

                if (response.isSuccessful()) {
                    Log.d(TAG, "Downloaded missing residents: " + response.body().toString());
                    residentList = new ArrayList<ResidentWithMissing>(response.body());
                    mAdapter.updateItems(residentList);
                } else {
                    if (response.code() == 401) {
                        Log.d(TAG, "Token expired.");
                        Toast.makeText(getActivity(), "Token expired.", Toast.LENGTH_SHORT).show();
                    }
                }
                EventBus.getDefault().post(new InProgressEvent(false));
            }

            @Override
            public void onFailure(Call<List<ResidentWithMissing>> call, Throwable t) {
                Log.d(TAG, "API Error apiMissingResidents():" + t.getMessage());
                Toast.makeText(getActivity(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                EventBus.getDefault().post(new InProgressEvent(false));
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_missing_list, container, false);

        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);

        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new MissingRecyclerViewAdapter(context, residentList, this);
        mRecyclerView.setAdapter(mAdapter);

        apiMissingResidents();
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
    public void onItemClick(ResidentWithMissing item) {
        Fragment fragment = MissingDetailFragment.newInstance(item);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        mListener.setActionBarTitle("Missing Cases", false);
    }

}
