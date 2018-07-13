package edu.np.ece.wetrack;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.np.ece.wetrack.model.Missing;
import edu.np.ece.wetrack.model.Resident;
import edu.np.ece.wetrack.model.ResidentWithMissing;

public class RelativeRecyclerViewAdapter extends RecyclerView.Adapter<RelativeRecyclerViewAdapter.ViewHolder> {
    private List<ResidentWithMissing> mItems;
    private Context mContext;
    private OnItemClickedListener mItemListener;

    public RelativeRecyclerViewAdapter(Context context, List<ResidentWithMissing> data, RelativeRecyclerViewAdapter.OnItemClickedListener listener) {
        mContext = context;
        mItems = data;
        mItemListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_relative, parent, false);
        return new RelativeRecyclerViewAdapter.ViewHolder(view, this.mItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RelativeRecyclerViewAdapter.ViewHolder holder, int position) {
        ResidentWithMissing item = mItems.get(position);

        holder.tvTitle.setText(item.getNameAge());
        holder.tvInfo.setText(item.getRemarkOrComment());
        if (item.getStatus() == Resident.STATUS_MISSING) {
            holder.ivStatus.setBackgroundColor(Color.RED);
        } else {
            holder.ivStatus.setBackgroundColor(Color.LTGRAY);
        }
        Missing missing = item.getActiveMissing();
        if (missing != null) {
            if (missing.getReportedAt() != null) {
                holder.tvReportedAt.setText("Last seen: "
                        + missing.getAddressOrGps() + " "
                        + missing.getUpdatedAtLocal(null));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void updateItems(List<ResidentWithMissing> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    private ResidentWithMissing getItem(int adapterPosition) {
        return mItems.get(adapterPosition);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle, tvInfo, tvLastSeen, tvReportedAt;
        ImageView ivAvatar, ivStatus;
        RelativeRecyclerViewAdapter.OnItemClickedListener mItemListener;

        ViewHolder(View itemView, RelativeRecyclerViewAdapter.OnItemClickedListener onItemClickedListener) {
            super(itemView);
            ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
            ivStatus = (ImageView) itemView.findViewById(R.id.ivStatus);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvInfo = (TextView) itemView.findViewById(R.id.tvInfo);
            tvReportedAt = (TextView) itemView.findViewById(R.id.tvReportedAt);

            // Assign item listener
            this.mItemListener = onItemClickedListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ResidentWithMissing item = getItem(getAdapterPosition());
            this.mItemListener.onItemClick(item);
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickedListener {
        void onItemClick(ResidentWithMissing item);
    }
}
