package io.ipoli.android.achievement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.threeten.bp.LocalDate;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class AchievementsProgress extends PersistedObject {

    public static final String TYPE = "achievementProgress";

    private Integer completedQuestCount;
    private ActionCountPerDay completedQuestsInADay;
    private ActionCountPerDay experienceInADay;
    private ActionCountPerDay completedQuestsInARow;

    public AchievementsProgress() {
        super(TYPE);
    }

    public static AchievementsProgress create() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setCompletedQuestsInADay(new ActionCountPerDay(0, LocalDate.now()));
        progress.setExperienceInADay(new ActionCountPerDay(0, LocalDate.now()));
        progress.setCompletedQuestsInARow(new ActionCountPerDay(0, LocalDate.now()));
        progress.setCompletedQuestCount(0);
        return progress;
    }

    @JsonIgnore
    public void incrementCompletedQuestCount() {
        completedQuestCount++;
    }

    @JsonIgnore
    public void incrementCompletedQuestsInADay() {
        long today = DateUtils.toMillis(LocalDate.now());
        if (completedQuestsInADay.getDate() == today) {
            completedQuestsInADay.increment();
        } else {
            completedQuestsInADay.setDate(today);
            completedQuestsInADay.setCount(1);
        }
    }

    @JsonIgnore
    public void incrementExperienceInADay(int experience) {
        long today = DateUtils.toMillis(LocalDate.now());
        if (experienceInADay.getDate() == today) {
            experienceInADay.increment(experience);
        } else {
            experienceInADay.setDate(today);
            experienceInADay.setCount(experience);
        }
    }

    @JsonIgnore
    public void incrementCompletedQuestsInARow() {
        long today = DateUtils.toMillis(LocalDate.now());
        long yesterday = DateUtils.toMillis(LocalDate.now().minusDays(1));
        if (completedQuestsInARow.getDate() == yesterday) {
            completedQuestsInARow.increment();
            completedQuestsInARow.setDate(today);
        } else if (completedQuestsInARow.getDate() < yesterday) {
            completedQuestsInARow.setDate(today);
            completedQuestsInARow.setCount(1);
        }
    }

    public int getCompletedQuestCount() {
        return completedQuestCount;
    }

    public void setCompletedQuestCount(int completedQuestCount) {
        this.completedQuestCount = completedQuestCount;
    }

    public ActionCountPerDay getCompletedQuestsInADay() {
        return completedQuestsInADay;
    }

    public void setCompletedQuestsInADay(ActionCountPerDay completedQuestsInADay) {
        this.completedQuestsInADay = completedQuestsInADay;
    }

    public void setExperienceInADay(ActionCountPerDay experienceInADay) {
        this.experienceInADay = experienceInADay;
    }

    public ActionCountPerDay getExperienceInADay() {
        return experienceInADay;
    }

    public void setCompletedQuestsInARow(ActionCountPerDay completedQuestsInARow) {
        this.completedQuestsInARow = completedQuestsInARow;
    }

    public ActionCountPerDay getCompletedQuestsInARow() {
        return completedQuestsInARow;
    }
}