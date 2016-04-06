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
    public TextEntityType nextTextEntityType;
    public boolean shouldReplace;

    public SuggestionDropDownItem(int icon, String visibleText) {
        this(icon, visibleText, visibleText, null, true);
    }

    public SuggestionDropDownItem(int icon, String visibleText, String text) {
        this(icon, visibleText, text, null, true);
    }

    public SuggestionDropDownItem(int icon, String visibleText, String text, TextEntityType nextTextEntityType) {
        this(icon, visibleText, text, nextTextEntityType, true);
    }

    public SuggestionDropDownItem(int icon, String visibleText, String text, TextEntityType nextTextEntityType, boolean shouldReplace) {
        this.icon = icon;
        this.visibleText = visibleText;
        this.text = text;
        this.nextTextEntityType = nextTextEntityType;
        this.shouldReplace = shouldReplace;
    }
}
