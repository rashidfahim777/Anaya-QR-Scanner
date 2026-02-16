package com.faheem.paktekkenframedata;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.faheem.paktekkenframedata.adapters.CharacterAdapter;
import com.faheem.paktekkenframedata.models.Character;
import com.faheem.paktekkenframedata.utils.DataLoader;
import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CharacterAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<Character> characterList = new ArrayList<>();
    private List<Character> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tekken Frame Data");
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Load data
        loadCharacterData();

        // Setup search
        setupSearch();
    }

    private void loadCharacterData() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        DataLoader.loadCharacters(this, new DataLoader.DataLoadListener() {
            @Override
            public void onSuccess(Map<String, Character> characters) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        characterList.clear();
                        characterList.addAll(characters.values());
                        filteredList.clear();
                        filteredList.addAll(characterList);

                        if (adapter == null) {
                            setupAdapter();
                        } else {
                            adapter.updateList(filteredList);
                        }

                        progressBar.setVisibility(View.GONE);
                        if (characterList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
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
    private void setupAdapter() {
        adapter = new CharacterAdapter(filteredList, new CharacterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Character character) {
                openMoveDetail(character);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCharacters(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCharacters(newText);
                return true;
            }
        });
    }

    private void filterCharacters(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(characterList);
        } else {
            for (Character character : characterList) {
                if (character.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(character);
                }
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No characters found");
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void openMoveDetail(Character character) {
        Intent intent = new Intent(MainActivity.this, MoveDetailActivity.class);
        intent.putExtra("character_name", character.getName());
        intent.putExtra("character_id", character.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadCharacterData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}