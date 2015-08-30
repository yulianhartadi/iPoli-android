package com.curiousily.ipoli.utils;

import android.content.Intent;

import com.google.gson.Gson;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/31/15.
 */
public class DataSharingUtils {
    public static String toString(Object data) {
        Gson gson = new Gson();
        return gson.toJson(data);
    }

    public static <T> T fromString(String data, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(data, clazz);
    }

    public static void put(String key, Object data, Intent destination) {
        destination.putExtra(key, toString(data));
    }

    public static <T> T get(String key, Class<T> clazz, Intent source) {
        return fromString(source.getStringExtra(key), clazz);
    }

}
