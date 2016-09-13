package io.ipoli.android.challenge.viewmodels;

import android.support.annotation.DrawableRes;

import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/13/16.
 */
public class PickChallengeViewModel {
    private final String name;
    private final String description;
    private final int picture;
    private final Category category;

    public PickChallengeViewModel(String name, String description, @DrawableRes int picture, Category category) {
        this.name = name;
        this.description = description;
        this.picture = picture;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPicture() {
        return picture;
    }

    public Category getCategory() {
        return category;
    }
}
