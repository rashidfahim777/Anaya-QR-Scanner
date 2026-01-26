package com.faheem.anayaqrscanner;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InputActivity extends AppCompatActivity {

    private LinearLayout layoutFields;
    private MaterialCardView layoutQrContainer; // Changed from LinearLayout to MaterialCardView
    private Button btnGenerate, btnDownload, btnShare;
    private ImageView ivQRCode;
    private QRCodeType qrType;
    private Bitmap qrBitmap;
    private Toolbar toolbar;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // 1. INITIALIZE TOOLBAR FIRST
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // This is CRITICAL

        // Get QR type from intent
        String typeStr = getIntent().getStringExtra("QR_TYPE");
        qrType = QRCodeType.valueOf(typeStr);

        // 2. SET UP ACTIONBAR WITH BACK BUTTON
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Generate " + qrType.name() + " QR Code");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 3. SET CLICK LISTENER FOR BACK ARROW
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Initialize other views
        layoutFields = findViewById(R.id.layoutFields);
        layoutQrContainer = findViewById(R.id.layoutQrContainer); // This is now a MaterialCardView
        btnGenerate = findViewById(R.id.btnGenerate);
        btnDownload = findViewById(R.id.btnDownload);
        btnShare = findViewById(R.id.btnShare);
        ivQRCode = findViewById(R.id.ivQRCode);

        // REMOVED: btnBack references since it doesn't exist in XML

        setupInputFields();

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQRCode();
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadQRCode();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareQRCode();
            }
        });
    }

    // 4. ADD THIS METHOD TO HANDLE BACK BUTTON FROM MENU
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 5. OPTIONAL: ADD ANIMATION TO BACK PRESS
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Add animation if you want
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void setupInputFields() {
        layoutFields.removeAllViews();

        switch (qrType) {
            case TEXT:
                addEditText("Enter text", "text");
                break;

            case WIFI:
                addEditText("Wi-Fi Network Name (SSID)", "ssid");
                addEditText("Password", "password");
                break;

            case URL:
                addEditText("Website URL", "url", "https://");
                break;

            case VCARD:
                addEditText("Full Name", "name");
                addEditText("Phone", "phone");
                addEditText("Email", "email");
                break;

            case LOCATION:
                addEditText("Latitude", "lat");
                addEditText("Longitude", "lng");
                break;

            case CONTACT:
                addEditText("Name", "contact_name");
                addEditText("Phone", "contact_phone");
                break;

            case FACEBOOK:
                addEditText("Facebook Username or Profile URL", "facebook");
                break;

            case INSTAGRAM:
                addEditText("Instagram Username", "instagram");
                break;

            case TWITTER:
                addEditText("X (Twitter) Username", "twitter");
                break;

            case LINKEDIN:
                addEditText("LinkedIn Profile URL", "linkedin");
                break;

            case WHATSAPP:
                addEditText("Phone Number (with country code)", "whatsapp", "+");
                break;

            case SKYPE:
                addEditText("Skype Username", "skype");
                break;

            case TELEGRAM:
                addEditText("Telegram Username", "telegram");
                break;

            case YOUTUBE:
                addEditText("YouTube URL (Channel or Video)", "youtube");
                break;

            case WECHAT:
                addEditText("WeChat ID", "wechat");
                break;

            case TUMBLR:
                addEditText("Tumblr URL", "tumblr");
                break;

            case LINE:
                addEditText("LINE ID", "line");
                break;

            case EVENT:
                addEditText("Event Title", "event_title");
                addEditText("Start Date", "event_date");
                addEditText("Location", "event_location");
                break;

            default:
                addEditText("Enter content", "content");
        }
    }

    private void addEditText(String hint, String tag) {
        addEditText(hint, tag, "");
    }

    private void addEditText(String hint, String tag, String defaultText) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setTag(tag);
        if (!defaultText.isEmpty()) {
            editText.setText(defaultText);
        }
        editText.setPadding(20, 16, 20, 16);
        editText.setTextSize(16);

        // Set background for better visual
        editText.setBackgroundResource(R.drawable.edittext_background);

        // Set margins between EditText fields
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16); // Bottom margin of 16dp
        editText.setLayoutParams(params);

        layoutFields.addView(editText);
    }

    private String getInputValue(String tag) {
        for (int i = 0; i < layoutFields.getChildCount(); i++) {
            View view = layoutFields.getChildAt(i);
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                if (tag.equals(editText.getTag())) {
                    return editText.getText().toString().trim();
                }
            }
        }
        return "";
    }

    private void generateQRCode() {
        String qrContent = formatQRContent();

        if (qrContent.isEmpty()) {
            Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            ivQRCode.setImageBitmap(qrBitmap);
            layoutQrContainer.setVisibility(View.VISIBLE);

            // Enable download and share buttons
            btnDownload.setEnabled(true);
            btnShare.setEnabled(true);

            Toast.makeText(this, "QR Code Generated!", Toast.LENGTH_SHORT).show();

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatQRContent() {
        switch (qrType) {
            case TEXT:
                return getInputValue("text");

            case WIFI:
                String ssid = getInputValue("ssid");
                String password = getInputValue("password");
                if (ssid.isEmpty()) return "";
                return String.format("WIFI:S:%s;T:WPA;P:%s;;", ssid, password);

            case URL:
                String url = getInputValue("url");
                if (url.isEmpty()) return "";
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                return url;

            case VCARD:
                String name = getInputValue("name");
                String phone = getInputValue("phone");
                String email = getInputValue("email");
                if (name.isEmpty() || phone.isEmpty()) return "";
                return String.format("BEGIN:VCARD\nVERSION:3.0\nFN:%s\nTEL:%s\nEMAIL:%s\nEND:VCARD",
                        name, phone, email.isEmpty() ? "" : email);

            case LOCATION:
                String lat = getInputValue("lat");
                String lng = getInputValue("lng");
                if (lat.isEmpty() || lng.isEmpty()) return "";
                return String.format("geo:%s,%s", lat, lng);

            case CONTACT:
                String contactName = getInputValue("contact_name");
                String contactPhone = getInputValue("contact_phone");
                if (contactName.isEmpty() || contactPhone.isEmpty()) return "";
                return String.format("MECARD:N:%s;TEL:%s;;", contactName, contactPhone);

            case FACEBOOK:
                String facebook = getInputValue("facebook");
                if (facebook.isEmpty()) return "";
                if (facebook.contains("facebook.com/")) return facebook;
                return "https://facebook.com/" + facebook;

            case INSTAGRAM:
                String instagram = getInputValue("instagram");
                if (instagram.isEmpty()) return "";
                return "https://instagram.com/" + instagram;

            case TWITTER:
                String twitter = getInputValue("twitter");
                if (twitter.isEmpty()) return "";
                return "https://twitter.com/" + twitter;

            case LINKEDIN:
                String linkedin = getInputValue("linkedin");
                if (linkedin.isEmpty()) return "";
                if (linkedin.contains("linkedin.com/")) return linkedin;
                return "https://linkedin.com/in/" + linkedin;

            case WHATSAPP:
                String whatsapp = getInputValue("whatsapp");
                if (whatsapp.isEmpty()) return "";
                return "https://wa.me/" + whatsapp.replace("+", "").replace(" ", "");

            case SKYPE:
                String skype = getInputValue("skype");
                if (skype.isEmpty()) return "";
                return "skype:" + skype + "?call";

            case TELEGRAM:
                String telegram = getInputValue("telegram");
                if (telegram.isEmpty()) return "";
                return "https://t.me/" + telegram;

            case YOUTUBE:
                String youtube = getInputValue("youtube");
                if (youtube.isEmpty()) return "";
                if (!youtube.startsWith("http")) {
                    youtube = "https://youtube.com/" + youtube;
                }
                return youtube;

            case WECHAT:
                String wechat = getInputValue("wechat");
                if (wechat.isEmpty()) return "";
                return wechat;

            case TUMBLR:
                String tumblr = getInputValue("tumblr");
                if (tumblr.isEmpty()) return "";
                if (!tumblr.startsWith("http")) {
                    tumblr = "https://" + tumblr + ".tumblr.com";
                }
                return tumblr;

            case LINE:
                String line = getInputValue("line");
                if (line.isEmpty()) return "";
                return line;

            case EVENT:
                String title = getInputValue("event_title");
                String date = getInputValue("event_date");
                String location = getInputValue("event_location");
                if (title.isEmpty() || date.isEmpty()) return "";
                return String.format("BEGIN:VEVENT\nSUMMARY:%s\nDTSTART:%s\nLOCATION:%s\nEND:VEVENT",
                        title, date, location);

            default:
                return getInputValue("content");
        }
    }

    private void downloadQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Please generate QR code first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "QR_" + qrType.name() + "_" + timeStamp + ".png";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Save to Downloads folder
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QR Codes");

                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

                if (uri != null) {
                    OutputStream outputStream = resolver.openOutputStream(uri);
                    if (outputStream != null) {
                        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.close();

                        Toast.makeText(this,
                                "✅ QR code saved to Downloads/QR Codes\n\nFile: " + fileName,
                                Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // Android 9 and below
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File qrDir = new File(downloadsDir, "QR Codes");

                if (!qrDir.exists()) {
                    qrDir.mkdirs();
                }

                File file = new File(qrDir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                // Update gallery
                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), fileName, "QR Code");

                Toast.makeText(this,
                        "✅ QR code saved to Downloads/QR Codes\n\nPath: " + file.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Please generate QR code first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Save bitmap to cache directory
            File cachePath = new File(getCacheDir(), "images");
            if (!cachePath.exists()) {
                cachePath.mkdirs();
            }

            File file = new File(cachePath, "qr_code_share_" + System.currentTimeMillis() + ".png");
            FileOutputStream stream = new FileOutputStream(file);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            // Get URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    file);

            if (contentUri != null) {
                // Create share intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Generated by *Anaya QR Scanner App*");
                shareIntent.setType("image/png");

                // Grant temporary read permission to the content URI
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Start share activity
                startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
            } else {
                Toast.makeText(this, "Failed to create shareable URI", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ShareQR", "Error: " + e.getMessage());
            Toast.makeText(this, "Failed to share QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - Request READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            // Android 6-12 - Request READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                // Show explanation if needed
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                                android.Manifest.permission.READ_MEDIA_IMAGES :
                                android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    new AlertDialog.Builder(this)
                            .setTitle("Permission Needed")
                            .setMessage("This permission is needed to save and share QR codes")
                            .setPositiveButton("OK", (dialog, which) ->
                                    checkAndRequestPermissions())
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }
            }
        }
    }
}