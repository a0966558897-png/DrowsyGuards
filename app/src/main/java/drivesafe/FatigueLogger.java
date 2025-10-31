package drivesafe;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.drivesafe.db.AppDatabase;
import com.example.drivesafe.db.FatigueRecord;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 一行就能寫入疲勞紀錄的工具。
 * 用法：FatigueLogger.log(getApplicationContext(), score);
 */
public final class FatigueLogger {

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private FatigueLogger() {}

    /** 直接存一筆疲勞紀錄（時間=現在，synced=false） */
    public static void log(Context ctx, float score) {
        final long now = System.currentTimeMillis();
        final FatigueRecord r = new FatigueRecord(now, score);
        r.setSynced(false);

        IO.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(ctx.getApplicationContext());
            db.fatigueDao().insert(r);
            MAIN.post(() ->
                    Toast.makeText(ctx, "已記錄疲勞分數：" + String.format("%.2f", score), Toast.LENGTH_SHORT).show()
            );
        });
    }

    /** （可選）加條件：超過門檻才記錄，避免太多雜訊 */
    public static void logIfOver(Context ctx, float score, float threshold) {
        if (score >= threshold) {
            log(ctx, score);
        }
    }
}
