package com.faheem.anayaqrscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ScanItem scanItem);
        void onItemDelete(ScanItem scanItem, int position);
    }

    private Context context;
    private List<ScanItem> scanItems;
    private OnItemClickListener listener;
    private DatabaseHelper databaseHelper;

    public HistoryAdapter(Context context, List<ScanItem> scanItems, OnItemClickListener listener) {
        this.context = context;
        this.scanItems = scanItems;
        this.listener = listener;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanItem scanItem = scanItems.get(position);

        // Set content (truncate if too long)
        String content = scanItem.getContent();
        if (content.length() > 50) {
            content = content.substring(0, 47) + "...";
        }
        holder.tvContent.setText(content);

        // Set type and background based on type
        if (scanItem.getType() == 256) { // QR Code
            holder.tvType.setText("QR Code");
            holder.tvType.setBackgroundResource(R.drawable.bg_label_qr);
        } else { // Barcode
            holder.tvType.setText("Barcode");
            holder.tvType.setBackgroundResource(R.drawable.bg_label_barcode);
        }

        // Format time
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(scanItem.getTimestamp());
            holder.tvDateTime.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.tvDateTime.setText(scanItem.getTimestamp());
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(scanItem);
            }
        });

        // More options button click
        holder.ivAction.setOnClickListener(v -> showOptionsMenu(scanItem, holder.getAdapterPosition(), v));

        // Long click for delete (optional)
        holder.itemView.setOnLongClickListener(v -> {
            deleteItem(scanItem, holder.getAdapterPosition());
            return true;
        });
    }

    private void showOptionsMenu(ScanItem scanItem, int position, View view) {
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(context, view);
        popupMenu.getMenu().add("Delete");
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Delete")) {
                deleteItem(scanItem, position);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void deleteItem(ScanItem scanItem, int position) {
        // Delete from database
        boolean deleted = databaseHelper.deleteScan(scanItem.getId());

        if (deleted) {
            // Remove from list and update UI
            scanItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, scanItems.size());

            Toast.makeText(context, "Scan deleted", Toast.LENGTH_SHORT).show();

            // Notify activity to update empty state
            if (listener != null) {
                listener.onItemDelete(scanItem, position);
            }
        } else {
            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return scanItems.size();
    }

    public void updateList(List<ScanItem> newList) {
        scanItems.clear();
        scanItems.addAll(newList);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < scanItems.size()) {
            scanItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvType, tvDateTime;
        ImageView ivAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvType = itemView.findViewById(R.id.tvType);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            ivAction = itemView.findViewById(R.id.ivAction);
        }
    }
}