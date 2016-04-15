package io.ipoli.android.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/15/16.
 */
public class LocalStorage {
    private final SharedPreferences sharedPreferences;

    private LocalStorage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public static LocalStorage of(Context context) {
        return new LocalStorage(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public void saveInt(String key, int value) {
        editor().putInt(key, value).apply();
    }

    public void saveStringSet(String key, Set<String> values) {
        Gson gson = new Gson();
        editor().putString(key, gson.toJson(values)).apply();
    }

    public Set<String> readStringSet(String key) {
        String json = sharedPreferences.getString(key, "");
        Gson gson = new Gson();
        Type listOfTestObject = new TypeToken<Set<String>>() {
        }.getType();
        return gson.fromJson(json, listOfTestObject);
    }

    public int readInt(String key) {
        return sharedPreferences.getInt(key, 0);
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
}
