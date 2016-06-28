package io.ipoli.android.tutorial;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/28/16.
 */
public class PickQuestViewModel<T> {
    private String text;
    private T quest;
    private boolean isSelected;
    private boolean isCompleted;

    public PickQuestViewModel(T quest, String text) {
        this(quest, text, false);
    }

    public PickQuestViewModel(T quest, String text, boolean isSelected) {
        this.quest = quest;
        this.isSelected = isSelected;
        this.text = text;
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

    public T getQuest() {
        return quest;
    }

    public void markCompleted() {
        isCompleted = true;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
