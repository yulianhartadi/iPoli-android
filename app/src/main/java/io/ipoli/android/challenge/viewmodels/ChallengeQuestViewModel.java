package io.ipoli.android.challenge.viewmodels;

import io.ipoli.android.quest.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/13/16.
 */
public class ChallengeQuestViewModel {

    private String id;
    private String name;
    private Category category;
    private boolean isRepeating;

    public ChallengeQuestViewModel(String id, String name, Category category, boolean isRepeating) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.isRepeating = isRepeating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }
}
