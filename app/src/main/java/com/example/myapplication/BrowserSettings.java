package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class BrowserSettings {
    private static final String PREFS_NAME = "browser_settings";
    private static final String KEY_AD_BLOCK = "ad_block_enabled";
    private static final String KEY_NIGHT_MODE = "night_mode_enabled";
    private static final String KEY_CUSTOM_UA = "custom_user_agent";
    private static final String KEY_JAVASCRIPT = "javascript_enabled";
    private static final String KEY_COOKIES = "cookies_enabled";

    private final SharedPreferences preferences;
    private static BrowserSettings instance;

    private BrowserSettings(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized BrowserSettings getInstance(Context context) {
        if (instance == null) {
            instance = new BrowserSettings(context);
        }
        return instance;
    }

    public void setAdBlockEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_AD_BLOCK, enabled).apply();
    }

    public boolean isAdBlockEnabled() {
        return preferences.getBoolean(KEY_AD_BLOCK, false);
    }

    public void setNightModeEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NIGHT_MODE, enabled).apply();
    }

    public boolean isNightModeEnabled() {
        return preferences.getBoolean(KEY_NIGHT_MODE, false);
    }

    public void setCustomUserAgent(String userAgent) {
        preferences.edit().putString(KEY_CUSTOM_UA, userAgent).apply();
    }

    public String getCustomUserAgent() {
        return preferences.getString(KEY_CUSTOM_UA, "");
    }

    public void setJavaScriptEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_JAVASCRIPT, enabled).apply();
    }

    public boolean isJavaScriptEnabled() {
        return preferences.getBoolean(KEY_JAVASCRIPT, true);
    }

    public void setCookiesEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_COOKIES, enabled).apply();
    }

    public boolean isCookiesEnabled() {
        return preferences.getBoolean(KEY_COOKIES, true);
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }
}