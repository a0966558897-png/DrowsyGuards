package drivesafe.db;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {
    private static final String PREF = "auth_prefs";
    private static final String KEY  = "access_token";

    private final SharedPreferences sp;

    public TokenStore(Context ctx) {
        this.sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void save(String token) { sp.edit().putString(KEY, token).apply(); }
    public String get() { return sp.getString(KEY, null); }
    public void clear() { sp.edit().remove(KEY).apply(); }
}
