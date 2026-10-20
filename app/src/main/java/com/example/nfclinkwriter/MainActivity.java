package com.example.nfclinkwriter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button writeButton = findViewById(R.id.btnWrite);
        Button readButton = findViewById(R.id.btnRead);

        writeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WriteActivity.class);
            startActivity(intent);
        });

        readButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReadActivity.class);
            startActivity(intent);
        });
    }
}
