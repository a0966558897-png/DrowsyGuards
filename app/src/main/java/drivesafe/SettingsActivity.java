package drivesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;
import com.example.drivesafe.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Toolbar 返回鍵
        MaterialToolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 背景音效開關（如需記憶狀態，可之後用 SharedPreferences 儲存）
        switchSound = findViewById(R.id.switchSound);
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(
                    this,
                    isChecked ? "背景音效開啟" : "背景音效關閉",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // 個人資料
        MaterialCardView cardProfile = findViewById(R.id.cardProfile);
        if (cardProfile != null) {
            cardProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class))
            );
        }

        // 帳號管理
        MaterialCardView cardAccountManage = findViewById(R.id.cardAccountManage);
        if (cardAccountManage != null) {
            cardAccountManage.setOnClickListener(v ->
                    startActivity(new Intent(this, AccountManageActivity.class))
            );
        }

        // 登出（保留記住的 Email：不清除 login_prefs）
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // 若你有自己的登入狀態（例如 JWT、Room、AuthStore），在這裡清除「會話狀態」即可，
            // 不要去動到 LoginActivity 使用的 login_prefs（remember / email）。
            // 例如：
            // AuthStore.signOut(this); // 僅清除 token / 當前使用者，別刪偏好 remember/email

            Intent toLogin = new Intent(this, LoginActivity.class);
            // 清掉返回堆疊，避免按返回鍵回到已登入頁面
            toLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(toLogin);
            // 不使用 finishAffinity() 也可，因為上面旗標已經清掉任務堆疊
            // finishAffinity();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
