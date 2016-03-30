package io.ipoli.android.quest.parsers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public interface QuestTextMatcher<R> {
    String match(String text);
    R parse(String text);
    boolean partiallyMatches(String text);
}
