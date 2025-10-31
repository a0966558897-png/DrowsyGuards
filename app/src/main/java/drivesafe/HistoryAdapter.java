package drivesafe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.drivesafe.R;                 // ← 加這行

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<HistoryItem> dataList;

    public HistoryAdapter(List<HistoryItem> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_row, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = dataList.get(position);
        holder.tvTime.setText(item.time);
        holder.tvScore.setText("疲勞指數：" + item.score);
        holder.tvStatus.setText(item.status);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvScore, tvStatus;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
