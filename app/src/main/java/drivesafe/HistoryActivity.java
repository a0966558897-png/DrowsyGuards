package drivesafe;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import com.example.drivesafe.R;
import com.example.drivesafe.BuildConfig;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drivesafe.db.AppDatabase;
import com.example.drivesafe.db.FatigueDao;
import com.example.drivesafe.db.FatigueRecord;

import drivesafe.net.ApiClient;                 // ← 修正
import com.example.drivesafe.db.ApiService;    // ← 修正
import com.example.drivesafe.db.DrivingRecordDto; // ← 修正
import com.example.drivesafe.db.FatigueDto;    // ← 修正
import com.example.drivesafe.db.MemberDto;     // ← 修正
import drivesafe.db.TokenStore;                // ← 修正

import drivesafe.FatigueAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HistoryActivity extends AppCompatActivity {

    /** 切換：true = 直接讀雲端顯示（最快達成「DB 新增 App 可見」），false = 先匯入 Room 再顯示（原邏輯） */
    private static final boolean USE_CLOUD_ONLY = true;

    // 仍保留一個暫時的 fallback（若查不到 memberId 時使用）
    private static final int TEMP_MEMBER_ID = 1;

    // 偏好 key：登入資訊
    private static final String PREF_NAME   = "login_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL   = "email"; // 回退用

    private MaterialToolbar toolbar;
    private RecyclerView rv;
    private TextView tvEmpty, tvSelectedDate;
    private MaterialButton btnAll, btnPickDate;
    private CircularProgressIndicator progress;

    private FatigueAdapter adapter;
    private AppDatabase db;
    private ExecutorService io;

    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

    // 記住目前篩選的時間範圍（all 時用 Long.MIN/MAX）
    private long lastStartMs = Long.MIN_VALUE;
    private long lastEndMs   = Long.MAX_VALUE;

    // 動態取得後端成員 id（這版我們固定成 3）
    private Integer memberId = null;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 強制亮色
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = AppDatabase.getInstance(getApplicationContext());
        io = Executors.newSingleThreadExecutor();

        toolbar = findViewById(R.id.historyToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FatigueAdapter();
        rv.setAdapter(adapter);

        tvEmpty = findViewById(R.id.tvEmptyHint);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnAll = findViewById(R.id.btnAll);
        btnPickDate = findViewById(R.id.btnPickDate);
        progress = findViewById(R.id.progress); // 可能為 null

        btnAll.setOnClickListener(v -> loadAll());
        btnPickDate.setOnClickListener(v -> openDatePicker());

        // 左滑刪除（仍只刪本機 Room；雲端刪除之後再補）
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override public boolean onMove(RecyclerView r, RecyclerView.ViewHolder vH, RecyclerView.ViewHolder t) {
                        return false;
                    }
                    @Override public void onSwiped(RecyclerView.ViewHolder vH, int dir) {
                        int pos = vH.getBindingAdapterPosition();
                        FatigueRecord rec = adapter.getItem(pos);
                        if (rec == null) {
                            runOnUiThread(() -> adapter.notifyItemChanged(pos));
                            return;
                        }
                        io.execute(() -> {
                            db.fatigueDao().delete(rec);
                            runOnUiThread(() -> {
                                reloadAfterChange(); // 依目前範圍重載
                                toast("已刪除（本機）");
                            });
                        });
                    }
                });
        helper.attachToRecyclerView(rv);

        // ✅ 固定使用 memberId = 3 來讀雲端資料
        memberId = 3;
        loadAll();

        if (!USE_CLOUD_ONLY) {
            importCloudRecordsToRoom(memberId, this::reloadAfterChange);
            testUploadOneRecord();
        }
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear) {
            confirmClearAll();
            return true;
        } else if (id == R.id.action_export) {
            exportCsvCurrent();
            return true;
        } else if (id == R.id.action_sync) {
            if (USE_CLOUD_ONLY) {
                loadAll();
            } else {
                resyncDays(30);
            }
            return true;
        }
        return false;
    }

    /* =====（暫時不用）解析 memberId：用登入 email 去 /members 對應 ===== */
    private void resolveMemberIdAndThenLoad() {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String email = sp.getString(KEY_EMAIL, null);

        if (email == null || email.trim().isEmpty()) {
            loadAll();
            return;
        }

        showLoading(true);
        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);
        api.getMembers().enqueue(new Callback<List<MemberDto>>() {
            @Override public void onResponse(Call<List<MemberDto>> call, Response<List<MemberDto>> resp) {
                showLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    toast("取得成員失敗，code=" + resp.code());
                    loadAll();
                    return;
                }
                Integer found = null;
                String want = email.trim().toLowerCase(Locale.ROOT);
                for (MemberDto m : resp.body()) {
                    if (m == null || m.email == null) continue;
                    if (want.equals(m.email.trim().toLowerCase(Locale.ROOT))) {
                        found = m.id;
                        break;
                    }
                }
                memberId = found;
                loadAll();
            }

            @Override public void onFailure(Call<List<MemberDto>> call, Throwable t) {
                showLoading(false);
                toast("取得成員錯誤：" + t.getMessage());
                loadAll();
            }
        });
    }

    /* ===== 取得目前 userId（舊 schema 用；沒有就回退 email，再沒有才用測試 ID） ===== */
    private @Nullable String currentUserId() {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String uid = sp.getString(KEY_USER_ID, null);
        if (uid == null || uid.isEmpty()) {
            uid = sp.getString(KEY_EMAIL, null);
        }
        if (uid == null || uid.isEmpty()) {
            uid = "00000000-0000-0000-0000-000000000001";
        }
        return uid;
    }

    /* ===== 新增：雲端直讀並顯示（不落地 Room） ===== */
    private void cloudFetchAllThenShow(@Nullable Long rangeStart, @Nullable Long rangeEnd) {
        showLoading(true);

        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);
        final int mid = (memberId == null ? TEMP_MEMBER_ID : memberId);
        api.getDrivingRecords(mid).enqueue(new Callback<List<DrivingRecordDto>>() {
            @Override
            public void onResponse(Call<List<DrivingRecordDto>> call, Response<List<DrivingRecordDto>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        toast("雲端讀取失敗，code=" + resp.code());
                        if (!USE_CLOUD_ONLY) reloadAfterChange();
                    });
                    return;
                }
                List<DrivingRecordDto> remote = resp.body();
                List<FatigueRecord> viewList = new ArrayList<>();
                if (remote != null) {
                    for (DrivingRecordDto d : remote) {
                        long when = parseIsoMillis(d.start_time);
                        if (when <= 0) continue;
                        if (rangeStart != null && rangeEnd != null) {
                            if (when < rangeStart || when > rangeEnd) continue;
                        }
                        FatigueRecord r = new FatigueRecord();
                        r.setDetectedAt(when);
                        r.setTimestampMs(when);
                        r.setScore(mapFatigueLevelToScore(d.fatigue_level));
                        r.setServerId(d.id == null ? null : String.valueOf(d.id));
                        r.setSynced(true);
                        viewList.add(r);
                    }
                }
                runOnUiThread(() -> {
                    showLoading(false);
                    adapter.setItems(viewList);
                    checkEmptyState();
                    if (viewList.isEmpty()) toast("目前範圍無資料（雲端）");
                });
            }

            @Override
            public void onFailure(Call<List<DrivingRecordDto>> call, Throwable t) {
                runOnUiThread(() -> {
                    showLoading(false);
                    toast("連線錯誤：" + t.getMessage());
                    if (!USE_CLOUD_ONLY) reloadAfterChange();
                });
            }
        });
    }

    /* ===== 載入資料 ===== */
    private void loadAll() {
        lastStartMs = Long.MIN_VALUE;
        lastEndMs   = Long.MAX_VALUE;
        tvSelectedDate.setText("目前：全部");

        if (USE_CLOUD_ONLY) {
            cloudFetchAllThenShow(null, null);
            return;
        }

        showLoading(true);
        io.execute(() -> {
            List<FatigueRecord> list = db.fatigueDao().getAllDesc();
            runOnUiThread(() -> {
                adapter.setItems(list);
                checkEmptyState();
                showLoading(false);
                if (list.isEmpty()) {
                    resyncDays(30);
                }
            });
        });
    }

    private void openDatePicker() {
        final Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (DatePicker view, int y, int m, int d) -> loadForDate(y, m, d),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void loadForDate(int year, int month, int day) {
        Calendar start = Calendar.getInstance();
        start.set(year, month, day, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        long startMs = start.getTimeInMillis();

        Calendar end = (Calendar) start.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        long endMs = end.getTimeInMillis();

        lastStartMs = startMs;
        lastEndMs   = endMs;

        tvSelectedDate.setText("目前：" + dateFmt.format(start.getTime()));

        if (USE_CLOUD_ONLY) {
            cloudFetchAllThenShow(startMs, endMs);
            return;
        }

        showLoading(true);
        io.execute(() -> {
            FatigueDao dao = db.fatigueDao();
            List<FatigueRecord> list = dao.getByTimeRange(startMs, endMs);
            runOnUiThread(() -> {
                adapter.setItems(list);
                checkEmptyState();
                showLoading(false);
                if (list.isEmpty()) {
                    fetchFromServerAndCache(startMs, endMs, this::reloadRangeAfterSync);
                }
            });
        });
    }

    /* ===== 舊 schema：FatigueDto（保留）===== */

    private void resyncDays(int days) {
        Calendar end = Calendar.getInstance();
        Calendar start = (Calendar) end.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        start.add(Calendar.DAY_OF_YEAR, -days);

        long startMs = start.getTimeInMillis();
        long endMs   = end.getTimeInMillis();

        fetchFromServerAndCache(startMs, endMs, this::reloadAfterChange);
    }

    private void fetchFromServerAndCache(long startMs, long endMs, Runnable onDone) {
        showLoading(true);

        String uid = currentUserId();

        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);

        String token = new TokenStore(getApplicationContext()).get();
        String authHeader = (token != null && !token.isEmpty()) ? "Bearer " + token : null;

        Call<List<FatigueDto>> call = api.getRecords(authHeader, uid, startMs, endMs);

        call.enqueue(new Callback<List<FatigueDto>>() {
            @Override public void onResponse(Call<List<FatigueDto>> c, Response<List<FatigueDto>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    onSyncFail("伺服器回應失敗（" + resp.code() + "）");
                    return;
                }
                List<FatigueDto> list = resp.body();
                io.execute(() -> {
                    try {
                        if (list != null && !list.isEmpty()) {
                            List<FatigueRecord> toInsert = new ArrayList<>(list.size());
                            for (FatigueDto d : list) {
                                FatigueRecord r = new FatigueRecord();
                                long t = d.timestamp_ms;
                                r.setDetectedAt(t);
                                r.setTimestampMs(t);
                                r.setScore(d.score);
                                r.setServerId(d.id);
                                r.setSynced(true);
                                toInsert.add(r);
                            }
                            if (!toInsert.isEmpty()) {
                                db.fatigueDao().insertAll(toInsert);
                            }
                        }
                    } catch (Exception ignore) {}
                    runOnUiThread(() -> {
                        showLoading(false);
                        if (onDone != null) onDone.run();
                        if (list == null || list.isEmpty()) {
                            toast("沒有可同步的資料");
                        } else {
                            toast("同步完成");
                        }
                    });
                });
            }

            @Override public void onFailure(Call<List<FatigueDto>> c, Throwable t) {
                onSyncFail("同步失敗：" + t.getMessage());
            }

            private void onSyncFail(String msg) {
                runOnUiThread(() -> {
                    showLoading(false);
                    toast(msg);
                    checkEmptyState();
                });
            }
        });
    }

    /* ===== 新 schema：把雲端資料匯入 Room（目前用不到） ===== */
    private void importCloudRecordsToRoom(int memberId, @Nullable Runnable onDone) {
        showLoading(true);

        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);
        api.getDrivingRecords(memberId).enqueue(new Callback<List<DrivingRecordDto>>() {
            @Override
            public void onResponse(Call<List<DrivingRecordDto>> call,
                                   Response<List<DrivingRecordDto>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        toast("雲端同步失敗，code=" + resp.code());
                    });
                    return;
                }
                List<DrivingRecordDto> list = resp.body();
                io.execute(() -> {
                    try {
                        if (list != null && !list.isEmpty()) {
                            List<FatigueRecord> toInsert = new ArrayList<>(list.size());
                            for (DrivingRecordDto d : list) {
                                long when = parseIsoMillis(d.start_time);
                                if (when <= 0) continue;

                                FatigueRecord r = new FatigueRecord();
                                r.setDetectedAt(when);
                                r.setTimestampMs(when);
                                r.setScore(mapFatigueLevelToScore(d.fatigue_level));
                                r.setServerId(d.id == null ? null : String.valueOf(d.id));
                                r.setSynced(true);
                                toInsert.add(r);
                            }
                            if (!toInsert.isEmpty()) {
                                db.fatigueDao().insertAll(toInsert);
                            }
                        }
                    } catch (Exception ignored) {}

                    runOnUiThread(() -> {
                        showLoading(false);
                        toast("雲端同步完成（匯入 " + (list == null ? 0 : list.size()) + " 筆）");
                        if (onDone != null) onDone.run();
                    });
                });
            }

            @Override
            public void onFailure(Call<List<DrivingRecordDto>> call, Throwable t) {
                runOnUiThread(() -> {
                    showLoading(false);
                    toast("連線錯誤：" + t.getMessage());
                });
            }
        });
    }

    /** 解析 ISO8601；支援 Z 或 +08:00，回傳毫秒 */
    private long parseIsoMillis(String iso) {
        if (iso == null || iso.isEmpty()) return 0L;
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US);
            f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return f.parse(iso).getTime();
        } catch (Exception e) {
            try {
                String s = iso.split("\\.")[0] + "Z";
                SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US);
                f2.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return f2.parse(s).getTime();
            } catch (Exception ignore) {
                return 0L;
            }
        }
    }

    /** 把 fatigue_level 字串轉成分數；數字字串直接轉，否則用等級對應 */
    private float mapFatigueLevelToScore(String level) {
        if (level == null) return 0f;
        try {
            return Float.parseFloat(level.trim());
        } catch (Exception ignore) { }
        String v = level.trim().toUpperCase(Locale.ROOT);
        switch (v) {
            case "HIGH":   return 8.0f;
            case "MEDIUM": return 5.0f;
            case "LOW":    return 2.0f;
            default:       return 0f;
        }
    }

    // === 單筆上傳測試：把一筆資料 POST 到雲端（新 schema） ===
    private void testUploadOneRecord() {
        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);

        DrivingRecordDto dto = new DrivingRecordDto();
        dto.member_id = (memberId == null ? TEMP_MEMBER_ID : memberId);
        long now = System.currentTimeMillis();
        dto.start_time = isoFromMillis(now);
        dto.end_time   = isoFromMillis(now + 15 * 60_000L);
        dto.location_start = "App";
        dto.location_end   = "App";
        dto.fatigue_level  = "6.5";
        dto.fatigue_detected = true;

        api.postDrivingRecord(dto).enqueue(new Callback<DrivingRecordDto>() {
            @Override
            public void onResponse(Call<DrivingRecordDto> call, Response<DrivingRecordDto> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    toast("上傳失敗，code=" + resp.code());
                    return;
                }
                toast("上傳成功，id=" + resp.body().id);

                io.execute(() -> {
                    long when = parseIsoMillis(dto.start_time);
                    FatigueRecord r = new FatigueRecord();
                    r.setDetectedAt(when);
                    r.setTimestampMs(when);
                    r.setScore(mapFatigueLevelToScore(dto.fatigue_level));
                    r.setServerId(resp.body().id == null ? null : String.valueOf(resp.body().id));
                    r.setSynced(true);
                    db.fatigueDao().insert(r);
                    runOnUiThread(HistoryActivity.this::reloadAfterChange);
                });
            }

            @Override
            public void onFailure(Call<DrivingRecordDto> call, Throwable t) {
                toast("連線錯誤：" + t.getMessage());
            }
        });
    }

    /** 把毫秒轉成 ISO8601（UTC，尾端 Z） */
    private String isoFromMillis(long ms) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return f.format(new java.util.Date(ms));
    }

    /* ===== 重載邏輯：依目前範圍刷新 ===== */
    private void reloadAfterChange() {
        if (USE_CLOUD_ONLY) {
            if (lastStartMs == Long.MIN_VALUE && lastEndMs == Long.MAX_VALUE) {
                cloudFetchAllThenShow(null, null);
            } else {
                cloudFetchAllThenShow(lastStartMs, lastEndMs);
            }
            return;
        }

        if (lastStartMs == Long.MIN_VALUE && lastEndMs == Long.MAX_VALUE) {
            io.execute(() -> {
                List<FatigueRecord> list = db.fatigueDao().getAllDesc();
                runOnUiThread(() -> {
                    adapter.setItems(list);
                    checkEmptyState();
                });
            });
        } else {
            reloadRangeAfterSync();
        }
    }

    private void reloadRangeAfterSync() {
        if (USE_CLOUD_ONLY) {
            cloudFetchAllThenShow(lastStartMs, lastEndMs);
            return;
        }
        final long s = lastStartMs, e = lastEndMs;
        io.execute(() -> {
            List<FatigueRecord> list = db.fatigueDao().getByTimeRange(s, e);
            runOnUiThread(() -> {
                adapter.setItems(list);
                checkEmptyState();
            });
        });
    }

    /* ===== 空狀態 / Loading ===== */

    private void checkEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);
    }

    private void showLoading(boolean show) {
        if (progress != null) {
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /* ===== 一鍵清空（本機） ===== */
    private void confirmClearAll() {
        new AlertDialog.Builder(this)
                .setTitle("清空所有紀錄")
                .setMessage("確定要刪除所有疲勞紀錄嗎？此動作無法復原。")
                .setPositiveButton("刪除", (d, w) -> {
                    showLoading(true);
                    io.execute(() -> {
                        db.fatigueDao().deleteAll();
                        runOnUiThread(() -> {
                            adapter.setItems(java.util.Collections.emptyList());
                            checkEmptyState();
                            showLoading(false);
                            toast("已清空");
                        });
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /* ===== 匯出 CSV（依目前列表） ===== */
    private void exportCsvCurrent() {
        try {
            File dir = getExternalFilesDir(null);
            if (dir == null) {
                toast("路徑取得失敗");
                return;
            }
            String name = "DriveSafe_" + System.currentTimeMillis() + ".csv";
            File file = new File(dir, name);

            StringBuilder sb = new StringBuilder();
            sb.append("id,timestamp_ms,time_local,score,synced,server_id\n");
            SimpleDateFormat full = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

            for (int i = 0; i < adapter.getItemCount(); i++) {
                FatigueRecord r = adapter.getItem(i);
                if (r == null) continue;
                long effective = r.effectiveTime();
                Long legacyTs = r.getTimestampMs();
                String localTime = (effective > 0) ? full.format(new java.util.Date(effective)) : "";

                sb.append(r.getId()).append(',')
                        .append(legacyTs == null ? "" : legacyTs).append(',')
                        .append('"').append(localTime).append('"').append(',')
                        .append(r.getScore()).append(',')
                        .append(r.isSynced() ? 1 : 0).append(',')
                        .append(r.getServerId() == null ? "" : r.getServerId())
                        .append('\n');
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(sb.toString().getBytes());
            }
            toast("已匯出：" + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            toast("匯出失敗：" + e.getMessage());
        }
    }

    /* ===== 小工具 ===== */
    private void toast(String s) {
        Toast.makeText(HistoryActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (io != null) io.shutdown();
    }
}
