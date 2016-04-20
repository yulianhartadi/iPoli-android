package io.ipoli.android.quest.suggestions;

import android.support.annotation.DrawableRes;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/16.
 */
public class SuggestionDropDownItem {

    @DrawableRes
    public final int icon;
    public final String visibleText;
    public final String text;
    public final TextEntityType nextTextEntityType;
    public final boolean shouldReplace;
    public final boolean shouldFinishMatch;

    public SuggestionDropDownItem(int icon, String visibleText) {
        this(icon, visibleText, visibleText, null, true, true);
    }

    public SuggestionDropDownItem(int icon, String visibleText, String text) {
        this(icon, visibleText, text, null, true, true);
    }

    public SuggestionDropDownItem(int icon, String visibleText, String text, TextEntityType nextTextEntityType) {
        this(icon, visibleText, text, nextTextEntityType, true, true);
    }

    public SuggestionDropDownItem(int icon, String visibleText, String text, TextEntityType nextTextEntityType, boolean shouldReplace, boolean shouldFinishMatch) {
        this.icon = icon;
        this.visibleText = visibleText;
        this.text = text;
        this.nextTextEntityType = nextTextEntityType;
        this.shouldReplace = shouldReplace;
        this.shouldFinishMatch = shouldFinishMatch;
    }
}
