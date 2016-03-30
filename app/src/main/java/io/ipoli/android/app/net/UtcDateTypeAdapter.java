
package io.ipoli.android.app.net;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class UtcDateTypeAdapter extends TypeAdapter<Date> {
    private static final String ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public void write(JsonWriter out, Date date) throws IOException {
        if (date == null) {
            out.nullValue();
        } else {
            String value = format(date);
            out.value(value);
        }
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        try {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    String date = in.nextString();
                    return parse(date);
            }
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }

    private static String format(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(ISO_8601_24H_FULL_FORMAT, Locale.getDefault());
        return formatter.format(date);
    }

    private static Date parse(String date) throws ParseException {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(ISO_8601_24H_FULL_FORMAT, Locale.getDefault());
            return formatter.parse(date);
        } catch (Exception fail) {
            String input = (date == null) ? null : ('"' + date + "'");
            throw new ParseException("Failed to parse date [" + input + "]: " + fail.getMessage(), 0);
        }
    }
}