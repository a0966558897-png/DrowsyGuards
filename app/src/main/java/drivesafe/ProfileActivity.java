package drivesafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.drivesafe.R; // ← 加上你 app 的 R
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvAge, tvPhone;
    private MaterialButton btnEditProfile;

    public static final String PREFS = "profile_prefs";
    public static final String K_NAME  = "name";
    public static final String K_EMAIL = "email";
    public static final String K_AGE   = "age";
    public static final String K_PHONE = "phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar 返回
        MaterialToolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 綁定元件
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvAge = findViewById(R.id.tvAge);
        tvPhone = findViewById(R.id.tvPhone);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // 按下編輯 → 開啟 EditProfileActivity
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    // 每次返回都重新載入資料
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String name = sp.getString(K_NAME, "王小明");
        String email = sp.getString(K_EMAIL, "xiaoming@example.com");
        int age = sp.getInt(K_AGE, 30);
        String phone = sp.getString(K_PHONE, "0912-345678");

        tvName.setText("姓名：" + name);
        tvEmail.setText("Email：" + email);
        tvAge.setText("年齡：" + age);
        tvPhone.setText("電話：" + phone);
    }

    // 系統 Up 鍵支援
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
