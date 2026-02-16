package com.faheem.paktekkenframedata.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.faheem.paktekkenframedata.R;
import com.faheem.paktekkenframedata.models.Character;

import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.ViewHolder> {

    private List<Character> characters;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Character character);
    }

    public CharacterAdapter(List<Character> characters, OnItemClickListener listener) {
        this.characters = characters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Character character = characters.get(position);
        holder.bind(character, listener);
    }

    @Override
    public int getItemCount() {
        return characters.size();
    }

    public void updateList(List<Character> newList) {
        this.characters = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCharacter;
        private TextView tvCharacterName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCharacter = itemView.findViewById(R.id.ivCharacter);
            tvCharacterName = itemView.findViewById(R.id.tvCharacterName);
        }

        public void bind(final Character character, final OnItemClickListener listener) {
            tvCharacterName.setText(character.getName());

            // Load image with Glide
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.character_placeholder)
                    .error(R.drawable.character_placeholder)
                    .centerCrop();

            Glide.with(itemView.getContext())
                    .load(character.getImageResId())
                    .apply(requestOptions)
                    .into(ivCharacter);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(character);
                }
            });
        }
    }
}