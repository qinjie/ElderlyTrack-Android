package edu.np.ece.elderlytrack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.np.ece.elderlytrack.model.BeaconProfile;

public class ResidentBeaconRecyclerViewAdapter extends RecyclerView.Adapter<ResidentBeaconRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = ResidentBeaconRecyclerViewAdapter.class.getSimpleName();
    private List<BeaconProfile> mItems;
    private Context mContext;
    private OnItemListener mItemListener;

    public ResidentBeaconRecyclerViewAdapter(Context context, List<BeaconProfile> data, OnItemListener listener) {
        mContext = context;
        mItems = data;
        mItemListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvLabel)
        TextView tvLabel;
        @BindView(R.id.tvUuid)
        TextView tvUuid;
        @BindView(R.id.tvMajor)
        TextView tvMajor;
        @BindView(R.id.tvMinor)
        TextView tvMinor;
        @BindView(R.id.ctvStatus)
        CheckedTextView ctvStatus;

        ViewHolder(View itemView, OnItemListener onItemClickedListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            ctvStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    BeaconProfile item = mItems.get(position);
                    int status = (item.getStatus() == BeaconProfile.STATUS_ENABLED) ? BeaconProfile.STATUS_DISABLED : BeaconProfile.STATUS_ENABLED;
                    mItemListener.onToggleButtonChecked(item.getId(), status, position);
                }
            });
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_resident_beacon, parent, false);
        return new ResidentBeaconRecyclerViewAdapter.ViewHolder(view, this.mItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ResidentBeaconRecyclerViewAdapter.ViewHolder holder, int position) {
        BeaconProfile item = mItems.get(position);

        holder.tvLabel.setText(item.getLabel());
        holder.tvUuid.setText(item.getUuid());
        holder.tvMajor.setText(String.valueOf(item.getMajor()));
        holder.tvMinor.setText(String.valueOf(item.getMinor()));
        holder.ctvStatus.setChecked(item.getStatus() == BeaconProfile.STATUS_ENABLED);
        if (holder.ctvStatus.isChecked()) {
            holder.ctvStatus.setText("Enabled");
            holder.ctvStatus.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
            holder.ctvStatus.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_green_light));
        } else {
            holder.ctvStatus.setText("Disabled");
            holder.ctvStatus.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
            holder.ctvStatus.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private BeaconProfile getItem(int adapterPosition) {
        return mItems.get(adapterPosition);
    }

    public interface OnItemListener {
        void onToggleButtonChecked(int beaconId, int status, int index);
    }

    public void updateItems(List<BeaconProfile> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public void updateItem(BeaconProfile item, int position) {
        Log.d(TAG, "Update item at position " + String.valueOf(position) + " :" + item.toString());
        mItems.set(position, item);
        notifyItemChanged(position, item);
    }

}
