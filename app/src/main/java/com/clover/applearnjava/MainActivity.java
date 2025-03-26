package com.clover.applearnjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private ActivityResultLauncher<Intent> loginResultLauncher;
    private ActivityResultLauncher<Intent> registerResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        // 检查是否已经登录
        if (isLoggedIn()) {
            int userId = getUserIdFromPrefs();
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
            return;
        }

        // 注册登录结果观察者
        loginResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            int userId = data.getIntExtra("user_id", -1);
                            saveLoginState(userId);
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            intent.putExtra("user_id", userId);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

        // 注册注册结果观察者
        registerResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String username = data.getStringExtra("username");
                            String password = data.getStringExtra("password");
                            Toast.makeText(MainActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                            usernameInput.setText(username);
                            passwordInput.setText(password);
                        }
                    }
                });

        // 登录按钮点击事件
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_USER_ID},
                    DatabaseHelper.COLUMN_USERNAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?",
                    new String[]{username, password},
                    null, null, null);

            if (cursor.moveToFirst()) {
                @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
                saveLoginState(userId);
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("user_id", userId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "用户名或密码错误！", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        });

        // 注册按钮点击事件
        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_USER_ID},
                    DatabaseHelper.COLUMN_USERNAME + " = ?",
                    new String[]{username},
                    null, null, null);

            if (cursor.getCount() > 0) {
                Toast.makeText(MainActivity.this, "用户名已存在！", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }

            cursor.close();

            SQLiteDatabase writeDb = dbHelper.getWritableDatabase();
            writeDb.execSQL("INSERT INTO " + DatabaseHelper.TABLE_USERS + " (" + DatabaseHelper.COLUMN_USERNAME + ", " + DatabaseHelper.COLUMN_PASSWORD + ") VALUES (?, ?)",
                    new Object[]{username, password});

            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("password", password);
            registerResultLauncher.launch(intent);
        });
    }

    private boolean isLoggedIn() {
        return getSharedPreferences("login_prefs", MODE_PRIVATE)
                .getBoolean("is_logged_in", false);
    }

    private int getUserIdFromPrefs() {
        return getSharedPreferences("login_prefs", MODE_PRIVATE)
                .getInt("user_id", -1);
    }

    private void saveLoginState(int userId) {
        getSharedPreferences("login_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", true)
                .putInt("user_id", userId)
                .apply();
    }

}