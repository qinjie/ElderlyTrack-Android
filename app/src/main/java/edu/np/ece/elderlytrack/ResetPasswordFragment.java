package edu.np.ece.elderlytrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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

public class ResetPasswordFragment extends Fragment {
    private static String TAG = ResetPasswordFragment.class.getSimpleName();

    // Butter Knife
    Unbinder unbinder;
    @BindView(R.id.etToken)
    EditText etToken;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.cbShow)
    CheckBox cbShow;
    @BindView(R.id.btSubmit)
    Button btSubmit;

    private static final String ARG_EMAIL = "ARG_EMAIL";

    private String email;

    private FragmentListener mListener;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    public static ResetPasswordFragment newInstance(String email) {
        ResetPasswordFragment fragment = new ResetPasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);
        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @OnCheckedChanged(R.id.cbShow)
    public void OnCheckedShowPassword(CompoundButton view) {
        if (cbShow.isChecked()) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
        }else{
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    @OnClick(R.id.btSubmit)
    public void onClickResetPassword(View view) {
        String token = etToken.getText().toString();
        String pwd = etPassword.getText().toString();

        if (TextUtils.isEmpty(token)) {
            Toast.makeText(mListener.getBaseApplication(), "Please enter token", Toast.LENGTH_SHORT).show();
            etToken.requestFocus();
        }
        if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(mListener.getBaseApplication(), "Please enter password", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
        }
        ApiGateway.apiResetPassword(email, token, pwd);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApiEventResetPassword(ApiClient.ApiEventResetPassword event) {
        Log.d(TAG, "onApiEventResetPassword()");
        if (event.isSuccessful()) {
            Toast.makeText(getContext(), "Reset password successful", Toast.LENGTH_SHORT).show();
            mListener.getBaseApplication().saveAuthToken(event.getAuthToken());
            getActivity().onBackPressed();
        } else {
            Toast.makeText(getContext(), "Reset password failed: " + event.getMessage(), Toast.LENGTH_SHORT).show();
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
                    + " must implement OnFragmentInteractionListener");
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
        MainActivity activity = (MainActivity) getActivity();
        mListener.setActionBarTitle("Set New Password", true);
    }
}
