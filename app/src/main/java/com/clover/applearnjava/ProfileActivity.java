package com.clover.applearnjava;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "login_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView phoneTextView = findViewById(R.id.phoneTextView);
        TextView emailTextView = findViewById(R.id.emailTextView);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button editButton = findViewById(R.id.editButton);
        Button accountingButton = findViewById(R.id.accountingButton); // 新增按钮

        // 从SharedPreferences中读取用户信息
        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        String username = prefs.getString("username", "未知用户");
        String phone = prefs.getString("phone", "未设置");
        String email = prefs.getString("email", "未设置");

        usernameTextView.setText("用户名: " + username);
        phoneTextView.setText("手机号: " + phone);
        emailTextView.setText("邮箱: " + email);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("phone", phone);
                intent.putExtra("email", email);
                startActivityForResult(intent, 1);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 更新登录状态为未登录
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(KEY_IS_LOGGED_IN, false);
                editor.apply();

                // 跳转到登录页面
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        accountingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AccountingActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String updatedUsername = data.getStringExtra("updatedUsername");
            String updatedPhone = data.getStringExtra("updatedPhone");
            String updatedEmail = data.getStringExtra("updatedEmail");

            TextView usernameTextView = findViewById(R.id.usernameTextView);
            TextView phoneTextView = findViewById(R.id.phoneTextView);
            TextView emailTextView = findViewById(R.id.emailTextView);

            usernameTextView.setText("用户名: " + updatedUsername);
            phoneTextView.setText("手机号: " + updatedPhone);
            emailTextView.setText("邮箱: " + updatedEmail);

            // 更新SharedPreferences中的数据
            SharedPreferences prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", updatedUsername);
            editor.putString("phone", updatedPhone);
            editor.putString("email", updatedEmail);
            editor.apply();
        }
    }
}