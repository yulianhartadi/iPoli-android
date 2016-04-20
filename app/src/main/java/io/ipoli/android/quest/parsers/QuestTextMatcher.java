package io.ipoli.android.quest.parsers;

import io.ipoli.android.quest.suggestions.MatcherType;
import io.ipoli.android.quest.suggestions.TextEntityType;
import io.ipoli.android.quest.suggestions.providers.SuggestionsProvider;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public interface QuestTextMatcher<R> {
    Match match(String text);
    R parse(String text);
    Match partialMatch(String text);
    MatcherType getMatcherType();
    TextEntityType getTextEntityType();
    SuggestionsProvider getSuggestionsProvider();
}
