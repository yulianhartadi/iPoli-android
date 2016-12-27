package io.ipoli.android.quest.data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/25/16.
 */
public class PeriodHistory {
    private long start;
    private long end;
    private int completedCount;
    private int totalCount;

    private String type;

    public PeriodHistory(long start, long end, int totalCount) {
        this.start = start;
        this.end = end;
        this.totalCount = totalCount;
        this.completedCount = 0;
    }

    public PeriodHistory(long start, long end) {
        this(start, end, -1);
    }

    public void increaseCompletedCount() {
        completedCount++;
        totalCount = Math.max(completedCount, totalCount);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
