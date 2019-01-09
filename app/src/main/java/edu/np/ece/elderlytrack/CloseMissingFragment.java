package edu.np.ece.elderlytrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import edu.np.ece.elderlytrack.api.ApiClient;
import edu.np.ece.elderlytrack.api.ApiGateway;
import edu.np.ece.elderlytrack.api.EventInProgress;
import edu.np.ece.elderlytrack.api.EventMissingCaseUpdate;
import edu.np.ece.elderlytrack.utils.Utils;

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
    public void onClickButtonReport(View view) {

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
    public void onApiEventCloseMissing(ApiClient.ApiEventCloseMissing event) {
        Log.d(TAG, "onApiEventCloseMissing()");
        if (event.isSuccessful()) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Close missing successful", Toast.LENGTH_SHORT).show();
            Utils.hideSoftKeyboard(activity);
            EventBus.getDefault().postSticky(new EventMissingCaseUpdate(false, etRemark.getText().toString()));
            activity.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("Close Missing Case", true);
        btReport.setText("Close Case");
    }

}
