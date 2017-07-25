package io.ipoli.android.achievement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.threeten.bp.LocalDate;

import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class ActionCountPerDay {
    private Integer count;
    private Long date;

    public ActionCountPerDay(int count, LocalDate date) {
        this.count = count;
        this.date = DateUtils.toMillis(date);
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    @JsonIgnore
    public void increment() {
        count++;
    }

    @JsonIgnore
    public void increment(int count) {
        this.count += count;
    }
}
