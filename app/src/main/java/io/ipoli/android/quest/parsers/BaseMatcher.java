package io.ipoli.android.quest.parsers;

import io.ipoli.android.quest.suggestions.providers.SuggestionsProvider;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/4/16.
 */
public abstract class BaseMatcher<R> implements QuestTextMatcher<R> {

    protected SuggestionsProvider suggestionsProvider;

    public BaseMatcher(SuggestionsProvider suggestionsProvider) {
        this.suggestionsProvider = suggestionsProvider;
    }

    @Override
    public Match partialMatch(String text) {
        String currText = text;
        while (!currText.isEmpty()) {
            if (partiallyMatches(currText) && !currText.trim().isEmpty()) {
                int start = text.length() - currText.length();
                int end = text.length() - 1;
                return new Match(currText, start, end);
            }

            int firstSpaceIndex = currText.indexOf(" ");
            currText = firstSpaceIndex >= 0 ? currText.substring(firstSpaceIndex + 1) : "";
        }
        return null;
    }

    @Override
    public SuggestionsProvider getSuggestionsProvider() {
        return suggestionsProvider;
    }

    protected abstract boolean partiallyMatches(String text);

}
