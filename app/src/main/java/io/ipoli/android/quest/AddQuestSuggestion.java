package io.ipoli.android.quest;

import android.support.annotation.DrawableRes;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/23/16.
 */
public class AddQuestSuggestion {

    @DrawableRes
    public int icon;
    public String visibleText;
    public String text;

    public AddQuestSuggestion(int icon, String visibleText) {
        this.icon = icon;
        this.visibleText = visibleText;
        text = visibleText;
    }

    public AddQuestSuggestion(int icon, String visibleText, String text) {
        this.icon = icon;
        this.visibleText = visibleText;
        this.text = text;
    }
}
