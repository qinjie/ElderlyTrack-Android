package edu.np.ece.wetrack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.np.ece.wetrack.model.BeaconProfile;

public class BeaconRecyclerViewAdapter extends RecyclerView.Adapter<BeaconRecyclerViewAdapter.ViewHolder> {
    private List<Beacon> mItems;
    private Context mContext;
    private OnItemClickedListener mItemListener;

    public BeaconRecyclerViewAdapter(Context context, List<Beacon> data, BeaconRecyclerViewAdapter.OnItemClickedListener listener) {
        mContext = context;
        mItems = data;
        mItemListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_beacon, parent, false);
        return new BeaconRecyclerViewAdapter.ViewHolder(view, this.mItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconRecyclerViewAdapter.ViewHolder holder, int position) {
        BeaconProfile item = new BeaconProfile(mItems.get(position));
        holder.tvName.setText(item.getLabel());
        holder.tvMac.setText(item.getMac());
        holder.tvUuid.setText("uuid: " + item.getUuid());
        holder.tvMajor.setText("major: " + item.getMajor());
        holder.tvMinor.setText("minor: " + item.getMinor());
        holder.tvDistance.setText(item.getDistanceString());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private Beacon getItem(int adapterPosition) {
        return mItems.get(adapterPosition);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvMac)
        TextView tvMac;
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvUuid)
        TextView tvUuid;
        @BindView(R.id.tvMajor)
        TextView tvMajor;
        @BindView(R.id.tvMinor)
        TextView tvMinor;
        @BindView(R.id.tvDistance)
        TextView tvDistance;

        BeaconRecyclerViewAdapter.OnItemClickedListener mItemListener;

        ViewHolder(View itemView, BeaconRecyclerViewAdapter.OnItemClickedListener onItemClickedListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            // Assign item listener
            this.mItemListener = onItemClickedListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Beacon item = getItem(getAdapterPosition());
            this.mItemListener.onItemClick(item);
//            notifyDataSetChanged();
        }
    }

    public interface OnItemClickedListener {
        void onItemClick(Beacon item);
    }

    public void updateItems(List<Beacon> items) {
        mItems = items;
        notifyDataSetChanged();
    }
}
