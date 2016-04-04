package io.ipoli.android.quest.suggestions.suggesters;

import java.util.List;

import io.ipoli.android.quest.parsers.QuestTextMatcher;
import io.ipoli.android.quest.suggestions.SuggesterResult;
import io.ipoli.android.quest.suggestions.SuggesterState;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.TextSuggester;

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
//        String textToParse = text.substring(startIdx);
//        Match match = matcher.match(textToParse);
//        if (match != null) {
//            lastParsedText = match.text;
//        }
//
//        boolean partiallyMatches = matcher.partiallyMatches(textToParse);
//        Log.d("Sugg partiallyMatch", partiallyMatches + " " + matcher.toString());
//        if(partiallyMatches) {
//            length = textToParse.length();
//            return new SuggesterResult(textToParse, SuggesterState.CONTINUE);
//        } else {
//            if(match == null) {
                return new SuggesterResult(SuggesterState.CANCEL);
//            } else {
//                length = match.text.length();
//                lastParsedText = "";
//                return new SuggesterResult(match.text, SuggesterState.FINISH);
//            }
//        }
    }

    @Override
    public List<SuggestionDropDownItem> getSuggestions() {
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
