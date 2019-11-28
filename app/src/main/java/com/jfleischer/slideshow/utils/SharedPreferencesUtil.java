package com.jfleischer.slideshow.utils;

import android.content.Context;

public class SharedPreferences {
}

public class SharedPreferencesUtil {

    private static android.content.SharedPreferences sharedPreferences;

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        refreshOptedInForAnalytics();
    }

    private static final String IS_WELCOMED_KEY = "IS_WELCOMED_KEY";

    public static boolean isWelcomed() {
        return sharedPreferences.getBoolean(IS_WELCOMED_KEY, false);
    }

    public static void setIsWelcomed() {
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_WELCOMED_KEY, true);
        editor.apply();
    }
