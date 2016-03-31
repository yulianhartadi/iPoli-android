package io.ipoli.android.quest.suggestions;

import android.util.Log;

import java.util.List;

import io.ipoli.android.quest.parsers.QuestTextMatcher;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class BaseTextSuggester implements TextSuggester {
    protected int startIdx = 0;
    protected int length;
    protected String lastParsedText = "";
    protected QuestTextMatcher matcher;

    @Override
    public SuggesterResult parse(String text) {
        String textToParse = text.substring(startIdx);
        String match = matcher.match(textToParse);
        if (!match.isEmpty()) {
            lastParsedText = match;
        }

        boolean partiallyMatches = matcher.partiallyMatches(textToParse);
        Log.d("Sugg partiallyMatch", partiallyMatches + " " + matcher.toString());
        if(partiallyMatches) {
            length = textToParse.length();
            return new SuggesterResult(textToParse, SuggesterState.CONTINUE);
        } else {
            if(lastParsedText.isEmpty()) {
                lastParsedText = "";
                return new SuggesterResult(SuggesterState.CANCEL);
            } else {
                length = match.length();
                lastParsedText = "";
                return new SuggesterResult(match, SuggesterState.FINISH);
            }
        }
    }

    @Override
    public List<AddQuestSuggestion> getSuggestions() {
        return null;
    }

    public String getLastParsedText() {
        return lastParsedText;
    }

    public int getStartIdx() {
        return startIdx;
    }

    public void setStartIdx(int startIdx) {
        this.startIdx = startIdx;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getEndIdx() {
        return length == 0 ? startIdx : startIdx + length - 1;
    }
}
