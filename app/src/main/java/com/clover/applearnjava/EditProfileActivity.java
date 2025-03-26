package com.clover.applearnjava;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView avatarImage;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private String currentAvatarPath;
    private int userId;

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
        Button saveButton = findViewById(R.id.saveButton);

        userId = getIntent().getIntExtra("user_id", -1);
        loadUserData(usernameInput, phoneInput, emailInput);

        // 初始化权限请求
        setupPermissionLauncher();
        // 初始化图片选择器
        setupImagePicker();

        btnChangeAvatar.setOnClickListener(v -> checkPermissionAndPickImage());

        saveButton.setOnClickListener(v -> {
            // 确保有头像更新时保存路径
            if (currentAvatarPath != null) {
                saveUserData(usernameInput, phoneInput, emailInput);
            } else {
                Toast.makeText(this, "请先选择头像", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData(EditText username, EditText phone, EditText email) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
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
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                // 添加空值检查
                String avatarUri = cursor.getString(3);
                if (!cursor.isNull(3)) {
                    currentAvatarPath = avatarUri; // 保持当前路径同步
                }

                username.setText(cursor.getString(0));
                phone.setText(cursor.getString(1));
                email.setText(cursor.getString(2));
                loadAvatarImage(avatarUri);
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "数据库查询失败", e);
        }
    }

    private void loadAvatarImage(String avatarUri) {
        if (avatarUri != null && !avatarUri.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(avatarUri))
                    .circleCrop()
                    .into(avatarImage);
        }
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        handlePermissionDenied();
                    }
                });
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            updateAvatar(selectedImageUri);
                        }
                    }
                });
    }

    private void checkPermissionAndPickImage() {
        String requiredPermission = getRequiredPermission();

        if (ContextCompat.checkSelfPermission(this, requiredPermission)
                == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(requiredPermission);
        }
    }

    @NonNull
    private String getRequiredPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES :
                Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    private void handlePermissionDenied() {
        if (shouldShowRequestPermissionRationale(getRequiredPermission())) {
            Toast.makeText(this,
                    "需要存储权限才能选择头像",
                    Toast.LENGTH_SHORT).show();
        } else {
            showPermissionSettingsDialog();
        }
    }

    private void showPermissionSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("权限需要")
                .setMessage("您已永久拒绝存储权限，请在设置中手动开启")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            pickImageLauncher.launch(intent);
        } else {
            Toast.makeText(this,
                    "未找到可用的相册应用",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAvatar(Uri imageUri) {
        if (imageUri != null) {
            currentAvatarPath = imageUri.toString();
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.default_avatar) // 添加默认图
                    .circleCrop()
                    .into(avatarImage);
        }
    }

    private void saveUserData(EditText username, EditText phone, EditText email) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username.getText().toString().trim());
        values.put(DatabaseHelper.COLUMN_PHONE, phone.getText().toString().trim());
        values.put(DatabaseHelper.COLUMN_EMAIL, email.getText().toString().trim());

        // 确保头像路径保存（新增空值检查）
        if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
            values.put(DatabaseHelper.COLUMN_AVATAR, currentAvatarPath);
        } else {
            Toast.makeText(this, "头像路径无效", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );

            if (rowsAffected > 0) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // 通知父界面刷新
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "数据库更新失败", e);
            Toast.makeText(this, "保存时发生错误", Toast.LENGTH_SHORT).show();
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("user_id", userId);
        resultIntent.putExtra("updatedAvatar", currentAvatarPath);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当返回界面时重新加载数据
        if (userId != -1) {
            loadUserData(
                    findViewById(R.id.usernameInput),
                    findViewById(R.id.phoneInput),
                    findViewById(R.id.emailInput)
            );
        }
    }
}