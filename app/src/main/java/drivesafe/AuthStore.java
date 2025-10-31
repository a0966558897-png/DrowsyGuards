package drivesafe;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class AuthStore {
    private static final String PREFS  = "auth_prefs";
    private static final String K_USER = "user";
    private static final String K_PASS = "pass";

    // 你要的預設帳密
    public static final String DEFAULT_USER = "c111193243@nkust.edu.tw";
    public static final String DEFAULT_PASS = "1234";

    /** 第一次啟動時，如果還沒存過，就寫入預設帳密 */
    public static void seedDefaultsIfEmpty(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!sp.contains(K_USER) || !sp.contains(K_PASS)) {
            save(ctx, DEFAULT_USER, DEFAULT_PASS);
        }
    }

    /** 儲存帳密（覆蓋） */
    public static void save(Context ctx, String user, String pass) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putString(K_USER, user)
                .putString(K_PASS, pass)
                .apply();
    }

    /** 重置為預設帳密（需要時可呼叫） */
    public static void resetToDefaults(Context ctx) {
        save(ctx, DEFAULT_USER, DEFAULT_PASS);
    }

    /** 取得目前儲存帳號（若沒有就回預設） */
    public static String getUser(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(K_USER, DEFAULT_USER);
    }

    /** 取得目前儲存密碼（若沒有就回預設） */
    public static String getPass(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(K_PASS, DEFAULT_PASS);
    }

    /** 檢查帳密：Email 不分大小寫；密碼區分大小寫。
     *  重點：即使偏好裡存了別的帳密，也**允許**預設那組登入。 */
    public static boolean check(Context ctx, String user, String pass) {
        String savedUser = getUser(ctx);
        String savedPass = getPass(ctx);

        String inputUser = user == null ? "" : user.trim().toLowerCase(Locale.ROOT);
        String inputPass = pass == null ? "" : pass.trim();

        boolean matchSaved =
                savedUser != null
                        && savedUser.trim().toLowerCase(Locale.ROOT).equals(inputUser)
                        && savedPass != null
                        && savedPass.equals(inputPass);

        boolean matchDefault =
                DEFAULT_USER.toLowerCase(Locale.ROOT).equals(inputUser)
                        && DEFAULT_PASS.equals(inputPass);

        return matchSaved || matchDefault;
    }
}
