package com.clover.applearnjava;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "login_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);

        // 检查是否已经登录
        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        if (isLoggedIn) {
            // 如果已经登录，直接跳转到个人信息页面
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 从SharedPreferences中读取上次输入的用户名和密码
        String savedUsername = prefs.getString(KEY_USERNAME, "");
        String savedPassword = prefs.getString(KEY_PASSWORD, "");
        usernameInput.setText(savedUsername);
        passwordInput.setText(savedPassword);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 保存登录状态和用户名密码
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.apply();

            // 跳转到个人信息页面
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
}