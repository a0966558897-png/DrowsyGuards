package drivesafe;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.DashPathEffect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import com.example.drivesafe.R;
import com.example.drivesafe.BuildConfig;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.drivesafe.db.AppDatabase;
import com.example.drivesafe.db.FatigueDao;
import com.example.drivesafe.db.FatigueRecord;
import drivesafe.net.ApiClient;              // ← 修正這行
import com.example.drivesafe.db.ApiService;
import com.example.drivesafe.db.FatigueDto;
import drivesafe.db.TokenStore;              // 你已經改到這裡，OK
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChartActivity extends AppCompatActivity {

    // 原本硬編 BASE_URL → 改用 BuildConfig.BASE_URL
    // private static final String BASE_URL = "http://10.0.2.2:8005/";
    private static final String FALLBACK_USER_ID  = "00000000-0000-0000-0000-000000000001";

    // Room
    private AppDatabase db;

    // UI
    private MaterialToolbar toolbar;
    private MaterialButton btnPickDate, btnExport, btnShare;
    private LineChart lineChart;
    private TextView tvAvg, tvMax, tvMin, tvEmptyHint, tvSelectedDate;

    // Colors
    @ColorInt private int colorLine;
    @ColorInt private int colorFillTop;
    @ColorInt private int colorFillBottom;
    @ColorInt private int colorTextDark;
    @ColorInt private int colorGrid;
    @ColorInt private int colorHighlight;

    // Formats
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

    // 目前使用者（從登入時存的 prefs 讀取）
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        db = AppDatabase.getInstance(getApplicationContext());

        // ★ 取得 userId：先從 Intent，其次從 SharedPreferences，最後用備援常數
        String intentUserId = getIntent().getStringExtra("user_id");
        if (intentUserId != null && !intentUserId.trim().isEmpty()) {
            currentUserId = intentUserId.trim();
        } else {
            final String PREF_NAME = "login_prefs";
            final String KEY_USER_ID = "user_id";
            currentUserId = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                    .getString(KEY_USER_ID, FALLBACK_USER_ID);
        }

        bindViews();
        bindColors();
        setupToolbar();
        setupChartAppearance();
        setupButtons();

        // 預設今天
        Calendar today = Calendar.getInstance();
        setDataForDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
    }

    private void bindViews() {
        toolbar        = findViewById(R.id.chartToolbar);
        btnPickDate    = findViewById(R.id.btnPickDate);
        lineChart      = findViewById(R.id.lineChart);
        tvAvg          = findViewById(R.id.tvAvg);
        tvMax          = findViewById(R.id.tvMax);
        tvMin          = findViewById(R.id.tvMin);
        tvEmptyHint    = findViewById(R.id.tvEmptyHint);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnExport      = findViewById(R.id.btnExport);
        btnShare       = findViewById(R.id.btnShare);
    }

    private void bindColors() {
        colorLine       = getColorCompat(this, R.color.titaniumBlueGray);
        colorFillTop    = getColorCompat(this, R.color.titaniumBlueGray);
        colorFillBottom = getColorCompat(this, R.color.silverGray);
        colorTextDark   = getColorCompat(this, R.color.textDark);
        colorGrid       = getColorCompat(this, R.color.inputStrokeGray);
        colorHighlight  = getColorCompat(this, R.color.accentRed);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupChartAppearance() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setNoDataText("尚無圖表資料");
        lineChart.setNoDataTextColor(colorGrid);
        lineChart.setExtraTopOffset(8f);
        lineChart.setExtraBottomOffset(10f);
        lineChart.setExtraLeftOffset(10f);
        lineChart.setExtraRightOffset(16f);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setHardwareAccelerationEnabled(true);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        // X 軸
        XAxis x = lineChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextColor(colorTextDark);
        x.setTextSize(11f);
        x.setDrawAxisLine(false);
        x.setDrawGridLines(true);
        x.setGridColor(colorGrid);
        x.setGridDashedLine(new DashPathEffect(new float[]{6f, 6f}, 0));

        // Y 軸
        YAxis yL = lineChart.getAxisLeft();
        yL.setAxisMinimum(0f);
        yL.setAxisMaximum(10f);
        yL.setLabelCount(6, true);
        yL.setTextColor(colorTextDark);
        yL.setTextSize(11f);
        yL.setDrawAxisLine(false);
        yL.setDrawGridLines(true);
        yL.setGridColor(colorGrid);
        yL.setGridDashedLine(new DashPathEffect(new float[]{6f, 6f}, 0));
        lineChart.getAxisRight().setEnabled(false);
    }

    /** X 軸顯示 HH:mm（x 值用「距當天 00:00 的分鐘數」） */
    private void setXAxisForDay(final long startOfDayMs) {
        XAxis x = lineChart.getXAxis();
        x.setGranularity(15f); // 15 分鐘一格
        x.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                long tMs = startOfDayMs + (long) (value * 60_000L);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(tMs);
                return timeFmt.format(c.getTime());
            }
        });
    }

    private void setupButtons() {
        btnPickDate.setOnClickListener(v -> openDatePicker());
        btnExport.setOnClickListener(v -> exportChartImage());
        btnShare.setOnClickListener(v -> shareChartImage());
    }

    /* ===== 日期選擇 & 資料載入 ===== */

    private void openDatePicker() {
        final Calendar cal = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (DatePicker view, int y, int m, int d) -> setDataForDate(y, m, d),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /** 先查本機；若空就向後端抓，寫回本機後再畫 */
    private void setDataForDate(int year, int month /*0-11*/, int day) {
        Calendar start = Calendar.getInstance();
        start.set(year, month, day, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        final long startMs = start.getTimeInMillis();

        Calendar end = (Calendar) start.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        final long endMs = end.getTimeInMillis();

        setXAxisForDay(startMs);

        // 本機查詢 → 空則拉網路
        queryDbAndRender(startMs, endMs, startMs, /*fallbackFetch*/true);

        // UI 顯示
        if (tvSelectedDate != null) {
            tvSelectedDate.setText("當日：" + dateFmt.format(start.getTime()));
        }
        TextView title = findViewById(R.id.tvChartTitle);
        if (title != null) {
            title.setText(String.format(Locale.getDefault(), "疲勞指數（%s）", dateFmt.format(start.getTime())));
        }
        if (btnPickDate != null) {
            btnPickDate.setText(dateFmt.format(start.getTime()));
        }
    }

    private void queryDbAndRender(long startMs, long endMs, long startOfDayMs, boolean fallbackFetch) {
        new Thread(() -> {
            FatigueDao dao = db.fatigueDao();
            List<FatigueRecord> records = dao.getByTimeRange(startMs, endMs);

            final List<Entry> entries = toEntries(records, startOfDayMs);

            runOnUiThread(() -> {
                if (entries.isEmpty()) {
                    if (fallbackFetch) {
                        fetchFromServerAndCacheThenRender(startMs, endMs, startOfDayMs);
                    } else {
                        showEmpty(true);
                        lineChart.clear();
                        bindStatsEmpty();
                    }
                } else {
                    showEmpty(false);
                    LineDataSet ds = new LineDataSet(entries, "疲勞指數");
                    styleDataSet(ds);
                    LineData data = new LineData(ds);
                    data.setDrawValues(false);
                    lineChart.setData(data);
                    lineChart.animateX(400);
                    lineChart.invalidate();
                    computeAndBindStats(entries);
                }
            });
        }).start();
    }

    /** 往後端 GET → 寫回 Room → 再從本機重畫 */
    private void fetchFromServerAndCacheThenRender(long startMs, long endMs, long startOfDayMs) {
        // ★ 1) 取得 ApiService（統一用 BuildConfig.BASE_URL）
        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);

        // ★ 2) 組 Authorization 標頭（無 token 時傳 null 也可呼叫）
        String token = new TokenStore(getApplicationContext()).get();
        String authHeader = (token != null && !token.isEmpty()) ? ("Bearer " + token) : null;

        // ★ 3) 使用現在的 getRecords 介面（bearerToken, userId, startMs, endMs）
        final String userId = (currentUserId == null || currentUserId.trim().isEmpty())
                ? FALLBACK_USER_ID : currentUserId;

        Call<List<FatigueDto>> call = api.getRecords(authHeader, userId, startMs, endMs);

        Toast.makeText(this, "正在從伺服器抓取資料…", Toast.LENGTH_SHORT).show();

        call.enqueue(new Callback<List<FatigueDto>>() {
            @Override public void onResponse(Call<List<FatigueDto>> call, Response<List<FatigueDto>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    onFail("伺服器回應失敗（" + resp.code() + "）");
                    return;
                }
                List<FatigueDto> list = resp.body();

                new Thread(() -> {
                    try {
                        if (list != null && !list.isEmpty()) {
                            List<FatigueRecord> toInsert = new ArrayList<>(list.size());
                            for (FatigueDto d : list) {
                                // 依你現有的 Entity API：以 timestamp_ms / score 建構，並寫入 server 狀態
                                FatigueRecord r = new FatigueRecord(d.timestamp_ms, d.score);
                                r.setServerId(d.id);
                                r.setSynced(true);
                                toInsert.add(r);
                            }
                            if (!toInsert.isEmpty()) {
                                db.fatigueDao().insertAll(toInsert);
                            }
                        }
                    } catch (Exception ignored) {}

                    runOnUiThread(() -> queryDbAndRender(startMs, endMs, startOfDayMs, /*fallbackFetch*/false));
                }).start();
            }

            @Override public void onFailure(Call<List<FatigueDto>> call, Throwable t) {
                onFail("抓取失敗：" + t.getMessage());
            }

            private void onFail(String msg) {
                runOnUiThread(() -> {
                    showEmpty(true);
                    lineChart.clear();
                    bindStatsEmpty();
                    Toast.makeText(ChartActivity.this, msg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /** 把資料轉成圖表點位：使用「有效時間」effectiveTime() */
    private List<Entry> toEntries(List<FatigueRecord> records, long startOfDayMs) {
        List<Entry> entries = new ArrayList<>();
        if (records == null) return entries;
        for (FatigueRecord r : records) {
            long ts = r.effectiveTime();
            if (ts <= 0) continue;
            float minutesFromStart = (ts - startOfDayMs) / 60_000f;
            entries.add(new Entry(minutesFromStart, r.getScore()));
        }
        return entries;
    }

    /* ===== DataSet 外觀 & 統計 ===== */

    private void styleDataSet(LineDataSet set) {
        set.setColor(colorLine);
        set.setLineWidth(2.3f);
        set.setDrawCircles(true);
        set.setCircleColor(colorLine);
        set.setCircleRadius(3.3f);
        set.setDrawCircleHole(false);

        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.18f);

        set.setHighlightEnabled(true);
        set.setHighLightColor(colorHighlight);
        set.setHighlightLineWidth(1.2f);
        set.setDrawHorizontalHighlightIndicator(false);

        set.setDrawFilled(true);
        set.setFillDrawable(makeFillGradient());
    }

    private GradientDrawable makeFillGradient() {
        int top = adjustAlpha(colorFillTop, 60);
        int bottom = adjustAlpha(colorFillBottom, 0);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{ top, bottom }
        );
        gd.setCornerRadius(0);
        return gd;
    }

    private void computeAndBindStats(List<Entry> entries) {
        float sum = 0f, max = -Float.MAX_VALUE, min = Float.MAX_VALUE;
        for (Entry e : entries) {
            float v = e.getY();
            sum += v;
            if (v > max) max = v;
            if (v < min) min = v;
        }
        float avg = entries.isEmpty() ? 0f : (sum / entries.size());
        tvAvg.setText(entries.isEmpty() ? "—" : String.format(Locale.getDefault(), "%.1f", avg));
        tvMax.setText(entries.isEmpty() ? "—" : String.format(Locale.getDefault(), "%.1f", max));
        tvMin.setText(entries.isEmpty() ? "—" : String.format(Locale.getDefault(), "%.1f", min));
    }

    private void bindStatsEmpty() {
        tvAvg.setText("—");
        tvMax.setText("—");
        tvMin.setText("—");
    }

    private void showEmpty(boolean show) {
        if (tvEmptyHint != null) tvEmptyHint.setVisibility(show ? View.VISIBLE : View.GONE);
        if (lineChart != null) lineChart.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    /* ===== 匯出 / 分享 ===== */

    private void exportChartImage() {
        Bitmap bmp = lineChart.getChartBitmap();
        if (bmp == null) {
            Toast.makeText(this, "沒有可匯出的圖表", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = saveBitmapToGallery(this, bmp, "fatigue_chart_" + System.currentTimeMillis() + ".png");
        Toast.makeText(this, uri != null ? "已匯出到相簿" : "匯出失敗", Toast.LENGTH_SHORT).show();
    }

    private void shareChartImage() {
        Bitmap bmp = lineChart.getChartBitmap();
        if (bmp == null) {
            Toast.makeText(this, "沒有可分享的圖表", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = saveBitmapToGallery(this, bmp, "share_fatigue_chart.png");
        if (uri == null) {
            Toast.makeText(this, "分享失敗", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "分享圖表"));
    }

    /* ===== Utils ===== */

    @ColorInt
    private static int getColorCompat(Context ctx, int resId) {
        return ContextCompat.getColor(ctx, resId);
    }

    @ColorInt
    private static int adjustAlpha(@ColorInt int color, int alpha /*0-255*/) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    @Nullable
    private static Uri saveBitmapToGallery(Context ctx, Bitmap bitmap, String displayName) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DriveSafe");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }
            Uri uri = ctx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return null;
            try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
                if (os == null) return null;
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                ctx.getContentResolver().update(uri, values, null, null);
            }
            return uri;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
