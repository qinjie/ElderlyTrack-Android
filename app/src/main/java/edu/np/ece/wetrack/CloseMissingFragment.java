package edu.np.ece.wetrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import edu.np.ece.wetrack.api.ApiEventCloseMissing;
import edu.np.ece.wetrack.api.ApiGateway;
import edu.np.ece.wetrack.api.EventInProgress;

public class CloseMissingFragment extends Fragment {
    private static final String TAG = CloseMissingFragment.class.getSimpleName();
    private FragmentListener mListener;

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.etRemark)
    EditText etRemark;
    @BindView(R.id.btReport)
    Button btReport;

    int residentId;

    public CloseMissingFragment() {
        // Required empty public constructor
    }

    public static CloseMissingFragment newInstance(int resident_id) {
        CloseMissingFragment fragment = new CloseMissingFragment();
        Bundle args = new Bundle();
        args.putInt("RESIDENT_ID", resident_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report_missing, container, false);
        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);

        residentId = this.getArguments().getInt("RESIDENT_ID", -1);
        if (residentId < 0) {
            Toast.makeText(getActivity(), "Invalid resident ID.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid resident ID");
        }

        return view;
    }

    @OnClick(R.id.btReport)
    public void onClickLogin(View view) {

//        apiCloseMissing();
        //        obj.addProperty("resident_id", this.residentId);
//        obj.addProperty("closure", etRemark.getText().toString());
        ApiGateway.apiCloseMissing(this.residentId, etRemark.getText().toString());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // unbind the view to free some memory
        unbinder.unbind();
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
    public void onInProgressEvent(EventInProgress event) {
        progressBar.setVisibility(event.isInProgress() ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApiEventCloseMissing(ApiEventCloseMissing event) {
        Log.d(TAG, "onApiEventCloseMissing()");
        if (event.isSuccessful()) {
            Toast.makeText(getContext(), "Close missing successful", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }
    }

//    private void apiCloseMissing() {
//        BeaconApplication application = mListener.getBaseApplication();
//        ApiInterface apiInterface = mListener.getApiInterface();
//        if (!application.hasInternetConnection) {
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
////        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJxaW5qaWVAbnAuZWR1LnNnLDEwLDQxMyIsImlhdCI6MTUzMDA2NDExNiwibmJmIjoxNTMwMDY0MTE2LCJqdGkiOiJlMTk4ODg2Zi1mNTEzLTQwODMtOTlhNy01ZjA4NDM1NzZjMjcifQ.UspO1dABIo9c5ZsgtcEBheDuvQDgTqVbUvyfeatg-1w";
//        String token = authoToken.getToken();
//        String contentType = "application/json";
//
//        JsonObject obj = new JsonObject();
//        obj.addProperty("resident_id", this.residentId);
//        obj.addProperty("closure", etRemark.getText().toString());
//        obj.addProperty("closed_by", authoToken.getUser().getId());
//
//        Log.d(TAG, obj.toString());
//        EventBus.getDefault().post(new EventInProgress(true));
//        apiInterface.closeMissingCase(token, contentType, obj).enqueue(new Callback<List<Missing>>() {
//            @Override
//            public void onResponse(Call<List<Missing>> call, Response<List<Missing>> response) {
//                Log.d(TAG, call.request().toString());
//                int statusCode = response.code();
//                if (response.isSuccessful()) {
//                    Log.d(TAG, response.body().toString());
//                    Toast.makeText(getContext(), "Close missing successful", Toast.LENGTH_SHORT).show();
//                    EventBus.getDefault().post(new EventInProgress(false));
//                    // Go back to Resident Detail screen
//                    getActivity().onBackPressed();
//                } else {
//                    Log.d(TAG, response.message());
//                    Toast.makeText(getContext(), "Close missing unsuccessful. Status code = " + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
//                    EventBus.getDefault().post(new EventInProgress(false));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Missing>> call, Throwable t) {
//                Log.d(TAG, "API Error:" + t.getMessage());
//                Toast.makeText(getContext(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("Close Missing Case", true);
    }

}
