package edu.np.ece.wetrack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.np.ece.wetrack.model.NearbyItem;

public class NearbyRecyclerViewAdapter extends RecyclerView.Adapter<NearbyRecyclerViewAdapter.ViewHolder> {
    private List<NearbyItem> mItems;
    private Context mContext;
    private OnItemClickedListener mItemListener;

    public NearbyRecyclerViewAdapter(Context context, List<NearbyItem> data, NearbyRecyclerViewAdapter.OnItemClickedListener listener) {
        mContext = context;
        mItems = data;
        mItemListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_nearby, parent, false);
        return new NearbyRecyclerViewAdapter.ViewHolder(view, this.mItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyRecyclerViewAdapter.ViewHolder holder, int position) {
        NearbyItem item = mItems.get(position);

        holder.tvTitle.setText(item.getResident().getGenderAge());
        holder.tvInfo.setText(item.getResident().getActiveMissing().getRemark());
        holder.tvFooter.setText(item.getLabelWithMinor());
        holder.tvDistance.setText(item.getDistanceString());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private NearbyItem getItem(int adapterPosition) {
        return mItems.get(adapterPosition);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvInfo)
        TextView tvInfo;
        @BindView(R.id.tvFooter)
        TextView tvFooter;
        @BindView(R.id.tvDistance)
        TextView tvDistance;

        NearbyRecyclerViewAdapter.OnItemClickedListener mItemListener;

        ViewHolder(View itemView, NearbyRecyclerViewAdapter.OnItemClickedListener onItemClickedListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            // Assign item listener
            this.mItemListener = onItemClickedListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NearbyItem item = getItem(getAdapterPosition());
            this.mItemListener.onItemClick(item);
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickedListener {
        void onItemClick(NearbyItem item);
    }

    public void updateItems(List<NearbyItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }
}
