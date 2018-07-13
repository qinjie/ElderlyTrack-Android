package edu.np.ece.elderlytrack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.np.ece.elderlytrack.model.BeaconProfile;
import edu.np.ece.elderlytrack.model.LocationWithBeacon;

public class MissingLocationRecyclerViewAdapter extends RecyclerView.Adapter<MissingLocationRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = MissingLocationRecyclerViewAdapter.class.getSimpleName();
    private List<LocationWithBeacon> mItems;
    private Context mContext;
    private MissingLocationRecyclerViewAdapter.OnItemListener mItemListener;

    public MissingLocationRecyclerViewAdapter(Context context, List<LocationWithBeacon> data, OnItemListener listener) {
        mContext = context;
        mItems = data;
        mItemListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvBeaconLabel)
        TextView tvBeaconLabel;
        @BindView(R.id.tvAddress)
        TextView tvAddress;
        @BindView(R.id.tvLongitude)
        TextView tvLongitude;
        @BindView(R.id.tvLatitude)
        TextView tvLatitude;
        @BindView(R.id.tvReportedAt)
        TextView tvReportedAt;

        ViewHolder(View itemView, OnItemListener onItemClickedListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mItemListener = onItemClickedListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemListener.onItemClicked(mItems.get(getAdapterPosition()));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_missing_location, parent, false);
        return new MissingLocationRecyclerViewAdapter.ViewHolder(view, this.mItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MissingLocationRecyclerViewAdapter.ViewHolder holder, int position) {
        LocationWithBeacon item = mItems.get(position);

        BeaconProfile beacon = item.getBeacon();
        if (beacon != null) {
            holder.tvBeaconLabel.setText(beacon.getLabel());
        }
        holder.tvAddress.setText(item.getAddress());
        holder.tvAddress.setVisibility(item.getAddress() == null ? View.GONE : View.VISIBLE);
        holder.tvLatitude.setText(String.valueOf(item.getLatitude()));
        holder.tvLongitude.setText(String.valueOf(item.getLongitude()));
        holder.tvReportedAt.setText(item.getCreatedAtLocal(null));
    }

    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        else
            return 0;
    }

    private LocationWithBeacon getItem(int adapterPosition) {
        return mItems.get(adapterPosition);
    }

    public interface OnItemListener {
        public void onItemClicked(LocationWithBeacon item);
    }

    public void updateItems(List<LocationWithBeacon> items) {
        mItems = items;
        notifyDataSetChanged();
    }

}
