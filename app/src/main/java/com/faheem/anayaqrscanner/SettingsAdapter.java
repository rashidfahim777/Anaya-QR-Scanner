package com.faheem.anayaqrscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    public interface OnSettingClickListener {
        void onSettingClick(int position);
        void onToggleChanged(int position, boolean isChecked);
    }

    private Context context;
    private List<SettingItem> settingsList;
    private OnSettingClickListener listener;

    public SettingsAdapter(Context context, List<SettingItem> settingsList, OnSettingClickListener listener) {
        this.context = context;
        this.settingsList = settingsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingItem item = settingsList.get(position);

        holder.tvTitle.setText(item.getTitle());

        // Show/hide subtitle
        if (item.getSubtitle() != null && !item.getSubtitle().isEmpty()) {
            holder.tvSubtitle.setText(item.getSubtitle());
            holder.tvSubtitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvSubtitle.setVisibility(View.GONE);
        }

        // Handle different setting types
        switch (item.getType()) {
            case SettingItem.TYPE_TOGGLE:
                holder.switchSetting.setChecked(item.getToggleState());
                holder.switchSetting.setVisibility(View.VISIBLE);
                holder.tvValue.setVisibility(View.GONE);
                holder.ivArrow.setVisibility(View.GONE);

                holder.switchSetting.setOnCheckedChangeListener(null); // Clear previous listener
                holder.switchSetting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (listener != null) {
                            listener.onToggleChanged(holder.getAdapterPosition(), isChecked);
                        }
                    }
                });
                break;

            case SettingItem.TYPE_ARROW:
                holder.switchSetting.setVisibility(View.GONE);
                holder.tvValue.setVisibility(View.GONE);
                holder.ivArrow.setVisibility(View.VISIBLE);
                break;

            case SettingItem.TYPE_TEXT:
                holder.switchSetting.setVisibility(View.GONE);
                holder.tvValue.setText(item.getSubtitle());
                holder.tvValue.setVisibility(View.VISIBLE);
                holder.ivArrow.setVisibility(View.GONE);
                break;
        }

        // Item click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && item.getType() != SettingItem.TYPE_TOGGLE) {
                    listener.onSettingClick(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return settingsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvValue;
        SwitchCompat switchSetting;
        ImageView ivArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvValue = itemView.findViewById(R.id.tvValue);
            switchSetting = itemView.findViewById(R.id.switchSetting);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}