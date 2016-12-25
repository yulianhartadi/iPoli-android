package io.ipoli.android.quest.data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/25/16.
 */
public class PeriodHistory {
    private Long start;
    private Long end;
    private Integer completedCount;
    private Integer totalCount;

    private String type;

    public enum PeriodType {WEEK, MONTH;}

    public PeriodHistory() {

    }

    public PeriodHistory(Long start, Long end, Integer completedCount, Integer totalCount, PeriodType periodType) {
        this.start = start;
        this.end = end;
        this.completedCount = completedCount;
        this.totalCount = totalCount;
        this.type = periodType.name();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
