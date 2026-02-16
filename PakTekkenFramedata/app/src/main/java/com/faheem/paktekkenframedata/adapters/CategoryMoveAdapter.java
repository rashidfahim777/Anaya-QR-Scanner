package com.faheem.paktekkenframedata.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.faheem.paktekkenframedata.R;
import com.faheem.paktekkenframedata.models.Move;
import java.util.List;

public class CategoryMoveAdapter extends RecyclerView.Adapter<CategoryMoveAdapter.MoveViewHolder> {

    private List<Move> moves;

    public CategoryMoveAdapter(List<Move> moves) {
        this.moves = moves;
    }

    @NonNull
    @Override
    public MoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_move, parent, false);
        return new MoveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoveViewHolder holder, int position) {
        Move move = moves.get(position);
        holder.tvCommand.setText(move.getCommand());
        holder.tvStartup.setText(move.getStartup());
    }

    @Override
    public int getItemCount() {
        return moves.size();
    }

    static class MoveViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommand;
        TextView tvStartup;

        public MoveViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommand = itemView.findViewById(R.id.tvCommand);
            tvStartup = itemView.findViewById(R.id.tvStartup);
        }
    }
}