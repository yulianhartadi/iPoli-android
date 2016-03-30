package io.ipoli.android.quest.suggestions;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Bus;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.app.App;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/24/16.
 */
public class SuggestionsManager {
    @Inject
    Bus eventBus;

    Context context;

    SuggestionType currentType;
    Map<SuggestionType, BaseTextSuggester> textSuggesters = new HashMap<>();
    Set<SuggestionType> usedTypes = new HashSet<>();
    List<SuggestionType> orderedSuggesterTypes = new ArrayList<SuggestionType>() {{
        add(SuggestionType.DURATION);
        add(SuggestionType.START_TIME);
        add(SuggestionType.DUE_DATE);
        add(SuggestionType.TIMES_PER_DAY);
        add(SuggestionType.RECURRENT);
        add(SuggestionType.MAIN);
    }};

    List<ParsedPart> parsedParts = new ArrayList<>();

    OnSuggestionsUpdatedListener suggestionsUpdatedListener;

    public SuggestionsManager(Context context, PrettyTimeParser parser) {
        this.context = context;
        App.getAppComponent(context).inject(this);
        currentType = SuggestionType.MAIN;

        textSuggesters.put(SuggestionType.MAIN, new MainTextSuggester());
        textSuggesters.put(SuggestionType.DUE_DATE, new DueDateTextSuggester(parser));
        textSuggesters.put(SuggestionType.DURATION, new DurationTextSuggester());
        textSuggesters.put(SuggestionType.START_TIME, new StartTimeTextSuggester(parser));
        textSuggesters.put(SuggestionType.RECURRENT, new RecurrenceTextSuggester());
        textSuggesters.put(SuggestionType.TIMES_PER_DAY, new TimesPerDayTextSuggester());
    }

    public void setSuggestionsUpdatedListener(OnSuggestionsUpdatedListener suggestionsUpdatedListener) {
        this.suggestionsUpdatedListener = suggestionsUpdatedListener;
    }

    public List<AddQuestSuggestion> getSuggestions() {
        return getCurrentSuggester().getSuggestions();
    }

    public List<SuggestionType> getUnusedTypes() {
        List<SuggestionType> types = new ArrayList<>();
        for (SuggestionType t : orderedSuggesterTypes) {
            if (!usedTypes.contains(t)) {
                types.add(t);
            }
        }
        return types;
    }

    public void changeCurrentSuggester(SuggestionType type, int startIdx, int length) {
        if(type == currentType) {
            return;
        }
        Log.d("ChangeCurrentSuggester", "From: " + currentType.name()
        + " To: " + type.name() + " startIdx: " + startIdx + " length: " + length);
        currentType = type;
        getCurrentSuggester().setStartIdx(startIdx);
        getCurrentSuggester().setLength(length);
        suggestionsUpdatedListener.onSuggestionsUpdated();
    }

    public List<ParsedPart> onTextChange(String text, int start, int before, int count) {
        ParsedPart p = getParsedPart(currentType);

        BaseTextSuggester suggester = getCurrentSuggester();
        SuggesterResult r = suggester.parse(text);
        SuggesterState state = r.getState();

        if (state == SuggesterState.CANCEL) {
            usedTypes.remove(currentType);
            parsedParts.remove(p);
            changeCurrentSuggester(SuggestionType.MAIN, text.length(), 0);
        } else if (state == SuggesterState.FINISH) {
            if(currentType != SuggestionType.MAIN) {
                usedTypes.add(currentType);
                p.isPartial = false;
                p.startIdx = suggester.getStartIdx();
                p.endIdx = suggester.getStartIdx() + r.getMatch().length() - 1;
            }

            SuggestionType next = SuggestionType.MAIN;
            int nextStartIdx = suggester.getStartIdx() + r.getMatch().length() + 1;
            int nextLength = 0;
            if (r.getNextSuggesterType() != null) {
                next = r.getNextSuggesterType();
                nextStartIdx = r.getNextSuggesterStartIdx();
                nextLength = r.getMatch().length();
            }
            changeCurrentSuggester(next, nextStartIdx, nextLength);
        } else if (state == SuggesterState.CONTINUE) {
            if(currentType != SuggestionType.MAIN) {
                p.isPartial = true;
                p.startIdx = suggester.getStartIdx();
                p.endIdx = suggester.getStartIdx() + suggester.getLength() - 1;
            }
        }

        return parsedParts;
    }

    public BaseTextSuggester getCurrentSuggester() {
        return textSuggesters.get(currentType);
    }

    private ParsedPart getParsedPart(SuggestionType type) {
        for (ParsedPart p : parsedParts) {
            if (p.type == type) {
                return p;
            }
        }
        ParsedPart p = new ParsedPart();
        p.type = currentType;
        if(type != SuggestionType.MAIN) {
            parsedParts.add(p);
        }
        return p;
    }

    public int[] onSuggestionItemClick(int selectionStart) {
        BaseTextSuggester s = getCurrentSuggester();
        int startIdx = s.getStartIdx();
        int endIdx = s.getEndIdx();
        if(startIdx == 0 && endIdx == 0) {
            startIdx = selectionStart;
            endIdx = selectionStart;
        }

        return new int[] {
                startIdx, endIdx
        };
    }

//    private List<AddQuestSuggestion> getRecurrentDayOfWeekSuggestions() {
//        int icon = R.drawable.ic_event_black_18dp;
//        List<AddQuestSuggestion> suggestions = new ArrayList<>();
//        suggestions.add(new AddQuestSuggestion(icon, "Monday", "Mon"));
//        suggestions.add(new AddQuestSuggestion(icon, "Tuesday", "Tue"));
//        suggestions.add(new AddQuestSuggestion(icon, "Wednesday", "Wed"));
//        suggestions.add(new AddQuestSuggestion(icon, "Thursday", "Thur"));
//        suggestions.add(new AddQuestSuggestion(icon, "Friday", "Fri"));
//        suggestions.add(new AddQuestSuggestion(icon, "Saturday", "Sat"));
//        suggestions.add(new AddQuestSuggestion(icon, "Sunday", "Sun"));
//        return suggestions;
//    }

//    private List<AddQuestSuggestion> getRecurrentDayOfMonthSuggestions() {
//        int icon = R.drawable.ic_event_black_18dp;
//        List<AddQuestSuggestion> suggestions = new ArrayList<>();
//        suggestions.add(new AddQuestSuggestion(icon, "21st of the month"));
//        suggestions.add(new AddQuestSuggestion(icon, "22nd of the month"));
//        return suggestions;
//    }

}
