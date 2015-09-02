package com.curiousily.ipoli.app.api.parameters;

import com.curiousily.ipoli.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/15.
 */
public class PathDate {
    private static final ThreadLocal<DateFormat> DF = new ThreadLocal<DateFormat>() {
        @Override
        public DateFormat initialValue() {
            return new SimpleDateFormat(Constants.DEFAULT_SERVER_DATE_FORMAT, Locale.getDefault());
        }
    };

    private final Date date;

    public PathDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return DF.get().format(date);
    }
}
