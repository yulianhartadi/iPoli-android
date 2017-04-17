package io.ipoli.android.app.scheduling;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class TimeBlock {
    private final int startMinute;
    private final int endMinute;
    private double probability;

    public TimeBlock(int startMinute, int endMinute) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public TimeBlock(TimeBlock timeBlock) {
        this.startMinute = timeBlock.startMinute;
        this.endMinute = timeBlock.endMinute;
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

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public double getProbability() {
        return probability;
    }

    public boolean doOverlap(int startMinute, int endMinute) {
        return !(getEndMinute() < startMinute || endMinute < getStartMinute());
    }
}
