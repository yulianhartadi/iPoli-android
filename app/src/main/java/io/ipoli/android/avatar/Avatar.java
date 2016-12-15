package io.ipoli.android.avatar;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/16.
 */
@IgnoreExtraProperties
public class Avatar extends PersistedObject {

    private String experience;
    private Integer level;
    private Long coins;
    private String picture;
    private List<String> mostProductiveTimesOfDay;
    private List<Integer> workDays;
    private Integer workStartMinute;
    private Integer workEndMinute;
    private Integer sleepStartMinute;
    private Integer sleepEndMinute;

    public Avatar() {
    }

    public Avatar(String experience, int level, long coins, String picture) {
        this.experience = experience;
        this.level = level;
        this.coins = coins;
        this.picture = picture;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
        setMostProductiveTimesOfDayList(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIME);
        setWorkDays(Constants.DEFAULT_PLAYER_WORK_DAYS);
        setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE);
        setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE);
        setSleepStartMinute(Constants.DEFAULT_PLAYER_SLEEP_START_MINUTE);
        setSleepEndMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE);
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    public void addExperience(long experience) {
        this.experience = new BigInteger(this.experience).add(new BigInteger(String.valueOf(experience))).toString();
    }

    public void removeExperience(long experience) {
        BigInteger newXP = new BigInteger(this.experience).subtract(new BigInteger(String.valueOf(experience)));
        if (newXP.compareTo(BigInteger.ZERO) < 0) {
            newXP = BigInteger.ZERO;
        }
        this.experience = newXP.toString();
    }

    public void addCoins(long coins) {
        this.coins += coins;
    }

    public void removeCoins(long coins) {
        this.coins = Math.max(0, this.coins - coins);
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public List<String> getMostProductiveTimesOfDay() {
        if(mostProductiveTimesOfDay == null) {
            mostProductiveTimesOfDay = new ArrayList<>();
        }
        return mostProductiveTimesOfDay;
    }

    @Exclude
    public List<TimeOfDay> getMostProductiveTimesOfDayList() {
        List<TimeOfDay> timesOfDay = new ArrayList<>();
        if(mostProductiveTimesOfDay == null) {
            mostProductiveTimesOfDay = new ArrayList<>();
            return timesOfDay;
        }
        for(String timeOfDay : mostProductiveTimesOfDay) {
            timesOfDay.add(TimeOfDay.valueOf(timeOfDay));
        }
        return timesOfDay;
    }

    public void setMostProductiveTimesOfDay(List<String> mostProductiveTimesOfDay) {
        this.mostProductiveTimesOfDay = mostProductiveTimesOfDay;
    }

    @Exclude
    public void setMostProductiveTimesOfDayList(List<TimeOfDay> timesOfDay) {
        mostProductiveTimesOfDay = new ArrayList<>();
        for (TimeOfDay timeOfDay : timesOfDay) {
            mostProductiveTimesOfDay.add(timeOfDay.name());
        }
    }

    public List<Integer> getWorkDays() {
        if(workDays == null) {
            workDays = new ArrayList<>();
        }
        return workDays;
    }

    public void setWorkDays(List<Integer> workDays) {
        this.workDays = workDays;
    }

    public Integer getWorkStartMinute() {
        return workStartMinute != null ? workStartMinute : -1;
    }

    @Exclude
    public Time getWorkStartTime() {
        if(getWorkStartMinute() < 0) {
            return null;
        }
        return Time.of(getWorkStartMinute());
    }

    public void setWorkStartMinute(Integer workStartMinute) {
        this.workStartMinute = workStartMinute;
    }

    @Exclude
    public void setWorkStartTime(Time startTime) {
        if (startTime != null) {
            setWorkStartMinute(startTime.toMinutesAfterMidnight());
        } else {
            setWorkStartMinute(null);
        }
    }

    public Integer getWorkEndMinute() {
        return workEndMinute != null ? workEndMinute : -1;
    }

    @Exclude
    public Time getWorkEndTime() {
        if(getWorkEndMinute() < 0) {
            return null;
        }
        return Time.of(getWorkEndMinute());
    }

    public void setWorkEndMinute(Integer workEndMinute) {
        this.workEndMinute = workEndMinute;
    }

    @Exclude
    public void setWorkEndTime(Time endTime) {
        if (endTime != null) {
            setWorkEndMinute(endTime.toMinutesAfterMidnight());
        } else {
            setWorkEndMinute(null);
        }
    }

    public Integer getSleepStartMinute() {
        return sleepStartMinute != null ? sleepStartMinute : -1;
    }

    @Exclude
    public Time getSleepStartTime() {
        if(getSleepStartMinute() < 0) {
            return null;
        }
        return Time.of(getSleepStartMinute());
    }

    public void setSleepStartMinute(Integer sleepStartMinute) {
        this.sleepStartMinute = sleepStartMinute;
    }

    @Exclude
    public void setSleepStartTime(Time startTime) {
        if (startTime != null) {
            setSleepStartMinute(startTime.toMinutesAfterMidnight());
        } else {
            setSleepStartMinute(null);
        }
    }

    public Integer getSleepEndMinute() {
        return sleepEndMinute != null ? sleepEndMinute : -1;
    }

    @Exclude
    public Time getSleepEndTime() {
        if(getSleepEndMinute() < 0) {
            return null;
        }
        return Time.of(getSleepEndMinute());
    }

    public void setSleepEndMinute(Integer sleepEndMinute) {
        this.sleepEndMinute = sleepEndMinute;
    }

    @Exclude
    public void setSleepEndTime(Time endTime) {
        if (endTime != null) {
            setSleepEndMinute(endTime.toMinutesAfterMidnight());
        } else {
            setSleepEndMinute(null);
        }
    }


}
