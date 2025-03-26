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
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> editProfileResultLauncher;
    private int currentUserId = -1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 从SharedPreferences获取用户ID
        currentUserId = getSharedPreferences("login_prefs", MODE_PRIVATE)
                .getInt("user_id", -1);

        // 检查登录状态
        if (currentUserId == -1) {
            redirectToLogin();
            return;
        }

        initializeViews();
        setupResultLauncher();
        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 二次验证登录状态
        if (!isUserLoggedIn()) {
            redirectToLogin();
        }
    }

    private boolean isUserLoggedIn() {
        return getSharedPreferences("login_prefs", MODE_PRIVATE)
                .getBoolean("is_logged_in", false);
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void initializeViews() {
        findViewById(R.id.editButton).setOnClickListener(v -> openEditProfile());
        findViewById(R.id.logoutButton).setOnClickListener(v -> performLogout());
        findViewById(R.id.accountingButton).setOnClickListener(v -> openAccounting());
    }

    private void setupResultLauncher() {
        editProfileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        reloadUserData();
                    }
                });
    }

    private void loadUserData() {
        try (DatabaseHelper dbHelper = new DatabaseHelper(this);
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
                     new String[]{String.valueOf(currentUserId)},
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                updateUI(cursor);
            }
        }
    }

    @SuppressLint("Range")
    private void updateUI(Cursor cursor) {
        String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
        String phone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE));
        String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));
        String avatarPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_AVATAR));

        ((TextView) findViewById(R.id.usernameTextView)).setText("用户名: " + username);
        ((TextView) findViewById(R.id.phoneTextView)).setText("手机号: " + phone);
        ((TextView) findViewById(R.id.emailTextView)).setText("邮箱: " + email);

        loadAvatar(avatarPath);
    }

    private void loadAvatar(String path) {
        ImageView avatarImage = findViewById(R.id.avatarImage);
        if (path != null && !path.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(path))
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(avatarImage);
        } else {
            avatarImage.setImageResource(R.drawable.default_avatar);
        }
    }

    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("user_id", currentUserId);
        editProfileResultLauncher.launch(intent);
    }

    private void performLogout() {
        getSharedPreferences("login_prefs", MODE_PRIVATE).edit()
                .clear()
                .apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private void openAccounting() {
        Intent intent = new Intent(this, AccountingActivity.class);
        intent.putExtra("user_id", currentUserId);
        startActivity(intent);
    }

    private void reloadUserData() {
        currentUserId = getSharedPreferences("login_prefs", MODE_PRIVATE)
                .getInt("user_id", -1);
        loadUserData();
    }
}