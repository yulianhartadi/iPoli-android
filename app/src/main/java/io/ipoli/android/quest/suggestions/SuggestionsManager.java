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
        if (type == currentType) {
            return;
        }
        Log.d("ChangeCurrentSuggester", "From: " + currentType.name()
                + " To: " + type.name() + " startIdx: " + startIdx + " length: " + length);
        currentType = type;
        getCurrentSuggester().setStartIdx(startIdx);
        getCurrentSuggester().setLength(length);
        suggestionsUpdatedListener.onSuggestionsUpdated();
    }

    public List<ParsedPart> onTextChange(String text, int start, int before, int count, boolean finishCurrentSuggester) {

        ParsedPart p = getParsedPart(currentType);
        BaseTextSuggester suggester = getCurrentSuggester();
        SuggesterResult r = suggester.parse(text);
        SuggesterState state = r.getState();

        if (state == SuggesterState.FINISH || finishCurrentSuggester) {
            if (currentType != SuggestionType.MAIN) {
                usedTypes.add(currentType);
                p.isPartial = false;
                p.startIdx = suggester.getStartIdx();
                p.endIdx = finishCurrentSuggester ? suggester.getStartIdx() + suggester.getLastParsedText().length() - 1 : suggester.getEndIdx();
            }

            SuggestionType next = SuggestionType.MAIN;
            int nextStartIdx = suggester.getEndIdx() + 1;
            int nextLength = 0;
            if (r.getNextSuggesterType() != null) {
                next = r.getNextSuggesterType();
                nextStartIdx = r.getNextSuggesterStartIdx();
                nextLength = r.getMatch().length();
            }
            changeCurrentSuggester(next, nextStartIdx, nextLength);
        } else if (state == SuggesterState.CANCEL) {
            usedTypes.remove(currentType);
            parsedParts.remove(p);
            changeCurrentSuggester(SuggestionType.MAIN, text.length(), 0);
        } else if (state == SuggesterState.CONTINUE) {
            if (currentType != SuggestionType.MAIN) {
                p.isPartial = true;
                p.startIdx = suggester.getStartIdx();
                p.endIdx = suggester.getEndIdx();
            }
        }

        return parsedParts;
    }

    public void onTextInserted(int start, int count) {
        updateIndexesAfterInsert(start, count);
        ParsedPart updatedPart = findNotPartialParsedPartContainingOrNextToIdx(start);
        if (updatedPart != null) {
            changeCurrentSuggester(updatedPart.type, Math.min(updatedPart.startIdx, start), updatedPart.endIdx - updatedPart.startIdx + 1 + count);
        }
    }

    public int[] onSuggestionItemClick(int selectionStart) {
        BaseTextSuggester s = getCurrentSuggester();
        int startIdx = s.getStartIdx();
        int endIdx = s.getEndIdx();
        if (startIdx == 0 && endIdx == 0) {
            startIdx = selectionStart;
            endIdx = selectionStart;
        }

        return new int[]{
                startIdx, endIdx
        };
    }

    public TextViewProps onTextDeleted(String text, int startIdx, int deletedLength) {
        int selectionIndex = startIdx;
        ParsedPart partToDelete = findNotPartialParsedPartContainingIdx(startIdx);
        if (partToDelete != null) {
            text = deleteText(text, partToDelete.startIdx, partToDelete.endIdx);
            deletedLength = partToDelete.endIdx - partToDelete.startIdx + 1;
            selectionIndex = partToDelete.startIdx;
            usedTypes.remove(partToDelete.type);
            parsedParts.remove(partToDelete);
        } else {
            text = text.substring(0, startIdx) + (startIdx + deletedLength >= text.length() ? "" : text.substring(startIdx + deletedLength));
        }
        updateIndexesAfterDelete(startIdx, deletedLength);
        return new TextViewProps(text, selectionIndex);
    }

    public List<ParsedPart> onCursorSelectionChanged(String text, int startIdx) {
        ParsedPart p = getParsedPart(currentType);
        BaseTextSuggester suggester = getCurrentSuggester();
        SuggesterResult r = suggester.parse(text);

        String match = r.getMatch();
        if (!match.isEmpty() && currentType != SuggestionType.MAIN) {
            usedTypes.add(currentType);
            p.isPartial = false;
            p.startIdx = suggester.getStartIdx();
            p.endIdx = suggester.getEndIdx();
        } else {
            removeParsedPart(currentType);
        }

        changeCurrentSuggester(SuggestionType.MAIN, startIdx, 0);
        return parsedParts;
    }

    private void updateIndexesAfterDelete(int startIdx, int lenToShiftLeft) {
        for (SuggestionType t : textSuggesters.keySet()) {
            BaseTextSuggester s = textSuggesters.get(t);
            if (s.getStartIdx() > startIdx) {
                s.setStartIdx(s.getStartIdx() - lenToShiftLeft);
            }
        }
        BaseTextSuggester currentSuggester = getCurrentSuggester();
        if (currentSuggester.getStartIdx() > startIdx) {
            currentSuggester.setStartIdx(currentSuggester.getStartIdx() - lenToShiftLeft);
        } else if (currentSuggester.getLength() <= lenToShiftLeft) {
            removeParsedPart(currentType);
            changeCurrentSuggester(SuggestionType.MAIN, startIdx, 0);
        }

        for (ParsedPart p : parsedParts) {
            if (p.startIdx > startIdx) {
                p.startIdx = Math.max(0, p.startIdx - lenToShiftLeft);
                p.endIdx = Math.max(0, p.endIdx - lenToShiftLeft);
            }
        }
    }

    private void updateIndexesAfterInsert(int startIdx, int lenToShiftRight) {
        for (SuggestionType t : textSuggesters.keySet()) {
            BaseTextSuggester s = textSuggesters.get(t);
            if (s.getStartIdx() > startIdx) {
                s.setStartIdx(s.getStartIdx() + lenToShiftRight);
            }
        }
        BaseTextSuggester currentSuggester = getCurrentSuggester();
        if (currentSuggester.getStartIdx() > startIdx) {
            currentSuggester.setStartIdx(currentSuggester.getStartIdx() + lenToShiftRight);
        }

        for (ParsedPart p : parsedParts) {
            if (p.startIdx > startIdx) {
                p.startIdx = p.startIdx + lenToShiftRight;
                p.endIdx = p.endIdx + lenToShiftRight;
            }
        }
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
        if (type != SuggestionType.MAIN) {
            parsedParts.add(p);
        }
        return p;
    }

    private String deleteText(String text, int startIdx, int endIdx) {
        return text.substring(0, startIdx) + (endIdx + 1 < text.length() ? text.substring(endIdx + 1) : "");
    }

    private ParsedPart findNotPartialParsedPartContainingIdx(int index) {
        for (ParsedPart p : parsedParts) {
            if (p.startIdx <= index && index <= p.endIdx && !p.isPartial) {
                return p;
            }
        }
        return null;
    }

    private ParsedPart findNotPartialParsedPartContainingOrNextToIdx(int index) {
        for (ParsedPart p : parsedParts) {
            if (p.startIdx - 1 <= index && index <= p.endIdx + 1 && !p.isPartial) {
                return p;
            }
        }
        return null;
    }

    private void removeParsedPart(SuggestionType type) {
        ParsedPart remove = null;
        for (ParsedPart p : parsedParts) {
            if (p.type == type) {
                remove = p;
                break;
            }
        }
        if (remove != null) {
            parsedParts.remove(remove);
        }
    }

    public class TextViewProps {
        public String text;
        public int selectionStartIdx;

        public TextViewProps(String text, int selectionStartIdx) {
            this.text = text;
            this.selectionStartIdx = selectionStartIdx;
        }
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
