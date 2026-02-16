package com.faheem.paktekkenframedata.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.faheem.paktekkenframedata.R;
import com.faheem.paktekkenframedata.models.Move;

import java.util.ArrayList;
import java.util.List;

public class MoveAdapter extends RecyclerView.Adapter<MoveAdapter.ViewHolder> implements Filterable {

    private List<Move> moveList;
    private List<Move> moveListFull;
    private OnMoveClickListener listener;

    public interface OnMoveClickListener {
        void onMoveClick(Move move, int position);
    }

    public MoveAdapter(List<Move> moveList, OnMoveClickListener listener) {
        this.moveList = moveList;
        this.moveListFull = new ArrayList<>(moveList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_move, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Move move = moveList.get(position);
        holder.bind(move, listener, position);
    }

    @Override
    public int getItemCount() {
        return moveList.size();
    }

    @Override
    public Filter getFilter() {
        return moveFilter;
    }

    private Filter moveFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Move> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(moveListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Move move : moveListFull) {
                    // Filter by command
                    if (move.getCommand().toLowerCase().contains(filterPattern)) {
                        filteredList.add(move);
                    }
                    // Filter by hit level
                    else if (filterPattern.startsWith("level:")) {
                        String level = filterPattern.substring(6);
                        if (move.getHitLevel() != null && move.getHitLevel().contains(level)) {
                            filteredList.add(move);
                        }
                    }
                    // Filter by damage range
                    else if (filterPattern.startsWith("damage>")) {
                        try {
                            int minDamage = Integer.parseInt(filterPattern.substring(7));
                            String damageStr = move.getDamage().replaceAll("[^0-9]", "");
                            if (!damageStr.isEmpty()) {
                                int damage = Integer.parseInt(damageStr);
                                if (damage > minDamage) {
                                    filteredList.add(move);
                                }
                            }
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            moveList.clear();
            moveList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void sortByStartup() {
        moveList.sort((m1, m2) -> {
            int s1 = extractNumber(m1.getStartup());
            int s2 = extractNumber(m2.getStartup());
            return Integer.compare(s1, s2);
        });
        notifyDataSetChanged();
    }

    public void sortByDamage() {
        moveList.sort((m1, m2) -> {
            int d1 = extractNumber(m1.getDamage());
            int d2 = extractNumber(m2.getDamage());
            return Integer.compare(d2, d1); // Descending
        });
        notifyDataSetChanged();
    }

    public void sortByBlockAdvantage() {
        moveList.sort((m1, m2) -> {
            int b1 = extractAdvantage(m1.getBlock());
            int b2 = extractAdvantage(m2.getBlock());
            return Integer.compare(b2, b1); // Higher is better
        });
        notifyDataSetChanged();
    }

    private int extractNumber(String str) {
        if (str == null || str.isEmpty()) return 0;
        String numStr = str.replaceAll("[^0-9]", "");
        if (numStr.isEmpty()) return 0;
        return Integer.parseInt(numStr);
    }

    private int extractAdvantage(String str) {
        if (str == null || str.isEmpty()) return 0;
        String numStr = str.replace("+", "").replace("-", "");
        if (numStr.isEmpty()) return 0;
        int value = Integer.parseInt(numStr);
        return str.contains("-") ? -value : value;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCommand;
        private TextView tvHitLevel;
        private TextView tvDamage;
        private TextView tvStartup;
        private TextView tvBlock;
        private TextView tvHit;
        private TextView tvCounterHit;
        private LinearLayout notesLayout;
        private TextView tvNotes;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommand = itemView.findViewById(R.id.tvCommand);
            tvHitLevel = itemView.findViewById(R.id.tvHitLevel);
            tvDamage = itemView.findViewById(R.id.tvDamage);
            tvStartup = itemView.findViewById(R.id.tvStartup);
            tvBlock = itemView.findViewById(R.id.tvBlock);
            tvHit = itemView.findViewById(R.id.tvHit);
            tvCounterHit = itemView.findViewById(R.id.tvCounterHit);
            notesLayout = itemView.findViewById(R.id.notesLayout);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(final Move move, final OnMoveClickListener listener, final int position) {
            tvCommand.setText(move.getCommand());
            tvHitLevel.setText(formatHitLevel(move.getHitLevel()));
            tvDamage.setText("DMG: " + move.getDamage());
            tvStartup.setText(formatStartup(move.getStartup()));
            tvBlock.setText(formatAdvantage("BL", move.getBlock()));
            tvHit.setText(formatAdvantage("HT", move.getHit()));
            tvCounterHit.setText(formatAdvantage("CH", move.getCounterHit()));

            // Set colors based on values
            tvBlock.setTextColor(move.getBlockAdvantageColor());
            tvHitLevel.setBackgroundColor(move.getHitLevelColor());

            // Handle notes
            if (move.getNotes() != null && !move.getNotes().isEmpty()) {
                notesLayout.setVisibility(View.VISIBLE);
                StringBuilder notesText = new StringBuilder();
                for (String note : move.getNotes()) {
                    notesText.append("• ").append(note).append("\n");
                }
                tvNotes.setText(notesText.toString());
            } else {
                notesLayout.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMoveClick(move, position);
                }
            });
        }

        private String formatHitLevel(String hitLevel) {
            if (hitLevel == null || hitLevel.isEmpty()) return "?";

            StringBuilder formatted = new StringBuilder();
            if (hitLevel.contains("h")) formatted.append("H ");
            if (hitLevel.contains("m")) formatted.append("M ");
            if (hitLevel.contains("l")) formatted.append("L ");
            if (hitLevel.contains("t")) formatted.append("T ");
            if (hitLevel.contains("!")) formatted.append("!");

            return formatted.toString().trim();
        }

        private String formatStartup(String startup) {
            if (startup == null || startup.isEmpty()) return "i?";
            if (startup.startsWith("i")) return startup;
            return "i" + startup;
        }

        private String formatAdvantage(String prefix, String value) {
            if (value == null || value.isEmpty()) return prefix + ": ?";
            return prefix + ": " + value;
        }
    }
}