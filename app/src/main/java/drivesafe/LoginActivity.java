package drivesafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.drivesafe.R;
import com.example.drivesafe.BuildConfig;
import com.patrick.main.MainActivity;

import drivesafe.net.ApiClient;
import com.example.drivesafe.db.ApiService;
import com.example.drivesafe.db.LoginReq;
import com.example.drivesafe.db.LoginResp;
import com.example.drivesafe.db.MemberDto;
import drivesafe.db.TokenStore;
// 若有用到預設偏好實作，取消下一行的註解：
// import drivesafe.db.PrefsTokenStore;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private CheckBox checkboxRemember;

    // 偏好儲存鍵值
    private static final String PREF_NAME          = "login_prefs";
    private static final String KEY_REMEMBER       = "remember";
    private static final String KEY_EMAIL          = "email";
    private static final String KEY_USER_ID        = "user_id";         // 以 email 作為 userId
    private static final String KEY_MEMBER_ID_INT  = "member_id_int";   // ★ 新增：雲端整數 member_id

    // 管理員密碼
    private static final String ADMIN_PASS = "2468";

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ★ 確保預設帳密已種入（c111193243@nkust.edu.tw / 1234）
        AuthStore.seedDefaultsIfEmpty(this);

        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        checkboxRemember = findViewById(R.id.checkboxRemember);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        // 管理員卡片 → 先輸入管理員密碼再進 AdminActivity
        MaterialCardView cardAdmin = findViewById(R.id.cardAdmin);
        if (cardAdmin != null) {
            cardAdmin.setOnClickListener(v -> showAdminLoginDialog());
        }

        sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // 讀「是否記住」與「上次 Email」
        boolean remembered = sp.getBoolean(KEY_REMEMBER, false);
        String savedEmail  = sp.getString(KEY_EMAIL, "");

        checkboxRemember.setChecked(remembered);

        // 若有記住 → 顯示記住的 Email；否則自動填入預設帳號
        if (remembered && !savedEmail.isEmpty()) {
            etEmail.setText(savedEmail);
            etEmail.setSelection(savedEmail.length());
        } else {
            String defaultUser = AuthStore.getUser(this); // 會回預設帳號
            etEmail.setText(defaultUser);
            etEmail.setSelection(defaultUser.length());
        }

        // 登入：優先打雲端，三種端點皆嘗試；都失敗才回退本地 AuthStore
        btnLogin.setOnClickListener(v -> {
            String user = String.valueOf(etEmail.getText()).trim();
            String pass = String.valueOf(etPassword.getText()).trim();

            if (user.isEmpty()) {
                Toast.makeText(this, "請輸入帳號", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass.isEmpty()) {
                Toast.makeText(this, "請輸入密碼", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            tryLoginMembers(user, pass);
        });
    }

    /** 依序嘗試：/members/login（JSON）→ /login（JSON）→ /token（form） */
    private void tryLoginMembers(String user, String pass) {
        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);

        LoginReq req = new LoginReq();
        req.email = user;     // 若後端用 username，將 LoginReq 改成 username 欄位並填入 user
        req.password = pass;

        api.loginMembers(req).enqueue(new retrofit2.Callback<LoginResp>() {
            @Override public void onResponse(retrofit2.Call<LoginResp> call,
                                             retrofit2.Response<LoginResp> resp) {
                String token = getTokenFromResp(resp.body());
                if (resp.isSuccessful() && token != null) {
                    handleLoginSuccess(user, token);
                } else {
                    tryLoginJson(user, pass);
                }
            }
            @Override public void onFailure(retrofit2.Call<LoginResp> call, Throwable t) {
                tryLoginJson(user, pass);
            }
        });
    }

    private void tryLoginJson(String user, String pass) {
        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);

        LoginReq req = new LoginReq();
        req.email = user;
        req.password = pass;

        api.loginJson(req).enqueue(new retrofit2.Callback<LoginResp>() {
            @Override public void onResponse(retrofit2.Call<LoginResp> call,
                                             retrofit2.Response<LoginResp> resp) {
                String token = getTokenFromResp(resp.body());
                if (resp.isSuccessful() && token != null) {
                    handleLoginSuccess(user, token);
                } else {
                    tryLoginForm(user, pass);
                }
            }
            @Override public void onFailure(retrofit2.Call<LoginResp> call, Throwable t) {
                tryLoginForm(user, pass);
            }
        });
    }

    private void tryLoginForm(String user, String pass) {
        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);

        api.loginForm(user, pass).enqueue(new retrofit2.Callback<LoginResp>() {
            @Override public void onResponse(retrofit2.Call<LoginResp> call,
                                             retrofit2.Response<LoginResp> resp) {
                String token = getTokenFromResp(resp.body());
                if (resp.isSuccessful() && token != null) {
                    handleLoginSuccess(user, token);
                } else {
                    tryLocalFallback(user, pass);
                }
            }
            @Override public void onFailure(retrofit2.Call<LoginResp> call, Throwable t) {
                tryLocalFallback(user, pass);
            }
        });
    }

    /** 從 LoginResp 取 token（你的後端可能是 access_token 或 token） */
    private String getTokenFromResp(LoginResp body) {
        if (body == null) return null;
        if (body.access_token != null && !body.access_token.isEmpty()) return body.access_token;
        try {
            java.lang.reflect.Field f = body.getClass().getField("token");
            Object v = f.get(body);
            if (v instanceof String && !((String) v).isEmpty()) return (String) v;
        } catch (Exception ignore) {}
        return null;
    }

    /** 本地 AuthStore 驗證（離線或雲端全部失敗時的備援） */
    private void tryLocalFallback(String user, String pass) {
        if (AuthStore.check(this, user, pass)) {
            Toast.makeText(this, "離線模式登入成功（本地）", Toast.LENGTH_SHORT).show();
            rememberEmail(user);
            proceedToMain(user);
        } else {
            Toast.makeText(this, "登入失敗（伺服器或本地都無法驗證）", Toast.LENGTH_LONG).show();
            MaterialButton btnLogin = findViewById(R.id.btnLogin);
            btnLogin.setEnabled(true);
        }
    }

    /** ★ 登入成功：存 token → 透過 /members 以 email 對應 member_id → 存起來 → 進主頁 */
    private void handleLoginSuccess(String user, String token) {
        // 1) 先存 token（之後要帶 Authorization 再取出用）
        new TokenStore(getApplicationContext()).save(token);

        // 2) 查 /members 取得 member_id 並保存；不管成功與否最後都進主頁
        ApiService api = ApiClient.get(BuildConfig.BASE_URL).create(ApiService.class);
        api.getMembers().enqueue(new retrofit2.Callback<java.util.List<MemberDto>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<MemberDto>> call,
                                   retrofit2.Response<java.util.List<MemberDto>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    Integer memberId = null;
                    for (MemberDto m : resp.body()) {
                        if (m != null && m.email != null && m.email.equalsIgnoreCase(user)) {
                            memberId = m.id;
                            break;
                        }
                    }
                    if (memberId != null) {
                        sp.edit().putInt(KEY_MEMBER_ID_INT, memberId).apply();
                    }
                }
                // 3) 記住 email → 進主頁
                rememberEmail(user);
                Toast.makeText(getApplicationContext(), "登入成功", Toast.LENGTH_SHORT).show();
                proceedToMain(user);
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<MemberDto>> call, Throwable t) {
                // 查不到也不擋登入
                rememberEmail(user);
                Toast.makeText(getApplicationContext(), "登入成功", Toast.LENGTH_SHORT).show();
                proceedToMain(user);
            }
        });
    }

    /** 記住帳號 or 清除；同時保存 userId 供其他頁使用 */
    private void rememberEmail(String user) {
        if (checkboxRemember.isChecked()) {
            sp.edit()
                    .putBoolean(KEY_REMEMBER, true)
                    .putString(KEY_EMAIL, user)
                    .putString(KEY_USER_ID, user)
                    .apply();
        } else {
            sp.edit()
                    .putBoolean(KEY_REMEMBER, false)
                    .remove(KEY_EMAIL)
                    .putString(KEY_USER_ID, user)
                    .apply();
        }
    }

    /** 進入主頁 */
    private void proceedToMain(String user) {
        Intent it = new Intent(this, MainActivity.class);
        it.putExtra("user_id", user);
        startActivity(it);
        finish();
    }

    /** 顯示管理員密碼對話框，正確才進 AdminActivity */
    private void showAdminLoginDialog() {
        final EditText et = new EditText(this);
        et.setHint("請輸入管理員密碼");
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("管理員登入")
                .setView(et)
                .setCancelable(true)
                .setPositiveButton("進入", null)
                .setNegativeButton("取消", null)
                .create();

        dlg.setOnShowListener(d -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String input = et.getText().toString().trim();
                if (ADMIN_PASS.equals(input)) {
                    dlg.dismiss();
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                } else {
                    et.setError("密碼錯誤");
                }
            });
        });

        dlg.show();
    }
}
