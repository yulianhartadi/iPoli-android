package io.ipoli.android.app.utils;

import java.util.Calendar;
import java.util.Date;

public class Time {
    private final int hours;
    private final int minutes;

    private Time(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }

    public static Time at(String timeString) {
        return new Time(parseHours(timeString), parseMinutes(timeString));
    }

    public static Time at(int hours, int minutes) {
        return new Time(hours, minutes);
    }

    public static Time after(int hours, int minutes) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, hours);
        c.add(Calendar.MINUTE, minutes);
        return new Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    private static int parseHours(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[0]);
    }

    private static int parseMinutes(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[1]);
    }

    public Date toDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        return c.getTime();
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public static Time now() {
        Calendar c = Calendar.getInstance();
        return new Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }
}