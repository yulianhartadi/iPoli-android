package io.ipoli.android.quest.parsers;

import io.ipoli.android.quest.suggestions.MatcherType;
import io.ipoli.android.quest.suggestions.TextEntityType;
import io.ipoli.android.quest.suggestions.providers.FlexibleSuggestionsProvider;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/14/16.
 */
public class FlexibleMatcher extends BaseMatcher<Void> {
    public FlexibleMatcher() {
        super(new FlexibleSuggestionsProvider());
    }

    @Override
    protected boolean partiallyMatches(String text) {
        return false;
    }

    @Override
    public Match match(String text) {
        return null;
    }

    @Override
    public Void parse(String text) {
        return null;
    }

    @Override
    public MatcherType getMatcherType() {
        return MatcherType.DATE;
    }

    @Override
    public TextEntityType getTextEntityType() {
        return TextEntityType.FLEXIBLE;
    }
}
