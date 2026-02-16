package com.faheem.bouncybeak;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;
    public class MainMenuActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main_menu);

            Button startButton = findViewById(R.id.btn_start);
            Button settingsButton = findViewById(R.id.btn_settings);
            Button soundButton = findViewById(R.id.btn_sound);

            startButton.setOnClickListener(v -> {
                // Open GameActivity
                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
                startActivity(intent);
            });

            settingsButton.setOnClickListener(v -> {
                // TODO: Open settings screen
            });

            soundButton.setOnClickListener(v -> {
                // TODO: Toggle sounds
            });
        }
    }

