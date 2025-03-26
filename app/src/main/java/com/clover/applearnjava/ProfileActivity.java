package com.clover.applearnjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import java.util.concurrent.atomic.AtomicInteger;

public class ProfileActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> editProfileResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        AtomicInteger userId = new AtomicInteger(getIntent().getIntExtra("user_id", -1));

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_PHONE, DatabaseHelper.COLUMN_EMAIL},
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId.get())},
                null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));
            ImageView ImageView = findViewById(R.id.avatarImage);
            ((TextView) findViewById(R.id.usernameTextView)).setText("用户名: " + username);
            ((TextView) findViewById(R.id.phoneTextView)).setText("手机号: " + phone);
            ((TextView) findViewById(R.id.emailTextView)).setText("邮箱: " + email);
        } else {
            ((TextView) findViewById(R.id.usernameTextView)).setText("用户名: 未知用户");
            ((TextView) findViewById(R.id.phoneTextView)).setText("手机号: 未知号码");
            ((TextView) findViewById(R.id.emailTextView)).setText("邮箱: 未知邮箱");
        }

        cursor.close();
        db.close();

        // 注册编辑资料结果观察者
        editProfileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // 从数据库重新加载完整数据
                        reloadUserData(userId.get());
                    }
                }
        );

        findViewById(R.id.editButton).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("user_id", userId.get());
            editProfileResultLauncher.launch(intent);
        });

        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            getSharedPreferences("login_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_logged_in", false)
                    .putInt("user_id", -1)
                    .apply();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.accountingButton).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AccountingActivity.class);
            intent.putExtra("user_id", userId.get());
            startActivity(intent);
        });
    }

    // 新增方法：重新加载用户数据
    private void reloadUserData(int userId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.COLUMN_USERNAME,
                        DatabaseHelper.COLUMN_PHONE,
                        DatabaseHelper.COLUMN_EMAIL,
                        DatabaseHelper.COLUMN_AVATAR
                },
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));
            @SuppressLint("Range") String avatarPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_AVATAR));

            // 更新所有UI组件
            updateProfileViews(username, phone, email);
            loadAvatar(avatarPath);
        }
        cursor.close();
        db.close();
    }

    // 更新头像加载方法
    private void loadAvatar(String path) {
        if (path != null && !path.isEmpty()) {
            ImageView avatarImage = findViewById(R.id.avatarImage);
            Glide.with(this)
                    .load(Uri.parse(path))
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(avatarImage);
        }
    }

    // 新增方法：统一处理界面更新
    private void updateProfileViews(String username, String phone, String email) {
        TextView usernameView = findViewById(R.id.usernameTextView);
        TextView phoneView = findViewById(R.id.phoneTextView);
        TextView emailView = findViewById(R.id.emailTextView);

        usernameView.setText("用户名: " + (username != null ? username : ""));
        phoneView.setText("手机号: " + (phone != null ? phone : ""));
        emailView.setText("邮箱: " + (email != null ? email : ""));
    }
}