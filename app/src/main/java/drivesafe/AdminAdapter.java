package drivesafe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.drivesafe.R;

import com.example.drivesafe.db.FatigueRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.VH> {

    public interface OnRowAction {
        void onEditClicked(@NonNull FatigueRecord r);
        void onDeleteClicked(@NonNull FatigueRecord r);
    }

    private final List<FatigueRecord> data;
    private final OnRowAction cb;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public AdminAdapter(@NonNull List<FatigueRecord> data, @NonNull OnRowAction cb) {
        this.data = data;
        this.cb = cb;
        setHasStableIds(true); // 可選：用 DB id 當 stable id
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= data.size()) return RecyclerView.NO_ID;
        return data.get(position).getId();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_record, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final FatigueRecord r = data.get(position);

        long t = r.effectiveTime();
        h.tvDate.setText(t > 0 ? fmt.format(new Date(t)) : "—");

        h.tvMeta.setText(String.format(
                Locale.getDefault(),
                "ID %d • Score %.2f",
                r.getId(),
                r.getScore()
        ));

        h.btnEdit.setOnClickListener(v -> cb.onEditClicked(r));
        h.btnDelete.setOnClickListener(v -> cb.onDeleteClicked(r));
        h.itemView.setOnClickListener(v -> cb.onEditClicked(r));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvDate, tvMeta;
        final View btnEdit, btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
