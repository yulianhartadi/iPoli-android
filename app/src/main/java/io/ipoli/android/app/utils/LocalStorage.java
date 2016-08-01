package io.ipoli.android.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/15/16.
 */
public class LocalStorage {
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private LocalStorage(SharedPreferences sharedPreferences, Gson gson) {
        this.sharedPreferences = sharedPreferences;
        this.gson = gson;
    }

    public static LocalStorage of(Context context, Gson gson) {
        return new LocalStorage(PreferenceManager.getDefaultSharedPreferences(context), gson);
    }

    public void saveInt(String key, int value) {
        editor().putInt(key, value).apply();
    }

    public void saveLong(String key, long value) {
        editor().putLong(key, value).apply();
    }

    public long readLong(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    public void saveStringSet(String key, Set<String> values) {
        editor().putString(key, gson.toJson(values)).apply();
    }

    public void saveIntSet(String key, Set<Integer> values) {
        editor().putString(key, gson.toJson(values)).apply();
    }

    public Set<String> readStringSet(String key) {
        String json = sharedPreferences.getString(key, "");
        if (TextUtils.isEmpty(json)) {
            return new CopyOnWriteArraySet<>();
        }
        Type typeToken = new TypeToken<CopyOnWriteArraySet<String>>() {
        }.getType();
        return gson.fromJson(json, typeToken);
    }

    public Set<Integer> readIntSet(String key, Set<Integer> defaultValue) {
        String json = sharedPreferences.getString(key, "");
        if (TextUtils.isEmpty(json)) {
            return defaultValue;
        }
        Type typeToken = new TypeToken<CopyOnWriteArraySet<Integer>>() {
        }.getType();
        return gson.fromJson(json, typeToken);
    }

    public int readInt(String key) {
        return readInt(key, 0);
    }

    public int readInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void increment(String key) {
        int value = readInt(key);
        value++;
        saveInt(key, value);
    }

    public void saveString(String key, String value) {
        editor().putString(key, value).apply();
    }

    private SharedPreferences.Editor editor() {
        return sharedPreferences.edit();
    }

    public String readString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public boolean readBool(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void saveBool(String key, boolean value) {
        editor().putBoolean(key, value).apply();
    }


}
