package edu.np.ece.wetrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import edu.np.ece.wetrack.api.ApiEventLogin;
import edu.np.ece.wetrack.api.ApiGateway;
import edu.np.ece.wetrack.api.EventInProgress;

/**
 * A fragment representing a list of Items.
 */
public class LoginFragment extends Fragment {
    private static final String TAG = LoginFragment.class.getSimpleName();
    private FragmentListener mListener;

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.atvEmail)
    AutoCompleteTextView atvEmail;
    @BindView(R.id.etPassword)
    EditText password;
    @BindView(R.id.tvForgotPassword)
    TextView tvForgotPassword;
    @BindView(R.id.btSignIn)
    Button btSignIn;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LoginFragment() {
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);

        return view;
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
    public void onApiEventLoginWithEmail(ApiEventLogin event) {
        Log.d(TAG, "onApiEventLoginWithEmail()");
        if (event.isSuccessful()) {
            Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
            mListener.getBaseApplication().saveAuthToken(event.getAuthToken());
            getActivity().onBackPressed();
        }
    }

    @OnClick(R.id.btSignIn)
    public void onClickLogin(View view) {
//        apiLoginWithEmail();
        String email = atvEmail.getText().toString();
        String pwd = password.getText().toString();
        ApiGateway.apiLoginWithEmail(email, pwd);
    }

    @OnClick(R.id.tvForgotPassword)
    public void onClickForgotPassword(View view) {
        //TODO
//        apiForgotPassword();
    }


//    private void apiLoginWithEmail() {
//        BeaconApplication application = mListener.getBaseApplication();
//        ApiInterface apiInterface = mListener.getApiInterface();
//
//        if (!application.isInternetConnected) {
//            Log.d(TAG, "No internet connection");
//            return;
//        }
//
//        String email = atvEmail.getText().toString();
//        String pwd = password.getText().toString();
//
//        JsonObject obj = new JsonObject();
//        obj.addProperty("email", email);
//        obj.addProperty("password", pwd);
//
//        String contentType = "application/json";
//        EventBus.getDefault().post(new EventInProgress(true));
//        apiInterface.loginWithEmail(contentType, obj).enqueue(new Callback<AuthToken>() {
//            @Override
//            public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
//                Log.d(TAG, call.request().toString());
//                Log.d(TAG, response.toString());
//
//                if (response.isSuccessful()) {
//                    AuthToken authToken = response.body();
//                    Log.d(TAG, "Login successful: " + authToken.toString());
//                    application.session.saveAuthToken(authToken);
//                    application.getAuthToken(true);
//                    Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
//                    EventBus.getDefault().post(new EventInProgress(false));
//                    getActivity().onBackPressed();
//                } else {
//                    Log.d(TAG, "Token expired.");
//                    Toast.makeText(getContext(), "Login unsuccessful. Status code = " + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
//                    EventBus.getDefault().post(new EventInProgress(false));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<AuthToken> call, Throwable t) {
//                Log.d(TAG, "API Error:" + t.getMessage());
//                Toast.makeText(getContext(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void apiForgotPassword() {
//        BeaconApplication application = mListener.getBaseApplication();
//        ApiInterface apiInterface = mListener.getApiInterface();
//
//        if (!application.isInternetConnected) {
//            Log.d(TAG, "No internet connection");
//            return;
//        }
//
//        String email = atvEmail.getText().toString();
//        String pwd = password.getText().toString();
//
//        JsonObject obj = new JsonObject();
//        obj.addProperty("email", email);
//        obj.addProperty("password", pwd);
//
//        String contentType = "application/json";
//        EventBus.getDefault().post(new EventInProgress(true));
//        apiInterface.loginWithEmail(contentType, obj).enqueue(new Callback<AuthToken>() {
//            @Override
//            public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
//                Log.d(TAG, call.request().toString());
//                Log.d(TAG, response.toString());
//
//                if (response.isSuccessful()) {
//                    AuthToken authToken = response.body();
//                    Log.d(TAG, "Login successful: " + authToken.toString());
//                    application.session.saveAuthToken(authToken);
//                    application.getAuthToken(true);
//                    Toast.makeText(getContext(), "Reset code has been emailed to you.", Toast.LENGTH_SHORT).show();
//                    EventBus.getDefault().post(new EventInProgress(false));
//                } else {
//                    Log.d(TAG, "Token expired.");
//                    Toast.makeText(getContext(), "Reset password is unsuccessful. Status code = " + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
//                    EventBus.getDefault().post(new EventInProgress(false));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<AuthToken> call, Throwable t) {
//                Log.d(TAG, "Login API Error:" + t.getMessage());
//                Toast.makeText(getContext(), "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        mListener.setActionBarTitle("Login", true);
    }

}
