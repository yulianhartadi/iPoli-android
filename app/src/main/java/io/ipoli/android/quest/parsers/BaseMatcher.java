package io.ipoli.android.quest.parsers;

import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/4/16.
 */
public abstract class BaseMatcher<R> implements QuestTextMatcher<R> {

    @Override
    public Match partialMatch(String text) {
        List<String> words = Arrays.asList(text.split(" "));
        Collections.reverse(words);

        String currText = text.endsWith(" ") ? " " : "";

        Match match = null;
        for (int i = 0; i < words.size(); i ++){
            currText = i == 0 ? words.get(i) + currText : words.get(i) + " " + currText;
            boolean partiallyMatches = partiallyMatches(currText);
            if (partiallyMatches && currText.length() > 2) {
                int start = text.length() - currText.length();
                int end = text.length() - 1;
                match = new Match(currText, start, end);
                if(i == words.size() -1) {
                    return match;
                }
            } else if(!partiallyMatches && match != null){
                return match;
            }
        }

        return null;
    }

    protected abstract boolean partiallyMatches(String text);

}
