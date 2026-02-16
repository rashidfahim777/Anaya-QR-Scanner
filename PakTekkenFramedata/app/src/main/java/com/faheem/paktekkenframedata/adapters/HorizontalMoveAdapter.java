package com.faheem.paktekkenframedata.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faheem.paktekkenframedata.R;
import com.faheem.paktekkenframedata.models.Move;

import java.util.ArrayList;
import java.util.List;

public class HorizontalMoveAdapter extends RecyclerView.Adapter<HorizontalMoveAdapter.MoveViewHolder> {

    private List<Move> moves;
    private OnMoveClickListener listener;

    public interface OnMoveClickListener {
        void onMoveClick(Move move);
    }

    public HorizontalMoveAdapter(List<Move> moves, OnMoveClickListener listener) {
        this.moves = moves != null ? moves : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public MoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_horizontal_move, parent, false);
        return new MoveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoveViewHolder holder, int position) {
        Move move = moves.get(position);
        holder.bind(move, listener);
    }

    @Override
    public int getItemCount() {
        return moves.size();
    }

    public void updateList(List<Move> newList) {
        this.moves = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class MoveViewHolder extends RecyclerView.ViewHolder {
        // JSON columns: command, hit_level, damage, startup, block, hit, counter_hit, notes
        private TextView tvCommand;
        private TextView tvHitLevel;
        private TextView tvDamage;
        private TextView tvStartup;
        private TextView tvBlock;
        private TextView tvHit;
        private TextView tvCounterHit;
        private TextView tvNotes;
        private LinearLayout rowLayout;

        public MoveViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize all views
            tvCommand = itemView.findViewById(R.id.tvCommand);
            tvHitLevel = itemView.findViewById(R.id.tvHitLevel);
            tvDamage = itemView.findViewById(R.id.tvDamage);
            tvStartup = itemView.findViewById(R.id.tvStartup);
            tvBlock = itemView.findViewById(R.id.tvBlock);
            tvHit = itemView.findViewById(R.id.tvHit);
            tvCounterHit = itemView.findViewById(R.id.tvCounterHit);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            rowLayout = itemView.findViewById(R.id.rowLayout);
        }

        public void bind(final Move move, final OnMoveClickListener listener) {
            // Set text for each column
            tvCommand.setText(getSafeString(move.getCommand()));
            tvHitLevel.setText(formatHitLevel(move.getHitLevel()));
            tvDamage.setText(getSafeString(move.getDamage()));
            tvStartup.setText(formatStartup(move.getStartup()));
            tvBlock.setText(getSafeString(move.getBlock()));
            tvHit.setText(getSafeString(move.getHit()));
            tvCounterHit.setText(getSafeString(move.getCounterHit()));

            // Handle notes - show first line only
            String notesText = formatNotes(move.getNotes());
            tvNotes.setText(notesText);

            // Set background color for alternating rows
            if (getAdapterPosition() % 2 == 0) {
                rowLayout.setBackgroundColor(0xFFF5F5F5);
            } else {
                rowLayout.setBackgroundColor(0xFFFFFFFF);
            }

            // Set color for hit level
            setHitLevelColor(tvHitLevel, move.getHitLevel());

            // Set color for block advantage
            setBlockAdvantageColor(tvBlock, move.getBlock());

            // Set color for startup (always purple)
            tvStartup.setTextColor(0xFF6200EE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMoveClick(move);
                }
            });
        }

        private String getSafeString(String value) {
            return value != null && !value.isEmpty() ? value : "-";
        }

        private String formatStartup(String startup) {
            if (startup == null || startup.isEmpty()) return "i?";
            if (startup.startsWith("i")) return startup;
            return "i" + startup;
        }

        private String formatHitLevel(String hitLevel) {
            if (hitLevel == null || hitLevel.isEmpty()) return "?";

            StringBuilder formatted = new StringBuilder();
            if (hitLevel.contains("h")) formatted.append("H");
            if (hitLevel.contains("m")) formatted.append("M");
            if (hitLevel.contains("l")) formatted.append("L");
            if (hitLevel.contains("t")) formatted.append("T");

            return formatted.toString();
        }

        private String formatNotes(List<String> notes) {
            if (notes == null || notes.isEmpty()) return "-";
            // Return first note only, truncated if too long
            String firstNote = notes.get(0);
            if (firstNote.length() > 25) {
                return firstNote.substring(0, 22) + "...";
            }
            return firstNote;
        }

        private void setHitLevelColor(TextView textView, String hitLevel) {
            if (hitLevel == null) return;

            if (hitLevel.contains("h")) {
                textView.setBackgroundColor(0xFF2196F3); // Blue
                textView.setTextColor(0xFFFFFFFF);
            } else if (hitLevel.contains("m")) {
                textView.setBackgroundColor(0xFFFF9800); // Orange
                textView.setTextColor(0xFFFFFFFF);
            } else if (hitLevel.contains("l")) {
                textView.setBackgroundColor(0xFF4CAF50); // Green
                textView.setTextColor(0xFFFFFFFF);
            } else if (hitLevel.contains("t")) {
                textView.setBackgroundColor(0xFF9C27B0); // Purple
                textView.setTextColor(0xFFFFFFFF);
            } else {
                textView.setBackgroundColor(0xFF9E9E9E); // Grey
                textView.setTextColor(0xFFFFFFFF);
            }
        }

        private void setBlockAdvantageColor(TextView textView, String block) {
            if (block == null || block.isEmpty()) return;

            try {
                String blockStr = block.replace("+", "").replace("-", "").replaceAll("[^0-9]", "");
                if (blockStr.isEmpty()) return;

                int value = Integer.parseInt(blockStr);

                if (block.contains("-")) {
                    // Negative is bad (unsafe)
                    if (value >= 10) {
                        textView.setTextColor(0xFFF44336); // Red
                    } else if (value >= 5) {
                        textView.setTextColor(0xFFFF9800); // Orange
                    } else {
                        textView.setTextColor(0xFFFFC107); // Yellow
                    }
                } else {
                    // Positive is good (safe)
                    if (value >= 5) {
                        textView.setTextColor(0xFF4CAF50); // Green
                    } else if (value >= 3) {
                        textView.setTextColor(0xFF8BC34A); // Light green
                    } else {
                        textView.setTextColor(0xFFCDDC39); // Lime
                    }
                }
            } catch (NumberFormatException e) {
                textView.setTextColor(0xFF9E9E9E); // Grey
            }
        }
    }
}