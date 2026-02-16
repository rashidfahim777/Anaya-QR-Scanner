package com.faheem.paktekkenframedata;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faheem.paktekkenframedata.adapters.HorizontalMoveAdapter;
import com.faheem.paktekkenframedata.models.Character;
import com.faheem.paktekkenframedata.models.Move;
import com.faheem.paktekkenframedata.utils.DataLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoveDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;  // This ID should match your XML
    private HorizontalMoveAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvCharacterName;
    private List<Move> moveList = new ArrayList<>();
    private String characterId;
    private String characterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_detail);

        // Get intent extras
        characterName = getIntent().getStringExtra("character_name");
        characterId = getIntent().getStringExtra("character_id");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tekken Frame Data");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }
        // Force the back arrow to be white
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(
                    ContextCompat.getColor(this, android.R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
            );
        }

        // Initialize views - MAKE SURE THESE IDs MATCH YOUR XML
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvCharacterName = findViewById(R.id.tvCharacterName);

        tvCharacterName.setText(characterName);

        // Check if recyclerView is null
        if (recyclerView == null) {
            throw new NullPointerException("recyclerView is null. Check if ID 'recyclerView' exists in your layout.");
        }

        // Setup RecyclerView
        setupRecyclerView();

        // Load move data
        loadMoveData();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HorizontalMoveAdapter(moveList, move -> showMoveDetails(move));
        recyclerView.setAdapter(adapter);
    }

    private void loadMoveData() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        DataLoader.loadCharacters(this, new DataLoader.DataLoadListener() {
            @Override
            public void onSuccess(Map<String, Character> characters) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Character character = characters.get(characterId);
                        if (character != null && character.getMoves() != null) {
                            moveList.clear();
                            moveList.addAll(character.getMoves().values());
                            adapter.updateList(moveList);

                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                            tvEmpty.setText("No move data available for " + characterName);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Error loading data: " + error);
                    }
                });
            }
        });
    }

    private void showMoveDetails(Move move) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(move.getCommand());

        StringBuilder message = new StringBuilder();
        message.append("Name: ").append(move.getCommand()).append("\n");
        message.append("Hit Level: ").append(move.getHitLevel()).append("\n");
        message.append("Damage: ").append(move.getDamage()).append("\n");
        message.append("Startup: ").append(move.getStartup()).append("\n");
        message.append("Block: ").append(move.getBlock()).append("\n");
        message.append("Hit: ").append(move.getHit()).append("\n");
        message.append("Counter Hit: ").append(move.getCounterHit()).append("\n\n");

        if (move.getNotes() != null && !move.getNotes().isEmpty()) {
            message.append("Notes:\n");
            for (String note : move.getNotes()) {
                message.append("• ").append(note).append("\n");
            }
        }

        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}