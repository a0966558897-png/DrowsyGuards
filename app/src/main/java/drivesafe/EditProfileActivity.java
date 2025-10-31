package drivesafe;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drivesafe.R; // ← 加上你 app 的 R
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;


public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etAge, etPhone;

    public static final String PREFS = "profile_prefs";
    public static final String K_NAME  = "name";
    public static final String K_EMAIL = "email";
    public static final String K_AGE   = "age";
    public static final String K_PHONE = "phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Toolbar 返回
        MaterialToolbar tb = findViewById(R.id.editToolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        // 綁定元件
        etName  = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etAge   = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        MaterialButton btnSave = findViewById(R.id.btnSaveProfile);

        // 載入既有資料
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        etName.setText(sp.getString(K_NAME, "王小明"));
        etEmail.setText(sp.getString(K_EMAIL, "xiaoming@example.com"));
        etAge.setText(String.valueOf(sp.getInt(K_AGE, 30)));
        etPhone.setText(sp.getString(K_PHONE, "0912-345678"));

        // 儲存資料
        btnSave.setOnClickListener(v -> {
            String name = getStr(etName);
            String email = getStr(etEmail);
            String ageStr = getStr(etAge);
            String phone = getStr(etPhone);

            if (TextUtils.isEmpty(name))  { toast("請輸入姓名"); return; }
            if (TextUtils.isEmpty(email)) { toast("請輸入 Email"); return; }
            if (TextUtils.isEmpty(ageStr)){ toast("請輸入年齡"); return; }

            int age;
            try { age = Integer.parseInt(ageStr); }
            catch (NumberFormatException e) { toast("年齡格式錯誤"); return; }

            sp.edit()
                    .putString(K_NAME, name)
                    .putString(K_EMAIL, email)
                    .putInt(K_AGE, age)
                    .putString(K_PHONE, phone)
                    .apply();

            toast("已儲存");
            finish();
        });
    }

    private String getStr(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
