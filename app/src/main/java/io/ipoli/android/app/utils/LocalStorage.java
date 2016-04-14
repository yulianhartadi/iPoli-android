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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void saveStringSet(String key, Set<String> values) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        editor.putString(key, gson.toJson(values));
        editor.apply();
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
}
