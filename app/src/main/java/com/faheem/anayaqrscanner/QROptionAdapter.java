package com.faheem.anayaqrscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QROptionAdapter extends RecyclerView.Adapter<QROptionAdapter.ViewHolder> {

    private Context context;
    private List<QROption> qrOptions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public QROptionAdapter(Context context, List<QROption> qrOptions, OnItemClickListener listener) {
        this.context = context;
        this.qrOptions = qrOptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_qr_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QROption option = qrOptions.get(position);

        // Set icon
        holder.ivIcon.setImageResource(option.getIconResId());

        // Set label with abbreviation for long names
        String label = getAbbreviatedLabel(option.getName());
        holder.tvLabel.setText(label);

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return qrOptions.size();
    }

    private String getAbbreviatedLabel(String originalLabel) {
        switch (originalLabel) {
            case "Instagram":
                return "IG";
            case "Snapchat":
                return "SC";
            case "Telegram":
                return "TG";
            case "YouTube":
                return "YT";
            case "WeChat":
                return "WC";
            case "LinkedIn":
                return "LN";
            default:
                return originalLabel;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }
}