package drivesafe;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drivesafe.R; // ← 匯入正確的 R
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AccountManageActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manage); // 你的帳號管理 XML

        MaterialToolbar toolbar = findViewById(R.id.accountToolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        etUsername        = findViewById(R.id.etUsername);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        MaterialButton btnSave = findViewById(R.id.btnSaveAccount);

        // 載入目前帳號至畫面
        etUsername.setText(AuthStore.getUser(this));
        etPassword.setText(AuthStore.getPass(this));
        etConfirmPassword.setText(AuthStore.getPass(this));

        btnSave.setOnClickListener(v -> {
            String user = text(etUsername);
            String pass = text(etPassword);
            String pass2= text(etConfirmPassword);

            if (TextUtils.isEmpty(user)) { toast("請輸入帳號"); return; }
            if (TextUtils.isEmpty(pass)) { toast("請輸入新密碼"); return; }
            if (!pass.equals(pass2))    { toast("兩次密碼不一致"); return; }

            AuthStore.save(this, user, pass);
            toast("已儲存。下次登入請用新的帳密");
            finish(); // 回到設定頁
        });
    }

    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
