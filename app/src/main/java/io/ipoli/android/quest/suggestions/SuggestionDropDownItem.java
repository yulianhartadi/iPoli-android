package io.ipoli.android.quest.suggestions;

import android.support.annotation.DrawableRes;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/16.
 */
public class SuggestionDropDownItem {

    @DrawableRes
    public int icon;
    public String visibleText;
    public String text;

    public SuggestionDropDownItem(int icon, String visibleText) {
        this.icon = icon;
        this.visibleText = visibleText;
        text = visibleText;
    }

    public SuggestionDropDownItem(int icon, String visibleText, String text) {
        this.icon = icon;
        this.visibleText = visibleText;
        this.text = text;
    }
}
