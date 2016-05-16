
package io.ipoli.android.app.net;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.ipoli.android.Constants;

public final class UtcDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    private static String format(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.API_DATETIME_ISO_8601_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    private static Date parse(String date) throws ParseException {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(Constants.API_DATETIME_ISO_8601_FORMAT);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.parse(date);
        } catch (Exception fail) {
            String input = (date == null) ? null : ('"' + date + "'");
            throw new ParseException("Failed to parse date [" + input + "]: " + fail.getMessage(), 0);
        }
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        try {
            return parse(json.getAsString());
        } catch (ParseException e) {
            Log.e("UtcDateTypeAdapter", e.getMessage());
            return null;
        }
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(format(src));
    }
}