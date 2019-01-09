package edu.np.ece.elderlytrack;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;
import edu.np.ece.elderlytrack.api.ApiClient;
import edu.np.ece.elderlytrack.api.ApiGateway;
import edu.np.ece.elderlytrack.api.EventInProgress;
import edu.np.ece.elderlytrack.api.EventMissingCaseUpdate;
import edu.np.ece.elderlytrack.model.Missing;
import edu.np.ece.elderlytrack.model.ResidentWithMissing;

/**
 * A fragment representing a list of Items.
 */
public class RelativeDetailFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = RelativeDetailFragment.class.getSimpleName();
    private FragmentListener mListener;
    private FragmentManager fragmentManager;

    private final String STATUS_MISSING = "Missing";
    private final String STATUS_PRESENT = "Present";
    private boolean toBeRefreshed = false;
    ResidentWithMissing item;

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.ivAvatar)
    ImageView ivAvatar;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvEmail)
    TextView tvGenderAge;
    @BindView(R.id.tvReportedAt)
    TextView tvReportedAt;
    @BindView(R.id.tvRemark)
    TextView tvRemark;
    @BindView(R.id.tbStatus)
    TextView tvStatus;
    @BindView(R.id.switch1)
    Switch switch1;
    @BindView(R.id.tvMissingRemark)
    TextView tvMissingRemark;
    @BindView(R.id.tvLastSeenLocation)
    TextView tvLastSeenLocation;
    @BindView(R.id.tvLastSeenTime)
    TextView tvLastSeenTime;
    @BindView(R.id.btBeacons)
    Button btBeacons;
    @BindView(R.id.btTrail)
    Button btTrail;
    @BindView(R.id.swipe_container)
    SwipeRefreshLayout swipeLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RelativeDetailFragment() {
    }

    public static RelativeDetailFragment newInstance(ResidentWithMissing item) {
        RelativeDetailFragment fragment = new RelativeDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("item", item);
        fragment.setArguments(args);
        return fragment;
    }

    @OnClick(R.id.btBeacons)
    public void onClickBeacons(View view) {
        Fragment fragment = ResidentBeaconsFragment.newInstance(item.getId());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @OnClick(R.id.btTrail)
    public void onClickTrail(View view) {
        if (item.getActiveMissing() != null) {
            Fragment fragment = MissingLocationsFragment.newInstance(item.getActiveMissing().getId());
//            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(getContext(), "Resident currently not missing.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnCheckedChanged(R.id.switch1)
    public void OnCheckedChanged(CompoundButton view, boolean isChecked) {
        // Switch.OnCheckChanged() will automatically be called during form load
        // Use view.isPressed() to determine whether pressed by a user
        if (!view.isPressed())
            return;

        // To refresh UI when returned to this fragment
//        toBeRefreshed = true;

        Fragment fragment = null;
        if (isChecked) {
            fragment = ReportMissingFragment.newInstance(item.getId());
        } else {
            fragment = CloseMissingFragment.newInstance(item.getId());
        }
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        item = (ResidentWithMissing) this.getArguments().getSerializable("item");

        View view = inflater.inflate(R.layout.fragment_relative_detail, container, false);
        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);
        swipeLayout.setOnRefreshListener(this);
        updateUI();

        return view;
    }

    private void updateUI() {
        tvName.setText(item.getFullname());
        tvGenderAge.setText(item.getGenderAge());
        tvRemark.setText(item.getRemarkOrComment());
        Missing missing = item.getActiveMissing();
        tvStatus.setText(item.getStatusString());
        switch1.setChecked(missing != null);
        if (missing != null) {
            tvReportedAt.setText(missing.getReportedAtLocal(null));
            tvReportedAt.setVisibility(View.VISIBLE);
            tvMissingRemark.setText(missing.getRemarkOrComment());
            tvLastSeenTime.setText(missing.getUpdatedAtLocal(null));
            if (missing.getAddressHtml() != null) {
                tvLastSeenLocation.setText(Html.fromHtml(missing.getAddressHtml()));
                tvLastSeenLocation.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                tvLastSeenLocation.setText("not available");
            }
            btTrail.setEnabled(true);
        } else {
            tvReportedAt.setText("");
            tvMissingRemark.setText("");
            tvLastSeenLocation.setText("");
            tvLastSeenTime.setText("");
            btTrail.setEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Butter Knife unbind the view to free some memory
        unbinder.unbind();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
            fragmentManager = getActivity().getSupportFragmentManager();
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
        mListener.setActionBarTitle("Relative Details", true);
        if (this.toBeRefreshed) {
            // Refresh item value
            ApiGateway.apiGetResident(item.getId());
        }
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
        Log.d(TAG, "EventInProgress");
        progressBar.setVisibility(event.isInProgress() ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApiEventGetResident(ApiClient.ApiEventGetResident event) {
        Log.d(TAG, "ApiClient.ApiEventGetResident");
        if (event.isSuccessful()) {
            Toast.makeText(getContext(), "Get resident successful", Toast.LENGTH_SHORT).show();
            item = event.getResident();
            updateUI();
        }
        progressBar.setVisibility(View.GONE);
        if (swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(false);
        }
    }

    @Subscribe(sticky = true)
    public void onMissingCaseUpdate(EventMissingCaseUpdate event) {
        Log.d(TAG, "EventMissingCaseUpdate");
//        Toast.makeText(getContext(), "EventMissingCaseUpdate: " + event.getRemark(), Toast.LENGTH_SHORT).show();

        if (event.isMissing()) {
            switch1.setChecked(true);
            tvStatus.setText(STATUS_MISSING);
            tvStatus.setTextColor(Color.RED);
            tvMissingRemark.setText(event.getRemark());
        } else {
            switch1.setChecked(false);
            tvStatus.setText(STATUS_PRESENT);
            tvStatus.setTextColor(Color.BLACK);
            tvMissingRemark.setText("");
            tvLastSeenTime.setText("");
            tvLastSeenLocation.setText("");
        }
    }

    @Override
    public void onRefresh() {
        ApiGateway.apiGetResident(item.getId());
    }


//    private void apiGetResident() {
//        BeaconApplication application = mListener.getBaseApplication();
//        ApiInterface apiInterface = mListener.getApiInterface();
//        if (!application.isInternetConnected) {
//            Log.d(TAG, "No internet connection");
//            return;
//        }
//
//        AuthToken authoToken = application.getAuthToken(true);
//        if (authoToken == null || authoToken.getToken() == null) {
//            Log.e(TAG, "Token not found");
//            return;
//        }
//
//        String token = authoToken.getToken();
//        String contentType = "application/json";
//        EventBus.getDefault().post(new EventInProgress(true));
//        apiInterface.getResident(token, item.getId()).enqueue(new Callback<ResidentWithMissing>() {
//            @Override
//            public void onResponse(Call<ResidentWithMissing> call, Response<ResidentWithMissing> response) {
//                Log.d(TAG, call.request().toString());
//                int statusCode = response.code();
//                Log.d(TAG, response.toString());
//                if (response.isSuccessful()) {
//                    Toast.makeText(getContext(), "Get resident successful", Toast.LENGTH_SHORT).show();
//                    item = response.body();
//                    updateUI();
//                    EventBus.getDefault().post(new EventInProgress(false));
//                } else {
//                    Log.d(TAG, "Token expired.");
//                    Toast.makeText(getContext(), "Get resident unsuccessful. Status code = " + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
//                    EventBus.getDefault().post(new EventInProgress(false));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResidentWithMissing> call, Throwable t) {
//                Log.d(TAG, "API Error:" + t.getMessage());
//                Toast.makeText(getContext(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

}
