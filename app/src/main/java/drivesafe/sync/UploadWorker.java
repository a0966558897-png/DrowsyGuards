package drivesafe.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.drivesafe.BuildConfig;
import com.example.drivesafe.db.AppDatabase;
import com.example.drivesafe.db.FatigueDao;
import com.example.drivesafe.db.FatigueRecord;

import drivesafe.net.ApiClient;                // ← ApiClient 在 drivesafe.net
import com.example.drivesafe.db.ApiService;   // ← Api 介面在 com.example.drivesafe.db
import com.example.drivesafe.db.FatigueDto;   // ← DTO 也在 db
import drivesafe.db.TokenStore;               // ← TokenStore 在 drivesafe.db

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;


/**
 * 將本機未同步紀錄上傳後端，成功後標記 synced=true 並寫回 serverId。
 */
public class UploadWorker extends Worker {

    // 預設（若 enqueue 時沒傳就用這個）
    private static final String DEFAULT_BASE_URL = BuildConfig.BASE_URL;
    private static final String DEFAULT_USER_ID  = "00000000-0000-0000-0000-000000000001";

    // InputData keys
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_USER_ID  = "user_id";

    private final AppDatabase db;
    private final FatigueDao dao;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = AppDatabase.getInstance(context.getApplicationContext());
        dao = db.fatigueDao();
    }

    /** 供外部呼叫：一次排程上傳工作 */
    public static void enqueue(@NonNull Context ctx, @NonNull String baseUrl, @NonNull String userId) {
        Data input = new Data.Builder()
                .putString(KEY_BASE_URL, baseUrl)
                .putString(KEY_USER_ID, userId)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setInputData(input)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(ctx.getApplicationContext()).enqueue(req);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // 讀取 InputData，沒有就用預設
            String baseUrl = getInputData().getString(KEY_BASE_URL);
            if (baseUrl == null || baseUrl.isEmpty()) baseUrl = DEFAULT_BASE_URL;

            String userId = getInputData().getString(KEY_USER_ID);
            if (userId == null || userId.isEmpty()) userId = DEFAULT_USER_ID;

            // 建立 ApiService（ApiClient 現在回傳 Retrofit，這裡用 .create(...)）
            ApiService api = ApiClient.get(baseUrl).create(ApiService.class);

            // 1) 取未同步
            List<FatigueRecord> unsynced = dao.getUnsynced();
            if (unsynced == null || unsynced.isEmpty()) {
                return Result.success();
            }

            // 2) 組 DTO（時間以 effectiveTime() 為準）
            List<FatigueDto> payload = new ArrayList<>(unsynced.size());
            for (FatigueRecord r : unsynced) {
                long ts = r.effectiveTime();
                if (ts <= 0) continue;
                payload.add(FatigueDto.of(userId, ts, r.getScore()));
            }
            if (payload.isEmpty()) {
                return Result.success();
            }

            // 3) Authorization 標頭（若無 token 就傳 null）
            String token = new TokenStore(getApplicationContext()).get();
            String authHeader = (token != null && !token.isEmpty()) ? ("Bearer " + token) : null;

            // 4) 呼叫後端（多筆上傳）
            Response<List<FatigueDto>> resp = api.postRecords(authHeader, payload).execute();
            if (!resp.isSuccessful() || resp.body() == null) {
                return Result.retry();
            }
            List<FatigueDto> created = resp.body();

            // 5) 寫回本機（標記 synced 並存 serverId）
            for (FatigueRecord r : unsynced) {
                // 嘗試找出對應的 FatigueDto：用時間戳 & 分數比對
                FatigueDto match = null;
                for (FatigueDto d : created) {
                    if (d != null && d.timestamp_ms == r.effectiveTime()
                            && d.score == r.getScore()) {
                        match = d;
                        break;
                    }
                }
                if (match != null && match.id != null) {
                    dao.markSynced(r.getId(), match.id);
                }
            }

            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
