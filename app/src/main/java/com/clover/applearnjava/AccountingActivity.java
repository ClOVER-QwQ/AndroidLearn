package com.clover.applearnjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AccountingActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> accountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        listView = findViewById(R.id.listView);
        Button addAccountButton = findViewById(R.id.addAccountButton);
        Button backButton = findViewById(R.id.backButton); // 新增返回按钮

        accountList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, accountList);
        listView.setAdapter(adapter);

        // 查询所有账单
        loadAccounts();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final long accountId = id;
                new AlertDialog.Builder(AccountingActivity.this)
                        .setTitle("删除账单")
                        .setMessage("是否删除该账单？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            db.delete(DatabaseHelper.TABLE_ACCOUNTS, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(accountId)});
                            loadAccounts();
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
        });

        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountingActivity.this, AddAccountActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountingActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String category = data.getStringExtra("category");
            double amount = data.getDoubleExtra("amount", 0.0);
            db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_ACCOUNTS + " (" + DatabaseHelper.COLUMN_CATEGORY + ", " + DatabaseHelper.COLUMN_AMOUNT + ") VALUES (?, ?)",
                    new Object[]{category, amount});
            loadAccounts();
        }
    }

    private void loadAccounts() {
        accountList.clear();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ACCOUNTS, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY));
            @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_AMOUNT));
            accountList.add("类别: " + category + ", 金额: " + amount);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }
}