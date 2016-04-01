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
    public SuggestionType nextSuggestionType;

    public SuggestionDropDownItem(int icon, String visibleText) {
        this.icon = icon;
        this.visibleText = visibleText;
        text = visibleText;
        nextSuggestionType = null;
    }

    public SuggestionDropDownItem(int icon, String visibleText, SuggestionType nextSuggestionType) {
        this.icon = icon;
        this.visibleText = visibleText;
        text = visibleText;
        this.nextSuggestionType = nextSuggestionType;
    }

    public SuggestionDropDownItem(int icon, String visibleText, String text) {
        this.icon = icon;
        this.visibleText = visibleText;
        this.text = text;
        nextSuggestionType = null;
    }

    public SuggestionDropDownItem(int icon, String visibleText, String text, SuggestionType nextSuggestionType) {
        this.icon = icon;
        this.visibleText = visibleText;
        this.text = text;
        this.nextSuggestionType = nextSuggestionType;
    }

    public boolean hasNextSuggestionType() {
        return nextSuggestionType != null;
    }
}
