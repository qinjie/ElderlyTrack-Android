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

import edu.np.ece.wetrack.model.Missing;
import edu.np.ece.wetrack.model.ResidentWithMissing;

public class MissingRecyclerViewAdapter extends RecyclerView.Adapter<MissingRecyclerViewAdapter.ViewHolder> {
    private List<ResidentWithMissing> mItems;
    private Context mContext;
    private OnItemClickedListener mItemListener;

    public MissingRecyclerViewAdapter(Context context, List<ResidentWithMissing> data, MissingRecyclerViewAdapter.OnItemClickedListener listener) {
        mContext = context;
        mItems = data;
        mItemListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_missing, parent, false);
        return new MissingRecyclerViewAdapter.ViewHolder(view, this.mItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MissingRecyclerViewAdapter.ViewHolder holder, int position) {
        ResidentWithMissing item = mItems.get(position);
        Missing missing = item.getActiveMissing();

        holder.tvTitle.setText(item.getGenderAge());
        if (missing != null) {
            holder.tvInfo.setText(missing.getRemark());
            holder.tvReportedAt.setText("reported: " + missing.getReportedAtLocal(null));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private ResidentWithMissing getItem(int adapterPosition) {
        return mItems.get(adapterPosition);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle, tvInfo, tvReportedAt;
        ImageView ivAvatar;

        MissingRecyclerViewAdapter.OnItemClickedListener mItemListener;

        ViewHolder(View itemView, MissingRecyclerViewAdapter.OnItemClickedListener onItemClickedListener) {
            super(itemView);
            ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
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
//            notifyDataSetChanged();
        }
    }

    public interface OnItemClickedListener {
        void onItemClick(ResidentWithMissing item);
    }

    public void updateItems(List<ResidentWithMissing> items) {
        mItems = items;
        notifyDataSetChanged();
    }
}
