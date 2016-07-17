package io.ipoli.android.tutorial;

import java.util.Date;

import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.data.BaseQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/28/16.
 */
public class PickQuestViewModel {
    private String text;
    private boolean isRepeating;
    private BaseQuest baseQuest;
    private boolean isSelected;
    private boolean isCompleted;
    private Date startDate;

    public PickQuestViewModel(BaseQuest baseQuest, String text) {
        this(baseQuest, text, false, false);
    }

    public PickQuestViewModel(BaseQuest baseQuest, String text, boolean isSelected) {
        this(baseQuest, text, isSelected, false);
    }

    public PickQuestViewModel(BaseQuest baseQuest, String text, Date startDate, boolean isRepeating) {
        this(baseQuest, text, startDate, false, isRepeating);
    }

    public PickQuestViewModel(BaseQuest baseQuest, String text, boolean isSelected, boolean isRepeating) {
        this(baseQuest, text, null, isSelected, isRepeating);
    }

    public PickQuestViewModel(BaseQuest baseQuest, String text, Date startDate, boolean isSelected, boolean isRepeating) {
        this.baseQuest = baseQuest;
        this.isSelected = isSelected;
        this.text = text;
        this.startDate = startDate;
        this.isRepeating = isRepeating;
        this.isCompleted = false;
    }

    public String getText() {
        return text;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void select() {
        isSelected = true;
    }

    public void deselect() {
        isSelected = false;
    }

    public BaseQuest getBaseQuest() {
        return baseQuest;
    }

    public void markCompleted() {
        isCompleted = true;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public Category getCategory() {
        return baseQuest.getCategory();
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public Date getStartDate() {
        return startDate;
    }
}
