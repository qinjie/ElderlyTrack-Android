package edu.np.ece.elderlytrack;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
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
import edu.np.ece.elderlytrack.api.ApiEventListRelativeResidents;
import edu.np.ece.elderlytrack.api.ApiGateway;
import edu.np.ece.elderlytrack.api.EventInProgress;
import edu.np.ece.elderlytrack.model.ResidentWithMissing;

/**
 */
public class RelativeFragment extends Fragment
        implements RelativeRecyclerViewAdapter.OnItemClickedListener {
    private static final String TAG = RelativeFragment.class.getSimpleName();
    private FragmentListener mListener;

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    ArrayList<ResidentWithMissing> residentList = new ArrayList<ResidentWithMissing>();
    RelativeRecyclerViewAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RelativeFragment() {
    }

    public static RelativeFragment newInstance() {
        RelativeFragment fragment = new RelativeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


//    private void apiRelativeResidents() {
//        BeaconApplication application = mListener.getBaseApplication();
//        ApiInterface apiInterface = mListener.getApiInterface();
//        if (!application.isInternetConnected) {
//            Log.d(TAG, "No internet connection");
//            return;
//        }
//        AuthToken authToken = application.getAuthToken(true);
//        if(authToken == null) {
//            Log.d(TAG, "Anonymous user");
//            return;
//        }
//
//        String token = authToken.getToken();
//        Log.d(TAG, "token = " + token);
//        EventBus.getDefault().post(new EventInProgress(true));
//        apiInterface.listRelativeResidents(token).enqueue(new Callback<List<ResidentWithMissing>>() {
//            @Override
//            public void onResponse(Call<List<ResidentWithMissing>> call, Response<List<ResidentWithMissing>> response) {
//                Log.d(TAG, call.request().toString());
//                Log.d(TAG, response.toString());
//
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "Downloaded relative residents: " + response.body().toString());
//                    residentList = new ArrayList<ResidentWithMissing>(response.body());
//                    mAdapter.updateItems(residentList);
//                } else {
//                    if (response.code() == 401) {
//                        Log.d(TAG, "Token expired.");
//                        Toast.makeText(getContext(), "Token expired.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                EventBus.getDefault().post(new EventInProgress(false));
//            }
//
//            @Override
//            public void onFailure(Call<List<ResidentWithMissing>> call, Throwable t) {
//                Log.d(TAG, "API Error apiMissingResidents():" + t.getMessage());
//                Toast.makeText(getContext(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                EventBus.getDefault().post(new EventInProgress(false));
//            }
//        });
//    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApiEventListRelativeResidents(ApiEventListRelativeResidents event) {
        Log.d(TAG, "onApiEventListRelativeResidents()");
        if (event.isSuccessful()) {
//            Toast.makeText(getContext(), "List relative resident successful", Toast.LENGTH_SHORT).show();
            residentList = new ArrayList<ResidentWithMissing>(event.getRelativeResidents());
            mAdapter.updateItems(residentList);
        }
        progressBar.setVisibility(View.GONE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_relative_list, container, false);

        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);
        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new RelativeRecyclerViewAdapter(context, residentList, this);
        mRecyclerView.setAdapter(mAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(mListener.getBaseApplication(), "Refreshing", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean result = ApiGateway.apiListRelativeResidents();
                        if (!result) {
                            EventBus.getDefault().post(new EventInProgress(false));
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        boolean result = ApiGateway.apiListRelativeResidents();
        if (!result) {
            progressBar.setVisibility(View.GONE);
        }
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
        Fragment fragment = RelativeDetailFragment.newInstance(item);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Butter Knife unbind the view to free some memory
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("Relatives", false);
    }
}
