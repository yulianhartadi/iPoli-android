package io.ipoli.android.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Time {

    public static final int MINUTES_IN_A_DAY = 24 * 60;

    private final int minutes;

    private Time(int minutesAfterMidnight) {
        if (minutesAfterMidnight < 0) {
            throw new IllegalArgumentException("Minutes must be >= 0. It was: " + minutesAfterMidnight);
        }
        this.minutes = minutesAfterMidnight % MINUTES_IN_A_DAY;
    }

    private Time(int hours, int minutes) {
        this(hours * 60 + minutes);
    }

    public static Time of(int minutesAfterMidnight) {
        return new Time(minutesAfterMidnight);
    }

    public static Time at(String timeString) {
        return new Time(parseHours(timeString), parseMinutes(timeString));
    }

    public static Time at(int hours, int minutes) {
        return new Time(hours, minutes);
    }

    public static Time atHours(int hours) {
        return at(hours, 0);
    }

    public static Time after(int hours, int minutes) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, hours);
        c.add(Calendar.MINUTE, minutes);
        return new Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    public static Time ago(int hours, int minutes) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, -hours);
        c.add(Calendar.MINUTE, -minutes);
        return new Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    public static Time afterHours(int hours) {
        return after(hours, 0);
    }

    public static Time afterMinutes(int minutes) {
        return after(0, minutes);
    }

    public static Time hoursAgo(int hours) {
        return ago(hours, 0);
    }

    public static Time minutesAgo(int minutes) {
        return ago(0, minutes);
    }

    private static int parseHours(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[0]);
    }

    private static int parseMinutes(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[1]);
    }

    public int toMinuteOfDay() {
        return minutes;
    }

    public long toMillisOfDay() {
        return TimeUnit.MINUTES.toMillis(minutes);
    }

    public int getHours() {
        return (int) TimeUnit.MINUTES.toHours(minutes);
    }

    public int getMinutes() {
        return minutes - getHours() * 60;
    }

    public static Time now() {
        Calendar c = Calendar.getInstance();
        return new Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    public static Time of(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return new Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%02d:%02d", getHours(), getMinutes());
    }

    public static Time plusMinutes(Time time, int minutes) {
        return Time.of(time.minutes + minutes);
    }

    public String toString(boolean use24HourFormat) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MINUTE, getMinutes());
        c.set(Calendar.HOUR_OF_DAY, getHours());

        String format = "HH:mm";
        if (!use24HourFormat) {
            format = getMinutes() > 0 ? "h:mm a" : "h a";
        }
        return new SimpleDateFormat(format).format(c.getTime());
    }
}