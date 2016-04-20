package io.ipoli.android.quest.parsers;

import io.ipoli.android.quest.suggestions.MatcherType;
import io.ipoli.android.quest.suggestions.TextEntityType;
import io.ipoli.android.quest.suggestions.providers.MainSuggestionsProvider;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/14/16.
 */
public class MainMatcher extends BaseMatcher<Void> {
    public MainMatcher() {
        super(new MainSuggestionsProvider());
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
        return MatcherType.MAIN;
    }

    @Override
    public TextEntityType getTextEntityType() {
        return TextEntityType.MAIN;
    }
}
