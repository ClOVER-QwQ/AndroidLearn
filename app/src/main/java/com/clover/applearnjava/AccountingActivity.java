package com.clover.applearnjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class AccountingActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> accountList;
    private final ArrayList<Long> accountIds = new ArrayList<>();
    private int userId;
    private ActivityResultLauncher<Intent> addAccountResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting);

        dbHelper = new DatabaseHelper(this);
        ListView listView = findViewById(R.id.listView);
        Button addAccountButton = findViewById(R.id.addAccountButton);
        Button backButton = findViewById(R.id.backButton);

        userId = getIntent().getIntExtra("user_id", -1);

        accountList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, accountList);
        listView.setAdapter(adapter);

        loadAccounts();

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < accountIds.size()) {
                final long accountId = accountIds.get(position);
                new AlertDialog.Builder(AccountingActivity.this)
                        .setTitle("删除账单")
                        .setMessage("是否删除该账单？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            int rowsDeleted = db.delete(DatabaseHelper.TABLE_ACCOUNTS,
                                    DatabaseHelper.COLUMN_ACCOUNT_ID + " = ?",
                                    new String[]{String.valueOf(accountId)});
                            db.close();
                            if (rowsDeleted > 0) {
                                loadAccounts(); // 重新加载数据，更新列表和ID集合
                                Toast.makeText(AccountingActivity.this, "账单已删除", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AccountingActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
            return false;
        });

        // 注册添加账单结果观察者
        addAccountResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String category = data.getStringExtra("category");
                            double amount = data.getDoubleExtra("amount", 0.0);

                            SQLiteDatabase writeDb = dbHelper.getWritableDatabase();
                            writeDb.execSQL("INSERT INTO " + DatabaseHelper.TABLE_ACCOUNTS + " (" + DatabaseHelper.COLUMN_USER_ID_FOREIGN + ", " + DatabaseHelper.COLUMN_CATEGORY + ", " + DatabaseHelper.COLUMN_AMOUNT + ") VALUES (?, ?, ?)",
                                    new Object[]{userId, category, amount});
                            writeDb.close();

                            loadAccounts();
                        }
                    }
                });

        addAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountingActivity.this, AddAccountActivity.class);
            intent.putExtra("user_id", userId);
            addAccountResultLauncher.launch(intent);
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountingActivity.this, ProfileActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        });
    }

    private void loadAccounts() {
        accountList.clear();
        accountIds.clear(); // 清空ID列表
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ACCOUNTS,
                null,
                DatabaseHelper.COLUMN_USER_ID_FOREIGN + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        while (cursor.moveToNext()) {
            @SuppressLint("Range") long accountId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ACCOUNT_ID));
            @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY));
            @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_AMOUNT));
            accountIds.add(accountId); // 添加ID到列表
            accountList.add("类别: " + category + ", 金额: " + amount);
        }

        cursor.close();
        db.close();
        adapter.notifyDataSetChanged();
    }
}