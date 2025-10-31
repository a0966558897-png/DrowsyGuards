package drivesafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drivesafe.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText etName, etEmail, etAge, etPhone, etPassword;
    MaterialButton btnRegister;
    TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 綁定元件
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // 註冊按鈕
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleRegister();
            }
        });

        // 返回登入
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 關閉這個畫面，返回登入
            }
        });
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();

        // 檢查欄位是否空白
        if (TextUtils.isEmpty(name)) {
            etName.setError("請輸入姓名");
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("請輸入正確的 Email");
            return;
        }

        if (TextUtils.isEmpty(ageStr)) {
            etAge.setError("請輸入年齡");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0) {
                etAge.setError("請輸入有效年齡");
                return;
            }
        } catch (NumberFormatException e) {
            etAge.setError("年齡需為數字");
            return;
        }

        if (TextUtils.isEmpty(phone) || phone.length() < 8) {
            etPhone.setError("請輸入有效電話");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("密碼需至少 6 字元");
            return;
        }

        // ✅ 模擬註冊成功（實作時可寫入 Room 或 Firebase）
        Toast.makeText(this, "註冊成功！", Toast.LENGTH_SHORT).show();

        // 跳回登入頁
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
