package drivesafe;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drivesafe.R;
import com.example.drivesafe.db.FatigueRecord;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** 列表：疲勞記錄（新版：分數＋等級＋同步 Chip） */
public class FatigueAdapter extends RecyclerView.Adapter<FatigueAdapter.ViewHolder> {

    private final List<FatigueRecord> data = new ArrayList<>();
    private final SimpleDateFormat timeFmt =
            new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(View v, int position, FatigueRecord record);
    }
    public interface OnItemLongClickListener {
        boolean onItemLongClick(View v, int position, FatigueRecord record);
    }

    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public FatigueAdapter() { setHasStableIds(true); }

    public FatigueAdapter(@NonNull List<FatigueRecord> initial) {
        setHasStableIds(true);
        setItems(initial);
    }

    // ====== 資料操作 ======
    public void setItems(@NonNull List<FatigueRecord> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void addItems(@NonNull List<FatigueRecord> items) {
        int start = data.size();
        data.addAll(items);
        notifyItemRangeInserted(start, items.size());
    }

    public void addItem(@NonNull FatigueRecord item) {
        data.add(item);
        notifyItemInserted(data.size() - 1);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    /** 讓外部可用 adapter.getItem(pos) */
    public FatigueRecord getItem(int position) {
        if (position < 0 || position >= data.size()) return null;
        return data.get(position);
    }

    @Override public long getItemId(int position) {
        FatigueRecord r = getItem(position);
        return (r != null) ? r.getId() : RecyclerView.NO_ID;
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.clickListener = l; }
    public void setOnItemLongClickListener(OnItemLongClickListener l) { this.longClickListener = l; }

    // ====== Adapter ======
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fatigue_record, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        FatigueRecord r = getItem(position);
        if (r == null) return;

        // 分數顯示
        final float score = r.getScore();
        final boolean tenScale = score <= 10f;
        String scoreText = tenScale
                ? String.format(Locale.getDefault(), "%.1f 分", score)
                : String.format(Locale.getDefault(), "%.0f 分", score);
        h.tvScore.setText(scoreText);

        // 時間
        long t = r.effectiveTime();
        h.tvTime.setText((t > 0) ? timeFmt.format(new Date(t)) : "—");

        // 等級（Chip）
        if (h.chipLevel != null) {
            float danger = tenScale ? 7f : 70f;
            float warn   = tenScale ? 3f : 30f;

            String levelText;
            int bg, fg;
            if (score >= danger) {
                levelText = "警示";
                bg = Color.parseColor("#FFEBEE");
                fg = Color.parseColor("#B71C1C");
            } else if (score >= warn) {
                levelText = "注意";
                bg = Color.parseColor("#FFF8E1");
                fg = Color.parseColor("#E65100");
            } else {
                levelText = "安全";
                bg = Color.parseColor("#E8F5E9");
                fg = Color.parseColor("#1B5E20");
            }
            h.chipLevel.setText(levelText);
            h.chipLevel.setChipBackgroundColor(ColorStateList.valueOf(bg));
            h.chipLevel.setTextColor(fg);
        }

        // 同步狀態
        String syncLabel = r.isSynced() ? "已上傳" : "待上傳";
        if (h.chipSync != null) {
            h.chipSync.setText(syncLabel);
            h.chipSync.setContentDescription(syncLabel);
        } else if (h.tvMeta != null) {
            h.tvMeta.setText(syncLabel);
        }

        if (h.rowMetrics != null) {
            h.rowMetrics.setVisibility(View.GONE);
        }

        // 點擊事件
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(v, h.getBindingAdapterPosition(), r);
        });
        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onItemLongClick(v, h.getBindingAdapterPosition(), r);
            }
            return false;
        });
    }

    @Override public int getItemCount() { return data.size(); }

    // ====== ViewHolder ======
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvScore;
        final TextView tvTime;
        final Chip chipLevel;
        final Chip chipSync;
        final TextView tvMeta;
        final View rowMetrics;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScore    = itemView.findViewById(R.id.tvScore);
            tvTime     = itemView.findViewById(R.id.tvTime);
            chipLevel  = itemView.findViewById(R.id.chipLevel);
            chipSync   = itemView.findViewById(R.id.chipSync);
            tvMeta     = itemView.findViewById(R.id.tvMeta);
            rowMetrics = itemView.findViewById(R.id.rowMetrics);
        }
    }
}
