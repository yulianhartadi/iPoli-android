package io.ipoli.android.utils;

public class Time {
    private final String time;
    public int hour;
    public int minute;

    public Time(String time) {
        this.time = time;
        setHour();
        setMinute();
    }

    public static Time of(String timeString) {
        return new Time(timeString);
    }

    private void setHour() {
        String[] pieces = time.split(":");
        hour = Integer.parseInt(pieces[0]);
    }

    private void setMinute() {
        String[] pieces = time.split(":");
        minute = Integer.parseInt(pieces[1]);
    }
}