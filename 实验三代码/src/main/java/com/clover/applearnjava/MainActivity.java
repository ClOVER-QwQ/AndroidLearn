package com.clover.applearnjava;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Contact> contacts = new ArrayList<>();
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化数据
        contacts.add(new Contact("张三", "同事"));
        contacts.add(new Contact("李四", "朋友"));
        contacts.add(new Contact("王五", "家人"));

        // 设置适配器
        ListView listViewContacts = findViewById(R.id.listViewContacts);
        adapter = new ContactAdapter(this, contacts);
        listViewContacts.setAdapter(adapter);

        // 点击事件
        listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = contacts.get(position);
                Toast.makeText(MainActivity.this, contact.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // 长按事件
        listViewContacts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int deletePosition = position;
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("提示")
                        .setMessage("确定要删除这个联系人吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                contacts.remove(deletePosition);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
        });

        // 添加按钮点击事件
        Button btnAddContact = findViewById(R.id.btnAddContact);
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View dialogView = inflater.inflate(R.layout.add_contact_dialog, null);

                final EditText etName = dialogView.findViewById(R.id.etName);
                final EditText etDescription = dialogView.findViewById(R.id.etDescription);

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("添加联系人")
                        .setView(dialogView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = etName.getText().toString();
                                String description = etDescription.getText().toString();
                                if (!name.isEmpty()) {
                                    contacts.add(new Contact(name, description));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
    }
}