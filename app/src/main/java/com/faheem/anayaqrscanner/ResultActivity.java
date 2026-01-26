package com.faheem.anayaqrscanner;

import static android.view.View.VISIBLE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ResultActivity extends AppCompatActivity {

    private TextView tvResultType, tvContent;
    private MaterialButton btnCopy, btnShare, btnOpen, btnSearch, btnHistory;
    private String scanType, scanContent;
    private Toolbar toolbar;
    private boolean isAlreadyInHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get data from intent
        scanType = getIntent().getStringExtra("type");
        scanContent = getIntent().getStringExtra("content");

        initializeViews();
        setupToolbar();
        setupContent();
        setupListeners();
        checkIfAlreadyInHistory();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvResultType = findViewById(R.id.tvResultType);
        tvContent = findViewById(R.id.tvContent);
        btnCopy = findViewById(R.id.btnCopy);
        btnShare = findViewById(R.id.btnShare);
        btnOpen = findViewById(R.id.btnOpen);
        btnSearch = findViewById(R.id.btnSearch);
        btnHistory = findViewById(R.id.btnHistory);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Scan Result");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupContent() {
        // Set result type
        if (scanType != null) {
            if (scanType.equals("256")) { // QR Code
                tvResultType.setText("QR Code");
                setupForQRCode();
            } else { // Barcode
                tvResultType.setText("Barcode");
                setupForBarcode();
            }
        }

        // Set content
        if (scanContent != null) {
            tvContent.setText(scanContent);
        }
    }

    private void setupForQRCode() {
        // Show Open button for URLs
        if (isUrl(scanContent)) {
            btnOpen.setVisibility(VISIBLE);
        }
        // Hide Search button for QR codes
        btnSearch.setVisibility(View.GONE);
    }

    private void setupForBarcode() {
        // Hide Open button for barcodes
        btnOpen.setVisibility(View.GONE);

        // Show Search button for product barcodes
        if (isProductBarcode(scanContent)) {
            btnSearch.setVisibility(VISIBLE);
        }
    }

    private void checkIfAlreadyInHistory() {
        if (scanContent != null && !scanContent.isEmpty()) {
            // Check if this scan is already in history
            android.content.SharedPreferences prefs = getSharedPreferences("ScanHistory", MODE_PRIVATE);
            Set<String> historySet = prefs.getStringSet("history_set", new HashSet<>());

            // Create a unique identifier for this scan
            String scanIdentifier = tvResultType.getText().toString() + ":" + scanContent;

            isAlreadyInHistory = historySet.contains(scanIdentifier);

            // Update button text and appearance if already in history
            if (isAlreadyInHistory) {
                btnHistory.setText("Saved in History");
                btnHistory.setEnabled(false);
                btnHistory.setAlpha(0.7f);

                // Show a snackbar hint
                Snackbar.make(findViewById(android.R.id.content),
                                "This scan is already saved in history",
                                Snackbar.LENGTH_LONG)
                        .setAction("View History", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Optional: Open history activity
                                // Intent intent = new Intent(ResultActivity.this, HistoryActivity.class);
                                // startActivity(intent);
                            }
                        })
                        .show();
            } else {
                btnHistory.setText("Add to History");
                btnHistory.setEnabled(true);
                btnHistory.setAlpha(1.0f);
            }
        }
    }

    private void setupListeners() {
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent();
            }
        });

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInBrowser();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchProduct();
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToHistory();
            }
        });
    }

    private boolean isUrl(String text) {
        return text != null && (text.startsWith("http://") || text.startsWith("https://") ||
                text.startsWith("www.") || text.contains("://"));
    }

    private boolean isProductBarcode(String code) {
        if (code == null) return false;

        // Check for common product barcode formats:
        // UPC-A: 12 digits, UPC-E: 8 digits, EAN-13: 13 digits, EAN-8: 8 digits
        String numericCode = code.replaceAll("[^0-9]", "");
        return numericCode.matches("^\\d{8}$") ||  // UPC-E or EAN-8
                numericCode.matches("^\\d{12}$") || // UPC-A
                numericCode.matches("^\\d{13}$");   // EAN-13
    }

    private void copyToClipboard() {
        if (scanContent != null && !scanContent.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Scan Result", scanContent);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareContent() {
        if (scanContent != null && !scanContent.isEmpty()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, scanContent);
            startActivity(Intent.createChooser(shareIntent, "Share Scan Result"));
        } else {
            Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show();
        }
    }

    private void openInBrowser() {
        if (scanContent != null && !scanContent.isEmpty()) {
            try {
                String url = scanContent;
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot open URL", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchProduct() {
        if (scanContent != null && !scanContent.isEmpty()) {
            try {
                // Search on Google Shopping
                String searchQuery = Uri.encode(scanContent);
                String url = "https://www.google.com/search?tbm=shop&q=" + searchQuery;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot search product", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addToHistory() {
        if (scanContent != null && !scanContent.isEmpty() && !isAlreadyInHistory) {
            // Get current date and time
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            // Create history entry
            String scanTypeText = tvResultType.getText().toString();
            String historyEntry = "[" + currentDateTime + "] " + scanTypeText + ": " + scanContent;
            String scanIdentifier = scanTypeText + ":" + scanContent;

            // Save to SharedPreferences using a Set for deduplication
            android.content.SharedPreferences prefs = getSharedPreferences("ScanHistory", MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = prefs.edit();

            // Get existing history Set
            Set<String> historySet = new HashSet<>(prefs.getStringSet("history_set", new HashSet<>()));

            // Add to both Set (for quick lookup) and String (for display)
            historySet.add(scanIdentifier);

            // Get existing history String (for backward compatibility)
            String existingHistory = prefs.getString("history", "");
            String newHistory = existingHistory.isEmpty() ?
                    historyEntry : existingHistory + "\n" + historyEntry;

            // Save both Set and String
            editor.putStringSet("history_set", historySet);
            editor.putString("history", newHistory);
            editor.apply();

            // Update UI
            isAlreadyInHistory = true;
            btnHistory.setText("Already in History");
            btnHistory.setEnabled(false);
            btnHistory.setAlpha(0.7f);

            // Show confirmation
            Toast.makeText(this, "Added to history", Toast.LENGTH_SHORT).show();

            // Show snackbar with option to undo
            Snackbar.make(findViewById(android.R.id.content),
                            "Scan saved to history",
                            Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeFromHistory(scanIdentifier, historyEntry);
                        }
                    })
                    .show();
        } else if (isAlreadyInHistory) {
            Toast.makeText(this, "Already in history", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFromHistory(String scanIdentifier, String historyEntry) {
        android.content.SharedPreferences prefs = getSharedPreferences("ScanHistory", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();

        // Remove from Set
        Set<String> historySet = new HashSet<>(prefs.getStringSet("history_set", new HashSet<>()));
        historySet.remove(scanIdentifier);

        // Remove from String (more complex - need to rebuild without the entry)
        String existingHistory = prefs.getString("history", "");
        if (existingHistory.contains(historyEntry)) {
            // Remove the entry and any extra newlines
            String newHistory = existingHistory.replace(historyEntry, "").trim();
            newHistory = newHistory.replaceAll("\n\n+", "\n"); // Remove duplicate newlines

            editor.putString("history", newHistory);
        }

        editor.putStringSet("history_set", historySet);
        editor.apply();

        // Update UI
        isAlreadyInHistory = false;
        btnHistory.setText("Add to History");
        btnHistory.setEnabled(true);
        btnHistory.setAlpha(1.0f);

        Toast.makeText(this, "Removed from history", Toast.LENGTH_SHORT).show();
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