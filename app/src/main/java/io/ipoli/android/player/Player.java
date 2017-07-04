package io.ipoli.android.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.crash.FirebaseCrash;

import org.threeten.bp.DayOfWeek;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.Constants;
import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class Player extends PersistedObject {

    public static final String TYPE = "player";

    private String username;
    private String displayName;
    private String bio;
    private Integer level;
    private String experience;
    private Long coins;
    private Long rewardPoints;
    private Integer avatarCode;
    private List<Pet> pets;
    private Set<String> mostProductiveTimesOfDay;
    private Set<Integer> workDays;
    private Integer workStartMinute;
    private Integer workEndMinute;
    private Integer sleepStartMinute;
    private Integer sleepEndMinute;
    private Boolean use24HourFormat;
    private Integer schemaVersion;
    private Integer completeDailyQuestsEndMinute;
    private AuthProvider currentAuthProvider;
    private List<AuthProvider> authProviders;
    private Map<Long, Category> androidCalendars;
    private Inventory inventory;

    public Player() {
        super(TYPE);
    }

    public Player(String username, String displayName, String experience, int level, long coins, long rewardPoints, Integer avatarCode, boolean use24HourFormat, Pet pet) {
        super(TYPE);
        setUsername(username);
        setDisplayName(displayName);
        pets = new ArrayList<>();
        pets.add(pet);
        this.schemaVersion = Constants.SCHEMA_VERSION;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());

        this.experience = experience;
        this.level = level;
        this.coins = coins;
        this.rewardPoints = rewardPoints;
        this.avatarCode = avatarCode;
        setMostProductiveTimesOfDaySet(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES);
        setWorkDays(Constants.DEFAULT_PLAYER_WORK_DAYS);
        setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE);
        setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE);
        setSleepStartMinute(Constants.DEFAULT_PLAYER_SLEEP_START_MINUTE);
        setSleepEndMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE);
        setCompleteDailyQuestsEndMinute(Constants.DEFAULT_PLAYER_COMPLETE_DAILY_QUESTS_MINUTE);
        setUse24HourFormat(use24HourFormat);
        setAndroidCalendars(new HashMap<>());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Pet> getPets() {
        return pets;
    }

    @JsonIgnore
    public Pet getPet() {
        return getPets().get(0);
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

    @JsonIgnore
    public void addCoins(long coins) {
        this.coins += coins;
    }

    @JsonIgnore
    public void removeCoins(long coins) {
        this.coins = Math.max(0, this.coins - coins);
    }

    @JsonIgnore
    public void addRewardPoints(long points) {
        this.rewardPoints = getRewardPoints() + points;
    }

    @JsonIgnore
    public void removeRewardPoints(long points) {
        this.rewardPoints = Math.max(0, getRewardPoints() - points);
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

    public Integer getAvatarCode() {
        return avatarCode;
    }

    public void setAvatarCode(Integer avatarCode) {
        this.avatarCode = avatarCode;
    }

    public Set<String> getMostProductiveTimesOfDay() {
        if (mostProductiveTimesOfDay == null) {
            mostProductiveTimesOfDay = new HashSet<>();
        }
        return mostProductiveTimesOfDay;
    }

    @JsonIgnore
    public Set<TimeOfDay> getMostProductiveTimesOfDaySet() {
        Set<TimeOfDay> timesOfDay = new HashSet<>();
        if (mostProductiveTimesOfDay == null) {
            mostProductiveTimesOfDay = new HashSet<>();
            return timesOfDay;
        }
        for (String timeOfDay : mostProductiveTimesOfDay) {
            timesOfDay.add(TimeOfDay.valueOf(timeOfDay));
        }
        return timesOfDay;
    }

    public void setMostProductiveTimesOfDay(Set<String> mostProductiveTimesOfDay) {
        this.mostProductiveTimesOfDay = mostProductiveTimesOfDay;
    }


    @JsonIgnore
    public void setMostProductiveTimesOfDaySet(Set<TimeOfDay> timesOfDay) {
        mostProductiveTimesOfDay = new HashSet<>();
        for (TimeOfDay timeOfDay : timesOfDay) {
            mostProductiveTimesOfDay.add(timeOfDay.name());
        }
    }

    public Set<Integer> getWorkDays() {
        if (workDays == null) {
            workDays = new HashSet<>();
        }
        return workDays;
    }

    public void setWorkDays(Set<Integer> workDays) {
        this.workDays = workDays;
    }

    @JsonIgnore
    public void setDayOfWeekWorkDays(Set<DayOfWeek> workDays) {
        setWorkDays(DateUtils.toIntegers(workDays));
    }

    @JsonIgnore
    public Set<DayOfWeek> getDayOfWeekWorkDays() {
        return DateUtils.toDaysOfWeek(getWorkDays());
    }

    public Integer getWorkStartMinute() {
        return workStartMinute;
    }

    @JsonIgnore
    public Time getWorkStartTime() {
        if (getWorkStartMinute() == null) {
            return null;
        }
        return Time.of(getWorkStartMinute());
    }

    public void setWorkStartMinute(Integer workStartMinute) {
        this.workStartMinute = workStartMinute;
    }

    @JsonIgnore
    public void setWorkStartTime(Time startTime) {
        if (startTime != null) {
            setWorkStartMinute(startTime.toMinuteOfDay());
        } else {
            setWorkStartMinute(null);
        }
    }

    public Integer getWorkEndMinute() {
        return workEndMinute;
    }

    @JsonIgnore
    public Time getWorkEndTime() {
        if (getWorkEndMinute() == null) {
            return null;
        }
        return Time.of(getWorkEndMinute());
    }

    public void setWorkEndMinute(Integer workEndMinute) {
        this.workEndMinute = workEndMinute;
    }

    @JsonIgnore
    public void setWorkEndTime(Time endTime) {
        if (endTime != null) {
            setWorkEndMinute(endTime.toMinuteOfDay());
        } else {
            setWorkEndMinute(null);
        }
    }

    public Integer getSleepStartMinute() {
        return sleepStartMinute;
    }

    @JsonIgnore
    public Time getSleepStartTime() {
        if (getSleepStartMinute() == null) {
            return null;
        }
        return Time.of(getSleepStartMinute());
    }

    public void setSleepStartMinute(Integer sleepStartMinute) {
        this.sleepStartMinute = sleepStartMinute;
    }

    @JsonIgnore
    public void setSleepStartTime(Time startTime) {
        if (startTime != null) {
            setSleepStartMinute(startTime.toMinuteOfDay());
        } else {
            setSleepStartMinute(null);
        }
    }

    public Integer getSleepEndMinute() {
        return sleepEndMinute;
    }


    @JsonIgnore
    public Time getSleepEndTime() {
        if (getSleepEndMinute() == 0) {
            return null;
        }
        return Time.of(getSleepEndMinute());
    }

    public void setSleepEndMinute(Integer sleepEndMinute) {
        this.sleepEndMinute = sleepEndMinute;
    }


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

    public Integer getCompleteDailyQuestsEndMinute() {
        return completeDailyQuestsEndMinute;
    }

    public void setCompleteDailyQuestsEndMinute(Integer completeDailyQuestsEndMinute) {
        this.completeDailyQuestsEndMinute = completeDailyQuestsEndMinute;
    }

    @JsonIgnore
    public void setCompleteDailyQuestsEndTime(Time completeDailyQuestsEndTime) {
        if (completeDailyQuestsEndTime != null) {
            setCompleteDailyQuestsEndMinute(completeDailyQuestsEndTime.toMinuteOfDay());
        } else {
            setCompleteDailyQuestsEndMinute(null);
        }
    }

    public AuthProvider getCurrentAuthProvider() {
        return currentAuthProvider;
    }

    public void setCurrentAuthProvider(AuthProvider currentAuthProvider) {
        this.currentAuthProvider = currentAuthProvider;
    }

    public List<AuthProvider> getAuthProviders() {
        if (authProviders == null) {
            authProviders = new ArrayList<>();
        }
        return authProviders;
    }

    public void setAuthProviders(List<AuthProvider> authProviders) {
        this.authProviders = authProviders;
    }

    @JsonIgnore
    public boolean isAuthenticated() {
        return currentAuthProvider != null;
    }

    @JsonIgnore
    public boolean isGuest() {
        return currentAuthProvider == null;
    }

    public Map<Long, Category> getAndroidCalendars() {
        if (androidCalendars == null) {
            androidCalendars = new HashMap<>();
        }
        return androidCalendars;
    }

    public void setAndroidCalendars(Map<Long, Category> androidCalendars) {
        this.androidCalendars = androidCalendars;
    }

    public Inventory getInventory() {
        if (inventory == null) {
            inventory = new Inventory();
        }
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Long getRewardPoints() {
        if (rewardPoints == null) {
            FirebaseCrash.report(new RuntimeException("Player with id " + getId() + " has no rewardPoints set"));
            return coins;
        }
        return rewardPoints;
    }

    public void setRewardPoints(Long rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    @JsonIgnore
    public Avatar getCurrentAvatar() {
        if (avatarCode == null) {
            FirebaseCrash.report(new RuntimeException("Player with id " + getId() + " has no avatarCode set"));
            return Constants.DEFAULT_PLAYER_AVATAR;
        }
        return Avatar.get(avatarCode);
    }

    @JsonIgnore
    public String getTitle(String[] playerTitles) {
        return playerTitles[Math.min(getLevel() / 10, playerTitles.length - 1)];
    }

    @JsonIgnore
    public void setAvatar(Avatar avatar) {
        avatarCode = avatar.code;
    }

    public String getDisplayName() {
        if (StringUtils.isNotEmpty(displayName)) {
            return displayName;
        }

        if (!isAuthenticated()) {
            throw new IllegalStateException("Asking for display name of unauthenticated player " + getId());
        }
        AuthProvider authProvider = getCurrentAuthProvider();
        return authProvider.getFirstName() + " " + authProvider.getLastName();
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @JsonIgnore
    public boolean doesNotHaveUsername() {
        return StringUtils.isEmpty(username);
    }

    @JsonIgnore
    public boolean hasUsername() {
        return !doesNotHaveUsername();
    }
}