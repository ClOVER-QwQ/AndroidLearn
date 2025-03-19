package com.clover.applearnjava;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText phoneInput = findViewById(R.id.phoneInput);
        EditText emailInput = findViewById(R.id.emailInput);
        Button saveButton = findViewById(R.id.saveButton);

        // 获取从ProfileActivity传递的数据
        String username = getIntent().getStringExtra("username");
        String phone = getIntent().getStringExtra("phone");
        String email = getIntent().getStringExtra("email");

        usernameInput.setText(username);
        phoneInput.setText(phone);
        emailInput.setText(email);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedUsername = usernameInput.getText().toString().trim();
                String updatedPhone = phoneInput.getText().toString().trim();
                String updatedEmail = emailInput.getText().toString().trim();

                Intent intent = new Intent();
                intent.putExtra("updatedUsername", updatedUsername);
                intent.putExtra("updatedPhone", updatedPhone);
                intent.putExtra("updatedEmail", updatedEmail);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}