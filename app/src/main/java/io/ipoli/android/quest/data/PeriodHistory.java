package io.ipoli.android.quest.data;

import org.threeten.bp.LocalDate;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/25/16.
 */
public class PeriodHistory {
    private LocalDate startDate;
    private LocalDate endDate;
    private int completedCount;
    private int totalCount;
    private int scheduledCount;

    public PeriodHistory(LocalDate startDate, LocalDate endDate, int totalCount) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCount = totalCount;
        this.completedCount = 0;
    }

    public PeriodHistory(LocalDate startDate, LocalDate endDate) {
        this(startDate, endDate, -1);
    }

    public void increaseCompletedCount() {
        completedCount++;
        totalCount = Math.max(completedCount, totalCount);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public int getRemainingCount() {
        return getTotalCount() - getCompletedCount();
    }

    public int getRemainingScheduledCount() {
        return getScheduledCount() - getCompletedCount();
    }

    public void increaseScheduledCount() {
        scheduledCount++;
    }

    public int getScheduledCount() {
        return scheduledCount;
    }
}
