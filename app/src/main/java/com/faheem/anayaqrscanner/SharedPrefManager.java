package com.faheem.anayaqrscanner;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "ScannerPrefs";

    // Keys for preferences
    private static final String KEY_VIBRATION = "vibration_enabled";
    private static final String KEY_SOUND = "sound_enabled";
    // REMOVED: KEY_DARK_MODE

    private SharedPreferences sharedPreferences;
    private Context context;

    public SharedPrefManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Vibration methods
    public boolean getVibrationEnabled() {
        return sharedPreferences.getBoolean(KEY_VIBRATION, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

    // Sound methods
    public boolean getSoundEnabled() {
        return sharedPreferences.getBoolean(KEY_SOUND, true);
    }

    public void setSoundEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    // REMOVED: All dark mode methods
}