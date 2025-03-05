package com.clover.applearnjava;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 设置 WindowInsets 监听器
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 获取用户名和密码输入框
        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);

        // 获取登录按钮
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            // 获取用户输入的用户名和密码
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // 打印调试信息
            Log.d("MainActivity", "Username: " + username);
            Log.d("MainActivity", "Password: " + password);

            // 检查用户名和密码是否符合要求
            if ("clover".equals(username) && "123456".equals(password)) {
                Log.d("MainActivity", "Showing Toast for successful login");
                Toast.makeText(MainActivity.this, "登录成功！",Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Showing Toast for failed login");
                Toast.makeText(MainActivity.this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
            }
        });
    }
}