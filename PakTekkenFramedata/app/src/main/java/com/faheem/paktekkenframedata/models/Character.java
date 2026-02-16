package com.faheem.paktekkenframedata.models;

import com.faheem.paktekkenframedata.R;
import java.util.Locale;
import java.util.Map;

public class Character {
    private String id;
    private String name;
    private int imageResId;
    private Map<String, Move> moves;

    public Character() {
    }

    public Character(String id, String name, int imageResId, Map<String, Move> moves) {
        this.id = id;
        this.name = name;
        this.imageResId = imageResId;
        this.moves = moves;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public Map<String, Move> getMoves() {
        return moves;
    }

    public void setMoves(Map<String, Move> moves) {
        this.moves = moves;
    }

    // Helper method to get image resource ID based on character ID
    public static int getImageResourceId(String characterId) {
        if (characterId == null) return R.drawable.character_placeholder;

        switch (characterId.toLowerCase(Locale.US)) {
            case "alisa":
                return R.drawable.alisa;
            case "armor_king":
            case "armorking":
                return R.drawable.armor_king;
            case "asuka":
                return R.drawable.asuka;
            case "azucena":
                return R.drawable.azucena;
            case "bryan":
                return R.drawable.bryan;
            case "claudio":
                return R.drawable.claudio;
            case "clive":
                return R.drawable.clive;
            case "devil_jin":
            case "deviljin":
                return R.drawable.devil_jin;
            case "dragunov":
                return R.drawable.dragunov;
            case "eddy":
                return R.drawable.eddy;
            case "fakhumram":
                return R.drawable.fakhumram;
            case "feng":
                return R.drawable.feng;
            case "heihachi":
                return R.drawable.heihachi;
            case "hwoarang":
                return R.drawable.hwoarang;
            case "jack_8":
            case "jack8":
            case "jack":
                return R.drawable.jack_8;
            case "jin":
                return R.drawable.jin;
            case "jun":
                return R.drawable.jun;
            case "kazuya":
                return R.drawable.kazuya;
            case "king":
                return R.drawable.king;
            case "kuma":
                return R.drawable.kuma;
            case "lars":
                return R.drawable.lars;
            case "law":
                return R.drawable.law;
            case "lee":
                return R.drawable.lee;
            case "leo":
                return R.drawable.leo;
            case "leroy":
                return R.drawable.leroy;
            case "lidia":
                return R.drawable.lidia;
            case "lili":
                return R.drawable.lili;
            case "mokujin":
                return R.drawable.mokujin;
            case "nina":
                return R.drawable.nina;
            case "panda":
                return R.drawable.panda;
            case "paul":
                return R.drawable.paul;
            case "raven":
                return R.drawable.raven;
            case "reina":
                return R.drawable.reina;
            case "shaheen":
                return R.drawable.shaheen;
            case "steve":
                return R.drawable.steve;
            case "victor":
                return R.drawable.victor;
            case "xiaoyu":
                return R.drawable.xiaoyu;
            case "yoshimitsu":
                return R.drawable.yoshimitsu;
            case "zafina":
                return R.drawable.zafina;
            default:
                return R.drawable.character_placeholder;
        }
    }
}