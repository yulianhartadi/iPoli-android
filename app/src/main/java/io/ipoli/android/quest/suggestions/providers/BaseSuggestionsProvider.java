package io.ipoli.android.quest.suggestions.providers;

import io.ipoli.android.quest.parsers.QuestTextMatcher;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public abstract class BaseSuggestionsProvider implements SuggestionsProvider {
    protected int startIdx = 0;
    protected int length;
    protected QuestTextMatcher matcher;

    public void setStartIdx(int startIdx) {
        this.startIdx = startIdx;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
