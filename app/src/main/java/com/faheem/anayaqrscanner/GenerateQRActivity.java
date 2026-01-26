package com.faheem.anayaqrscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GenerateQRActivity extends AppCompatActivity implements QROptionAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private QROptionAdapter adapter;
    private List<QROption> qrOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Generate QR Code");
        }

        // Set back arrow click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);

        // Set GridLayoutManager with 4 columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);

        // Improve performance
        recyclerView.setHasFixedSize(true);

        // Initialize data
        initializeQROptions();

        // Set adapter
        adapter = new QROptionAdapter(this, qrOptions, this);
        recyclerView.setAdapter(adapter);
    }

    private void initializeQROptions() {
        qrOptions = new ArrayList<>();

        qrOptions.add(new QROption("Text", R.drawable.ic_text, QRCodeType.TEXT));
        qrOptions.add(new QROption("Wi-Fi", R.drawable.ic_wifi, QRCodeType.WIFI));
        qrOptions.add(new QROption("Website", R.drawable.ic_website, QRCodeType.URL));
        qrOptions.add(new QROption("VCard", R.drawable.ic_vcard, QRCodeType.VCARD));
        qrOptions.add(new QROption("Location", R.drawable.ic_location, QRCodeType.LOCATION));
        qrOptions.add(new QROption("Contact", R.drawable.ic_contact, QRCodeType.CONTACT));
        qrOptions.add(new QROption("Image", R.drawable.ic_image, QRCodeType.IMAGE));
        qrOptions.add(new QROption("Facebook", R.drawable.ic_facebook, QRCodeType.FACEBOOK));
        qrOptions.add(new QROption("LinkedIn", R.drawable.ic_linkedin, QRCodeType.LINKEDIN));
        qrOptions.add(new QROption("Instagram", R.drawable.ic_instagram, QRCodeType.INSTAGRAM));
        qrOptions.add(new QROption("WhatsApp", R.drawable.ic_whatsapp, QRCodeType.WHATSAPP));
        qrOptions.add(new QROption("Skype", R.drawable.ic_skype, QRCodeType.SKYPE));
        qrOptions.add(new QROption("X", R.drawable.ic_twitter, QRCodeType.TWITTER));
        qrOptions.add(new QROption("Snapchat", R.drawable.ic_snapchat, QRCodeType.SNAPCHAT));
        qrOptions.add(new QROption("Telegram", R.drawable.ic_telegram, QRCodeType.TELEGRAM));
        qrOptions.add(new QROption("YouTube", R.drawable.ic_youtube, QRCodeType.YOUTUBE));
        qrOptions.add(new QROption("WeChat", R.drawable.ic_wechat, QRCodeType.WECHAT));
        qrOptions.add(new QROption("Tumblr", R.drawable.ic_tumblr, QRCodeType.TUMBLR));
        qrOptions.add(new QROption("LINE", R.drawable.ic_line, QRCodeType.LINE));
        qrOptions.add(new QROption("Event", R.drawable.ic_event, QRCodeType.EVENT));
    }

    @Override
    public void onItemClick(int position) {
        QROption selectedOption = qrOptions.get(position);
        navigateToInputActivity(selectedOption);
    }

    private void navigateToInputActivity(QROption option) {
        Intent intent = new Intent(GenerateQRActivity.this, InputActivity.class);
        intent.putExtra("QR_TYPE", option.getType().name());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}