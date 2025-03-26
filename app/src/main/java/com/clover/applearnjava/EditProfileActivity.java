package com.clover.applearnjava;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;

public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView avatarImage;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private String currentAvatarPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 初始化视图
        avatarImage = findViewById(R.id.avatarImage);
        Button btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText phoneInput = findViewById(R.id.phoneInput);
        EditText emailInput = findViewById(R.id.emailInput);

        int userId = getIntent().getIntExtra("user_id", -1);

        // 加载现有数据（包含头像）
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.COLUMN_USERNAME,
                        DatabaseHelper.COLUMN_PHONE,
                        DatabaseHelper.COLUMN_EMAIL,
                        DatabaseHelper.COLUMN_AVATAR
                },
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            // 填充所有输入框
            usernameInput.setText(cursor.getString(0));
            phoneInput.setText(cursor.getString(1));
            emailInput.setText(cursor.getString(2));
            // ... 加载头像 ...
        }
        cursor.close();

        db.close();

        // 注册图片选择器
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            currentAvatarPath = selectedImageUri.toString();
                            // 使用 Glide 显示新头像
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .into(avatarImage);
                        }
                    }
                }
        );

        // 注册权限请求
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "需要存储权限才能选择头像", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 点击更换头像按钮
        btnChangeAvatar.setOnClickListener(v -> checkPermissionAndPickImage());

        // 保存按钮逻辑修改
        Button saveButton = findViewById(R.id.saveButton);
        // 保存按钮点击事件
        saveButton.setOnClickListener(v -> {
            String updatedUsername = usernameInput.getText().toString().trim();
            String updatedPhone = phoneInput.getText().toString().trim();
            String updatedEmail = emailInput.getText().toString().trim();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, updatedUsername);
            values.put(DatabaseHelper.COLUMN_PHONE, updatedPhone);
            values.put(DatabaseHelper.COLUMN_EMAIL, updatedEmail);
            values.put(DatabaseHelper.COLUMN_AVATAR, currentAvatarPath);

            // 更新数据库（包含头像路径）
            SQLiteDatabase writeDb = dbHelper.getWritableDatabase();
            writeDb.update(DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)});
            writeDb.close();

            // 返回结果
            Intent resultIntent = new Intent();
            resultIntent.putExtra("user_id", userId);
            resultIntent.putExtra("updatedAvatar", currentAvatarPath); // 返回新头像路径
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            // 请求权限
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            currentAvatarPath = selectedImageUri.toString();
            // 显示新头像
            Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(avatarImage);
        }
    }
}