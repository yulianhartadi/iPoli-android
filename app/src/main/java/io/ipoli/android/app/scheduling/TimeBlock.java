package io.ipoli.android.app.scheduling;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class TimeBlock {
    private final int startMinute;
    private int endMinute;

    public TimeBlock(int startMinute, int endMinute) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public int getDuration() {
        return endMinute - startMinute;
    }
}
