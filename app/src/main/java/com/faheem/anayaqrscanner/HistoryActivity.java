package com.faheem.anayaqrscanner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private TextView tvEmptyState;
    private Button btnClearAll;
    private ChipGroup chipGroup;
    private Chip chipAll, chipQR, chipBarcode;

    private HistoryAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = DatabaseHelper.getInstance(this);

        initializeViews();
        setupToolbar();
        setupListeners();
        setupRecyclerView();
        setupChipGroup();
        loadHistory(0); // Load "All" by default
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyState = findViewById(R.id.empty_state);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        btnClearAll = findViewById(R.id.btnClearAll);
        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipQR = findViewById(R.id.chipQR);
        chipBarcode = findViewById(R.id.chipBarcode);

        // Set empty state text
        tvEmptyState.setText("No scans yet\nYour scan history will appear here");

        Log.d(TAG, "Views initialized");
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Scan History");
            }

            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            Log.e(TAG, "Toolbar not found!");
            // Fallback to system action bar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Scan History");
            }
        }
    }

    private void setupListeners() {
        btnClearAll.setOnClickListener(v -> {
            Log.d(TAG, "Clear All button clicked");
            showClearAllDialog();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create adapter with item click listener
        adapter = new HistoryAdapter(this, new ArrayList<>(), new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ScanItem scanItem) {
                Log.d(TAG, "Item clicked: " + scanItem.getContent());
                // Open result screen with scan item details
                Intent intent = new Intent(HistoryActivity.this, ResultActivity.class);
                intent.putExtra("type", scanItem.getType());
                intent.putExtra("content", scanItem.getContent());
                startActivity(intent);
            }

            @Override
            public void onItemDelete(ScanItem scanItem, int position) {
                // This is called when user deletes an item from history
                Log.d(TAG, "Item deleted: " + scanItem.getId());

                // Reload the current filter after deletion
                int selectedFilter = getSelectedFilter();
                loadHistory(selectedFilter);
            }
        });

        recyclerView.setAdapter(adapter);
        Log.d(TAG, "RecyclerView setup complete");
    }

    private void setupChipGroup() {
        // Set the first chip (All) as selected by default
        chipAll.setChecked(true);

        // Set chip click listeners
        chipAll.setOnClickListener(v -> {
            chipAll.setChecked(true);
            chipQR.setChecked(false);
            chipBarcode.setChecked(false);
            loadHistory(0);
        });

        chipQR.setOnClickListener(v -> {
            chipQR.setChecked(true);
            chipAll.setChecked(false);
            chipBarcode.setChecked(false);
            loadHistory(1);
        });

        chipBarcode.setOnClickListener(v -> {
            chipBarcode.setChecked(true);
            chipAll.setChecked(false);
            chipQR.setChecked(false);
            loadHistory(2);
        });

        // Handle chip group single selection
        chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, List<Integer> checkedIds) {
                if (checkedIds.isEmpty()) {
                    // If nothing is selected, select "All" by default
                    chipAll.setChecked(true);
                    loadHistory(0);
                } else {
                    int selectedId = checkedIds.get(0);
                    if (selectedId == R.id.chipAll) {
                        loadHistory(0);
                    } else if (selectedId == R.id.chipQR) {
                        loadHistory(1);
                    } else if (selectedId == R.id.chipBarcode) {
                        loadHistory(2);
                    }
                }
            }
        });

        Log.d(TAG, "ChipGroup setup complete");
    }

    private int getSelectedFilter() {
        if (chipAll.isChecked()) return 0;
        if (chipQR.isChecked()) return 1;
        if (chipBarcode.isChecked()) return 2;
        return 0; // Default to All
    }

    private void loadHistory(int filterType) {
        List<ScanItem> scanItems;

        switch (filterType) {
            case 0: // All
                scanItems = dbHelper.getAllScans();
                break;
            case 1: // QR Codes
                scanItems = dbHelper.getScansByType(256); // QR Code type
                break;
            case 2: // Barcodes
                // Get all barcode types
                List<ScanItem> allBarcodes = new ArrayList<>();
                allBarcodes.addAll(dbHelper.getScansByType(1));   // CODE_128
                allBarcodes.addAll(dbHelper.getScansByType(2));   // CODE_39
                allBarcodes.addAll(dbHelper.getScansByType(4));   // CODE_93
                allBarcodes.addAll(dbHelper.getScansByType(8));   // CODABAR
                allBarcodes.addAll(dbHelper.getScansByType(16));  // DATA_MATRIX
                allBarcodes.addAll(dbHelper.getScansByType(32));  // EAN_8
                allBarcodes.addAll(dbHelper.getScansByType(64));  // EAN_13
                allBarcodes.addAll(dbHelper.getScansByType(128)); // UPC_A
                allBarcodes.addAll(dbHelper.getScansByType(512)); // UPC_E
                allBarcodes.addAll(dbHelper.getScansByType(1024)); // PDF_417
                allBarcodes.addAll(dbHelper.getScansByType(2048)); // AZTEC
                allBarcodes.addAll(dbHelper.getScansByType(4096)); // ITF
                scanItems = allBarcodes;
                break;
            default:
                scanItems = dbHelper.getAllScans();
        }

        // Sort by timestamp (newest first)
        Collections.sort(scanItems, (item1, item2) ->
                item2.getTimestamp().compareTo(item1.getTimestamp()));

        // Update adapter with filtered data
        adapter.updateList(scanItems);

        // Update UI state
        updateEmptyState(scanItems.isEmpty());

        // Update clear all button visibility
        updateClearAllButton(scanItems.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            // Set appropriate empty message based on filter
            int selectedFilter = getSelectedFilter();
            String message = "No scans found";
            switch (selectedFilter) {
                case 1:
                    message = "No QR codes found";
                    break;
                case 2:
                    message = "No barcodes found";
                    break;
            }
            tvEmptyState.setText(message);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateClearAllButton(boolean isEmpty) {
        // Only show clear all button if there are items in the current filter
        if (isEmpty) {
            btnClearAll.setVisibility(View.GONE);
        } else {
            // Check if there are any items in the entire database
            int totalCount = dbHelper.getScanCount();
            btnClearAll.setVisibility(totalCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void showClearAllDialog() {
        Log.d(TAG, "Showing clear all dialog");

        new AlertDialog.Builder(this)
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all scan history?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Log.d(TAG, "User confirmed clear all");
                    clearAllHistory();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "User cancelled clear all");
                    dialog.dismiss();
                })
                .show();
    }

    private void clearAllHistory() {
        Log.d(TAG, "Clearing all history");

        boolean success = dbHelper.clearAllHistory();

        if (success) {
            // Clear current adapter data
            adapter.updateList(new ArrayList<>());

            // Update UI
            updateEmptyState(true);
            btnClearAll.setVisibility(View.GONE);

            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "History cleared successfully");
        } else {
            Toast.makeText(this, "Failed to clear history", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to clear history");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // Reload history based on current selected filter
        int selectedFilter = getSelectedFilter();
        loadHistory(selectedFilter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}