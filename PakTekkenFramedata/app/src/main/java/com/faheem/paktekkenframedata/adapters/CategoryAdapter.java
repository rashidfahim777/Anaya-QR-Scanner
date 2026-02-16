package com.faheem.paktekkenframedata.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faheem.paktekkenframedata.R;
import com.faheem.paktekkenframedata.models.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Map<String, List<Move>> categorizedMoves;
    private List<String> categoryList;

    public CategoryAdapter(Map<String, List<Move>> categorizedMoves) {
        this.categorizedMoves = categorizedMoves;
        this.categoryList = new ArrayList<>(categorizedMoves.keySet());
    }

    public void updateData(Map<String, List<Move>> newData) {
        this.categorizedMoves = newData;
        this.categoryList = new ArrayList<>(newData.keySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_column, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String categoryName = categoryList.get(position);
        List<Move> movesInCategory = categorizedMoves.get(categoryName);

        holder.bind(categoryName, movesInCategory);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryTitle;
        private RecyclerView rvMovesInCategory;
        private TextView tvEmptyCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
            rvMovesInCategory = itemView.findViewById(R.id.rvMovesInCategory);
            tvEmptyCategory = itemView.findViewById(R.id.tvEmptyCategory);
        }

        public void bind(String categoryName, List<Move> moves) {
            tvCategoryTitle.setText(categoryName);

            if (moves != null && !moves.isEmpty()) {
                rvMovesInCategory.setVisibility(View.VISIBLE);
                tvEmptyCategory.setVisibility(View.GONE);

                // Set up vertical RecyclerView for moves in this category
                CategoryMoveAdapter adapter = new CategoryMoveAdapter(moves);
                rvMovesInCategory.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                rvMovesInCategory.setAdapter(adapter);
            } else {
                rvMovesInCategory.setVisibility(View.GONE);
                tvEmptyCategory.setVisibility(View.VISIBLE);
                tvEmptyCategory.setText("No moves");
            }
        }
    }
}