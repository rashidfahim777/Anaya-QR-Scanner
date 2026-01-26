package com.faheem.anayaqrscanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.OnSettingClickListener {

    private RecyclerView recyclerView;
    private SettingsAdapter adapter;
    private List<SettingItem> settingsList;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefManager = new SharedPrefManager(this);
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadSettings();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupRecyclerView() {
        settingsList = new ArrayList<>();
        adapter = new SettingsAdapter(this, settingsList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    private void loadSettings() {
        settingsList.clear();

        // Vibration Setting
        settingsList.add(new SettingItem(
                "Enable Vibration",
                "Vibrate on successful scan",
                SettingItem.TYPE_TOGGLE,
                prefManager.getVibrationEnabled()
        ));

        // Sound Setting
        settingsList.add(new SettingItem(
                "Enable Sound",
                "Play sound on successful scan",
                SettingItem.TYPE_TOGGLE,
                prefManager.getSoundEnabled()
        ));

        // Privacy Policy
        settingsList.add(new SettingItem(
                "Privacy Policy",
                "View our privacy policy",
                SettingItem.TYPE_ARROW,
                false
        ));

        // About App
        settingsList.add(new SettingItem(
                "About App",
                "App information and features",
                SettingItem.TYPE_ARROW,
                false
        ));

        // Version Info
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            settingsList.add(new SettingItem(
                    "Version",
                    versionName,
                    SettingItem.TYPE_TEXT,
                    false
            ));
        } catch (Exception e) {
            settingsList.add(new SettingItem(
                    "Version",
                    "1.0",
                    SettingItem.TYPE_TEXT,
                    false
            ));
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSettingClick(int position) {
        SettingItem item = settingsList.get(position);

        switch (position) {
            case 2: // Privacy Policy
                openPrivacyPolicy();
                break;
            case 3: // About App
                showAboutDialog();
                break;
        }
    }

    @Override
    public void onToggleChanged(int position, boolean isChecked) {
        SettingItem item = settingsList.get(position);

        switch (position) {
            case 0: // Vibration
                prefManager.setVibrationEnabled(isChecked);
                break;
            case 1: // Sound
                prefManager.setSoundEnabled(isChecked);
                break;
        }

        // Update the item in the list
        item.setToggleState(isChecked);
    }

    private void openPrivacyPolicy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://anayakhan6193172-cloud.github.io/Privacypolicy/privacy-policy.html"));
        startActivity(intent);
    }

    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Anaya QR Scanner")
                .setMessage("A simple and efficient QR & Barcode scanner.\n\n" +
                        "Features:\n" +
                        "• Scan QR codes\n" +
                        "• Scan barcodes\n" +
                        "• Save scan history\n" +
                        "• Share results\n" +
                        "• Generate custom QR codes\n\n" +
                        "• Contact: Anayakhan6193172@gmail.com\n\n" +
                        "© 2026 Anaya IT Solutions")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}