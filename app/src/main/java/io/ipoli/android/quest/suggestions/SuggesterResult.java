package io.ipoli.android.quest.suggestions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/29/16.
 */
public class SuggesterResult {
    private String match;
    private SuggesterState state;
    private SuggestionType nextSuggesterType;
    private int nextSuggesterStartIdx;

    public SuggesterResult(SuggesterState state) {
        this.state = state;
    }

    public SuggesterResult(String match, SuggesterState state) {
        this.match = match;
        this.state = state;
    }

    public SuggesterResult(String match, SuggesterState state, SuggestionType nextSuggesterType, int nextSuggesterStartIdx) {
        this.match = match;
        this.state = state;
        this.nextSuggesterType = nextSuggesterType;
        this.nextSuggesterStartIdx = nextSuggesterStartIdx;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public SuggesterState getState() {
        return state;
    }

    public void setState(SuggesterState state) {
        this.state = state;
    }

    public SuggestionType getNextSuggesterType() {
        return nextSuggesterType;
    }

    public void setNextSuggesterType(SuggestionType nextSuggesterType) {
        this.nextSuggesterType = nextSuggesterType;
    }

    public int getNextSuggesterStartIdx() {
        return nextSuggesterStartIdx;
    }

    public void setNextSuggesterStartIdx(int nextSuggesterStartIdx) {
        this.nextSuggesterStartIdx = nextSuggesterStartIdx;
    }
}
