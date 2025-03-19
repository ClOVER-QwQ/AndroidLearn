package com.clover.applearnjava;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class AddAccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        EditText categoryInput = findViewById(R.id.categoryInput);
        EditText amountInput = findViewById(R.id.amountInput);
        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = categoryInput.getText().toString().trim();
                double amount = Double.parseDouble(amountInput.getText().toString().trim());

                Intent intent = new Intent();
                intent.putExtra("category", category);
                intent.putExtra("amount", amount);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}