package com.jfleischer.slideshow.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
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

    private static final String SLIDE_DURATION = "SLIDE_DURATION";
    public static int get_slide_duration() {
        return sharedPreferences.getInt(SLIDE_DURATION, 2500);
    }
    public static void set_slide_duration(int duration_ms) {
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SLIDE_DURATION, duration_ms);
        editor.apply();
    }

    private static final String FADE_DURATION = "FADE_DURATION";
    public static int get_fade_duration() {
        return sharedPreferences.getInt(FADE_DURATION, 2500);
    }
    public static void set_fade_duration(int duration_ms) {
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(FADE_DURATION, duration_ms);
        editor.apply();
    }

    private static final String CONTROLLABLE = "CONTROLLABLE";
    public static boolean get_slideshow_controllable() {
        return sharedPreferences.getBoolean(CONTROLLABLE, false);
    }
    public static void set_slideshow_controllable(boolean c) {
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CONTROLLABLE, c);
        editor.apply();
    }

}
