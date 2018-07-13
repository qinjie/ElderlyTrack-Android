package edu.np.ece.elderlytrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import edu.np.ece.elderlytrack.api.ApiEventLogin;
import edu.np.ece.elderlytrack.api.ApiGateway;
import edu.np.ece.elderlytrack.api.ApiInterface;
import edu.np.ece.elderlytrack.api.EventInProgress;
import edu.np.ece.elderlytrack.model.AuthToken;
import edu.np.ece.elderlytrack.model.User;

public class SettingFragment extends Fragment {
    private static final String TAG = SettingFragment.class.getSimpleName();
    private FragmentListener mListener;

    Unbinder unbinder;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.ivAvatar)
    ImageView ivAvatar;
    @BindView(R.id.tvReportedAt)
    TextView tvName;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvRemark)
    TextView tvPhone;
    @BindView(R.id.tvAboutUs)
    TextView tvAboutUs;
    @BindView(R.id.tvAboutApp)
    TextView tvAboutApp;
    @BindView(R.id.tvMissingRemark)
    TextView tvFaq;
    @BindView(R.id.btLogin)
    Button btLogin;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
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
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);
        // Update UI components
        updateUI();

        return view;
    }

    private void updateUI() {
        tvName.setText("Anonymous");
        tvEmail.setText("");
        tvPhone.setText("");
        btLogin.setText("Login");

        BeaconApplication application = mListener.getBaseApplication();
        AuthToken authoToken = application.getAuthToken(true);
        if (authoToken == null) {
            Log.d(TAG, "AutoToken not available. Login as anonymous first");
            return;
        }
        User user = authoToken.getUser();
        if (user != null) {
            tvName.setText(user.getFullnameAndUsername());
            tvEmail.setText(user.getEmail());
            tvPhone.setText(user.getUserProfile().getPhone());
            btLogin.setText("Logout");
        }
    }

//    @OnClick(R.id.tvAboutUs)
//    public void onClickAboutUs(View view) {
//        Fragment fragment = AboutUsFragment.newInstance();
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.frame_layout, fragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
//    }
//
//    @OnClick(R.id.tvAboutApp)
//    public void onClickAboutApp(View view) {
//        Fragment fragment = AboutAppFragment.newInstance();
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.frame_layout, fragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
//    }

    @OnClick(R.id.tvMissingRemark)
    public void onClickFaq(View view) {
        Fragment fragment = FaqFragment.newInstance("");
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @OnClick(R.id.btLogin)
    public void onClickLogin(View view) {
        BeaconApplication application = mListener.getBaseApplication();
        AuthToken authoToken = application.getAuthToken(true);

        if (authoToken.getUser() != null) {
            //Logout
            application.session.saveAuthToken(null);
            application.getAuthToken(true);
            apiLoginAnonymous();
            updateUI();
        } else {
            // Go to Login screen
            Fragment fragment = LoginFragment.newInstance();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
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

    private void apiLoginAnonymous() {
        BeaconApplication application = mListener.getBaseApplication();
        ApiInterface apiInterface = mListener.getApiInterface();

        if (!application.isInternetConnected) {
            Log.d(TAG, "No internet connection");
            return;
        }
        EventBus.getDefault().post(new EventInProgress(true));
        ApiGateway.apiLoginAnonymous();
    }

    @Subscribe()
    public void onApiEventLogin(ApiEventLogin event) {
        EventBus.getDefault().post(new EventInProgress(false));
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
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("Settings", false);
    }

}
