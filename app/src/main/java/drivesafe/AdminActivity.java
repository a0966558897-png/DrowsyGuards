package drivesafe;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import drivesafe.admin.AdminRecordAdapter;          // ← 修正路徑
import com.example.drivesafe.db.AppDatabase;
import com.example.drivesafe.db.FatigueDao;
import com.example.drivesafe.db.FatigueRecord;
import com.example.drivesafe.R;                      // ← 建議加上，避免 R 未解析

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AdminActivity extends AppCompatActivity implements AdminRecordAdapter.Listener {

    private AppDatabase db;
    private FatigueDao dao;
    private ExecutorService exec;

    private MaterialToolbar toolbar;
    private RecyclerView rv;
    private TextView tvSelectedDate, tvCount, tvAvg;
    private MaterialButton btnFilterAll, btnPickDate, btnDeleteAll;

    private AdminRecordAdapter adapter;
    private LiveData<List<FatigueRecord>> live;

    private final SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db  = AppDatabase.getInstance(getApplicationContext());
        dao = db.fatigueDao();
        exec = Executors.newSingleThreadExecutor();

        toolbar        = findViewById(R.id.adminToolbar);
        rv             = findViewById(R.id.rvAdmin);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvCount        = findViewById(R.id.tvCount);
        tvAvg          = findViewById(R.id.tvAvg);
        btnFilterAll   = findViewById(R.id.btnFilterAll);
        btnPickDate    = findViewById(R.id.btnPickDate);
        btnDeleteAll   = findViewById(R.id.btnDeleteAll);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRecordAdapter(this);
        rv.setAdapter(adapter);

        btnFilterAll.setOnClickListener(v -> switchToAll());
        btnPickDate.setOnClickListener(v -> openDatePicker());
        btnDeleteAll.setOnClickListener(v -> confirmDeleteAll());

        tvSelectedDate.setText("目前：全部");
        switchToAll();  // 初始載入
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) finish();
        else if (id == R.id.action_add) showAddDialog();
        else if (id == R.id.action_export) exportCsv();
        else if (id == R.id.action_refresh) toast("列表會自動更新");
        return true;
    }

    // ------- 資料觀察 -------
    private void switchToAll() {
        if (live != null) live.removeObservers(this);
        tvSelectedDate.setText("目前：全部");
        live = dao.observeAll();
        live.observe(this, list -> {
            adapter.submit(list);
            updateStatsFrom(list);
        });
    }

    private void switchToRange(long start, long end, String label) {
        if (live != null) live.removeObservers(this);
        tvSelectedDate.setText("目前：" + label);
        live = dao.observeBetween(start, end);
        live.observe(this, list -> {
            adapter.submit(list);
            updateStatsFrom(list);
        });
    }

    private void updateStatsFrom(List<FatigueRecord> list) {
        int n = list == null ? 0 : list.size();
        tvCount.setText(String.valueOf(n));
        if (n == 0) {
            tvAvg.setText("—");
            return;
        }
        double sum = 0;
        for (FatigueRecord r : list) sum += r.getScore();
        tvAvg.setText(String.format(Locale.getDefault(), "%.2f", sum / n));
    }

    private void openDatePicker() {
        final Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (DatePicker view, int y, int m, int d) -> {
                    Calendar start = Calendar.getInstance();
                    start.set(y, m, d, 0, 0, 0);
                    start.set(Calendar.MILLISECOND, 0);
                    Calendar end = (Calendar) start.clone();
                    end.set(Calendar.HOUR_OF_DAY, 23);
                    end.set(Calendar.MINUTE, 59);
                    end.set(Calendar.SECOND, 59);
                    end.set(Calendar.MILLISECOND, 999);
                    switchToRange(start.getTimeInMillis(), end.getTimeInMillis(), dayFmt.format(start.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ------- 新增 -------
    private void showAddDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_admin_edit, null, false);
        final EditText etScore = view.findViewById(R.id.etScore);
        final EditText etTime  = view.findViewById(R.id.etTime);
        etScore.setHint("分數（0~10）");
        etTime.setHint("時間戳 (ms)；留空 = 現在");

        new AlertDialog.Builder(this)
                .setTitle("新增紀錄")
                .setView(view)
                .setPositiveButton("儲存", (d, w) -> {
                    float score;
                    long ts = System.currentTimeMillis();
                    try { score = Float.parseFloat(etScore.getText().toString().trim()); }
                    catch (Exception e) { toast("分數格式錯誤"); return; }

                    String sTime = etTime.getText().toString().trim();
                    if (!sTime.isEmpty()) {
                        try { ts = Long.parseLong(sTime); }
                        catch (Exception e) { toast("時間格式錯誤"); return; }
                    }

                    FatigueRecord record = new FatigueRecord(ts, score);
                    exec.execute(() -> db.fatigueDao().insert(record));
                    toast("已新增");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ------- 編輯 -------
    @Override public void onEdit(@NonNull FatigueRecord r) {
        View view = getLayoutInflater().inflate(R.layout.dialog_admin_edit, null, false);
        final EditText etScore = view.findViewById(R.id.etScore);
        final EditText etTime  = view.findViewById(R.id.etTime);
        etScore.setText(String.valueOf(r.getScore()));
        etTime.setText(String.valueOf(r.effectiveTime()));

        new AlertDialog.Builder(this)
                .setTitle("編輯紀錄 #" + r.getId())
                .setView(view)
                .setPositiveButton("儲存", (d, w) -> {
                    float score;
                    long ts;
                    try { score = Float.parseFloat(etScore.getText().toString().trim()); }
                    catch (Exception e) { toast("分數格式錯誤"); return; }
                    try { ts = Long.parseLong(etTime.getText().toString().trim()); }
                    catch (Exception e) { toast("時間格式錯誤"); return; }

                    r.setScore(score);
                    r.setBothTimes(ts);
                    exec.execute(() -> dao.update(r));
                    toast("已更新");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ------- 刪除 -------
    @Override public void onDelete(@NonNull FatigueRecord r) {
        new AlertDialog.Builder(this)
                .setMessage("刪除這筆紀錄？")
                .setPositiveButton("刪除", (d, w) -> exec.execute(() -> dao.delete(r)))
                .setNegativeButton("取消", null)
                .show();
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setMessage("確定要清空所有紀錄？")
                .setPositiveButton("清空", (d, w) -> exec.execute(() -> dao.clearAll()))
                .setNegativeButton("取消", null)
                .show();
    }

    // ------- CSV 匯出 -------
    private void exportCsv() {
        List<FatigueRecord> list = adapter.current();
        if (list.isEmpty()) { toast("沒有資料可匯出"); return; }

        String fileName = "fatigue_export_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) +
                ".csv";

        File dir = getExternalFilesDir(null);
        if (dir == null) { toast("找不到儲存位置"); return; }
        File out = new File(dir, fileName);

        exec.execute(() -> {
            try (PrintWriter pw = new PrintWriter(new FileWriter(out))) {
                pw.println("id,timestamp,score,synced,serverId");
                for (FatigueRecord r : list) {
                    pw.printf(Locale.US, "%d,%d,%.2f,%d,%s%n",
                            r.getId(),
                            r.effectiveTime(),
                            r.getScore(),
                            r.isSynced() ? 1 : 0,
                            r.getServerId() == null ? "" : r.getServerId());
                }
                runOnUiThread(() -> toast("已匯出：" + out.getAbsolutePath()));
            } catch (Exception e) {
                runOnUiThread(() -> toast("匯出失敗：" + e.getMessage()));
            }
        });
    }

    // ------- 通用 -------
    @Override public void onRowClick(@NonNull FatigueRecord r) {
        onEdit(r); // 點整排也叫出編輯
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (exec != null) exec.shutdown();
    }
}
