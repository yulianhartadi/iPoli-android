package io.ipoli.android.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/15/16.
 */
public class LocalStorage {
    private final SharedPreferences sharedPreferences;
    private final ObjectMapper objectMapper;

    private LocalStorage(SharedPreferences sharedPreferences, ObjectMapper objectMapper) {
        this.sharedPreferences = sharedPreferences;
        this.objectMapper = objectMapper;
    }

    public static LocalStorage of(Context context, ObjectMapper objectMapper) {
        return new LocalStorage(PreferenceManager.getDefaultSharedPreferences(context), objectMapper);
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

    public void saveIntSet(String key, Set<Integer> values) {
        try {
            editor().putString(key, objectMapper.writeValueAsString(values)).apply();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can't convert int set to JSON string", e);
        }
    }

    public Set<Integer> readIntSet(String key, Set<Integer> defaultValue) {
        String json = sharedPreferences.getString(key, "");
        if (TextUtils.isEmpty(json)) {
            return defaultValue;
        }
        TypeReference typeReference = new TypeReference<CopyOnWriteArraySet<Integer>>() {
        };
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Can't create int set from JSON: " + json, e);
        }
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
