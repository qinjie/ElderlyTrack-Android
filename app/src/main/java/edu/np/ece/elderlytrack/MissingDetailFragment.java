package edu.np.ece.elderlytrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import edu.np.ece.elderlytrack.model.Missing;
import edu.np.ece.elderlytrack.model.ResidentWithMissing;

/**
 * A fragment representing a list of Items.
 */
public class MissingDetailFragment extends Fragment {
    private static final String TAG = "MissingDetailFragment";
    private FragmentListener mListener;

    ResidentWithMissing item;

    // Butter Knife
    Unbinder unbinder;

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
    @BindView(R.id.tvAboutApp)
    TextView tvLastSeen;
    @BindView(R.id.ivDirection)
    ImageView ivDirection;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MissingDetailFragment() {
    }

    public static MissingDetailFragment newInstance(ResidentWithMissing item) {
        MissingDetailFragment fragment = new MissingDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("item", item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        item = (ResidentWithMissing) this.getArguments().getSerializable("item");

        View view = inflater.inflate(R.layout.fragment_missing_detail, container, false);
        // bind view using butter knife
        unbinder = ButterKnife.bind(this, view);

//        tvName.setText(item.getFullname());
        tvGenderAge.setText(item.getGenderAge());
        Missing missing = item.getActiveMissing();
        if (missing != null) {
            tvReportedAt.setText(missing.getReportedAtLocal(null));
            tvRemark.setText(missing.getRemark());
            if (missing.getAddressHtml() != null) {
                tvLastSeen.setText(Html.fromHtml(missing.getAddressHtml()));
                tvLastSeen.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                tvLastSeen.setText("not available");
            }
        }
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
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("Missing Case Details", true);
    }

}
