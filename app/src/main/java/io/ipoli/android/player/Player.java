package io.ipoli.android.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.Exclude;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.pet.data.Pet;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class Player extends PersistedObject {

    private Integer level;
    private String experience;
    private Long coins;
    private String picture;
    private List<Pet> pets;
    private List<String> mostProductiveTimesOfDay;
    private List<Integer> workDays;
    private Integer workStartMinute;
    private Integer workEndMinute;
    private Integer sleepStartMinute;
    private Integer sleepEndMinute;
    private Boolean use24HourFormat;
    private Integer schemaVersion;

    public Player() {
    }

    public Player(String experience, int level, long coins, String picture, boolean use24HourFormat, Pet pet) {
        pets = new ArrayList<>();
        pets.add(pet);
        this.schemaVersion = Constants.SCHEMA_VERSION;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());

        this.experience = experience;
        this.level = level;
        this.coins = coins;
        this.picture = picture;
        setMostProductiveTimesOfDayList(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIME);
        setWorkDays(Constants.DEFAULT_PLAYER_WORK_DAYS);
        setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE);
        setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE);
        setSleepStartMinute(Constants.DEFAULT_PLAYER_SLEEP_START_MINUTE);
        setSleepEndMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE);
        setUse24HourFormat(use24HourFormat);
    }

    public List<Pet> getPets() {
        return pets;
    }

    @JsonIgnore
    public Pet getPet() {
        return getPets().get(0);
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

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    @JsonIgnore
    public void addExperience(long experience) {
        this.experience = new BigInteger(this.experience).add(new BigInteger(String.valueOf(experience))).toString();
    }

    @JsonIgnore
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
        if (mostProductiveTimesOfDay == null) {
            mostProductiveTimesOfDay = new ArrayList<>();
        }
        return mostProductiveTimesOfDay;
    }

    @Exclude
    @JsonIgnore
    public List<TimeOfDay> getMostProductiveTimesOfDayList() {
        List<TimeOfDay> timesOfDay = new ArrayList<>();
        if (mostProductiveTimesOfDay == null) {
            mostProductiveTimesOfDay = new ArrayList<>();
            return timesOfDay;
        }
        for (String timeOfDay : mostProductiveTimesOfDay) {
            timesOfDay.add(TimeOfDay.valueOf(timeOfDay));
        }
        return timesOfDay;
    }

    public void setMostProductiveTimesOfDay(List<String> mostProductiveTimesOfDay) {
        this.mostProductiveTimesOfDay = mostProductiveTimesOfDay;
    }

    @Exclude
    @JsonIgnore
    public void setMostProductiveTimesOfDayList(List<TimeOfDay> timesOfDay) {
        mostProductiveTimesOfDay = new ArrayList<>();
        for (TimeOfDay timeOfDay : timesOfDay) {
            mostProductiveTimesOfDay.add(timeOfDay.name());
        }
    }

    public List<Integer> getWorkDays() {
        if (workDays == null) {
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
    @JsonIgnore
    public Time getWorkStartTime() {
        if (getWorkStartMinute() < 0) {
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
            setWorkStartMinute(startTime.toMinuteOfDay());
        } else {
            setWorkStartMinute(null);
        }
    }

    public Integer getWorkEndMinute() {
        return workEndMinute != null ? workEndMinute : -1;
    }

    @Exclude
    public Time getWorkEndTime() {
        if (getWorkEndMinute() < 0) {
            return null;
        }
        return Time.of(getWorkEndMinute());
    }

    public void setWorkEndMinute(Integer workEndMinute) {
        this.workEndMinute = workEndMinute;
    }

    @Exclude
    @JsonIgnore
    public void setWorkEndTime(Time endTime) {
        if (endTime != null) {
            setWorkEndMinute(endTime.toMinuteOfDay());
        } else {
            setWorkEndMinute(null);
        }
    }

    public Integer getSleepStartMinute() {
        return sleepStartMinute != null ? sleepStartMinute : -1;
    }

    @Exclude
    @JsonIgnore
    public Time getSleepStartTime() {
        if (getSleepStartMinute() < 0) {
            return null;
        }
        return Time.of(getSleepStartMinute());
    }

    public void setSleepStartMinute(Integer sleepStartMinute) {
        this.sleepStartMinute = sleepStartMinute;
    }

    @Exclude
    @JsonIgnore
    public void setSleepStartTime(Time startTime) {
        if (startTime != null) {
            setSleepStartMinute(startTime.toMinuteOfDay());
        } else {
            setSleepStartMinute(null);
        }
    }

    public Integer getSleepEndMinute() {
        return sleepEndMinute != null ? sleepEndMinute : -1;
    }

    @Exclude
    @JsonIgnore
    public Time getSleepEndTime() {
        if (getSleepEndMinute() < 0) {
            return null;
        }
        return Time.of(getSleepEndMinute());
    }

    public void setSleepEndMinute(Integer sleepEndMinute) {
        this.sleepEndMinute = sleepEndMinute;
    }

    @Exclude
    @JsonIgnore
    public void setSleepEndTime(Time endTime) {
        if (endTime != null) {
            setSleepEndMinute(endTime.toMinuteOfDay());
        } else {
            setSleepEndMinute(null);
        }
    }

    public Boolean getUse24HourFormat() {
        return use24HourFormat;
    }

    public void setUse24HourFormat(Boolean use24HourFormat) {
        this.use24HourFormat = use24HourFormat;
    }
}