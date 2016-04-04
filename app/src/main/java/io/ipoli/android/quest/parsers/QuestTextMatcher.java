package io.ipoli.android.quest.parsers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public interface QuestTextMatcher<R> {
    Match match(String text);
    R parse(String text);
    Match partialMatch(String text);
}
