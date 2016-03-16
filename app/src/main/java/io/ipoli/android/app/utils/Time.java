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

    public static Time atMinutes(int minutes) {
        Calendar c = Calendar.getInstance();
        return at(c.get(Calendar.HOUR_OF_DAY), minutes);
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
        c.add(Calendar.HOUR_OF_DAY, - hours);
        c.add(Calendar.MINUTE, - minutes);
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

    public static Time of(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return new Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }
}