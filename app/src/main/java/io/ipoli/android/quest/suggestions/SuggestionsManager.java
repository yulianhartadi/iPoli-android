package io.ipoli.android.quest.suggestions;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.ipoli.android.app.parsers.DateTimeParser;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.EndDateMatcher;
import io.ipoli.android.quest.parsers.FlexibleMatcher;
import io.ipoli.android.quest.parsers.MainMatcher;
import io.ipoli.android.quest.parsers.Match;
import io.ipoli.android.quest.parsers.QuestTextMatcher;
import io.ipoli.android.quest.parsers.RecurrenceDayOfMonthMatcher;
import io.ipoli.android.quest.parsers.RecurrenceDayOfWeekMatcher;
import io.ipoli.android.quest.parsers.RecurrenceEveryDayMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.parsers.TimesAMonthMatcher;
import io.ipoli.android.quest.parsers.TimesAWeekMatcher;
import io.ipoli.android.quest.suggestions.providers.MainSuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.SuggestionsProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/24/16.
 */
public class SuggestionsManager {

    private final Map<MatcherType, List<TextEntityType>> matcherTypeToTextEntityTypes;
    private Map<TextEntityType, QuestTextMatcher> typeToMatcher;

    private TextEntityType currentType;

    private Map<MatcherType, TextEntityType> usedMatcherTypesToUsedTextEntityType = new HashMap<>();

    private List<SuggestionDropDownItem> currentSuggestions = new ArrayList<>();

    private OnSuggestionsUpdatedListener suggestionsUpdatedListener;

    private Set<TextEntityType> excludedTypes = new HashSet<>();

    public static SuggestionsManager createForQuest(DateTimeParser parser, boolean use24HourFormat) {
        return new SuggestionsManager(parser, true, use24HourFormat);
    }

    public static SuggestionsManager createForRepeatingQuest(DateTimeParser parser, boolean use24HourFormat) {
        return new SuggestionsManager(parser, false, use24HourFormat);
    }

    private SuggestionsManager(DateTimeParser parser, boolean disableRepeating, boolean use24HourFormat) {
        Set<TextEntityType> disabledEntityTypes = new HashSet<>();
        if(disableRepeating) {
            disabledEntityTypes.add(TextEntityType.RECURRENT);
            disabledEntityTypes.add(TextEntityType.FLEXIBLE);
        } else {
            disabledEntityTypes.add(TextEntityType.DUE_DATE);
        }

        typeToMatcher = new HashMap<TextEntityType, QuestTextMatcher>() {{
            put(TextEntityType.MAIN, new MainMatcher(disabledEntityTypes));
            put(TextEntityType.DURATION, new DurationMatcher());
            put(TextEntityType.START_TIME, new StartTimeMatcher(parser, use24HourFormat));
            if(disableRepeating) {
                put(TextEntityType.DUE_DATE, new EndDateMatcher(parser));
                excludedTypes.add(TextEntityType.FLEXIBLE);
                excludedTypes.add(TextEntityType.TIMES_A_WEEK);
                excludedTypes.add(TextEntityType.TIMES_A_MONTH);
                excludedTypes.add(TextEntityType.RECURRENT);
                excludedTypes.add(TextEntityType.RECURRENT_DAY_OF_MONTH);
                excludedTypes.add(TextEntityType.RECURRENT_DAY_OF_WEEK);
            } else {
                put(TextEntityType.FLEXIBLE, new FlexibleMatcher());
                put(TextEntityType.TIMES_A_WEEK, new TimesAWeekMatcher());
                put(TextEntityType.TIMES_A_MONTH, new TimesAMonthMatcher());
                put(TextEntityType.RECURRENT, new RecurrenceEveryDayMatcher());
                put(TextEntityType.RECURRENT_DAY_OF_MONTH, new RecurrenceDayOfMonthMatcher());
                put(TextEntityType.RECURRENT_DAY_OF_WEEK, new RecurrenceDayOfWeekMatcher());
                excludedTypes.add(TextEntityType.DUE_DATE);
            }
        }};

        matcherTypeToTextEntityTypes = new HashMap<>();

        for (QuestTextMatcher matcher : typeToMatcher.values()) {
            MatcherType mt = matcher.getMatcherType();
            TextEntityType et = matcher.getTextEntityType();
            if (!matcherTypeToTextEntityTypes.containsKey(mt)) {
                matcherTypeToTextEntityTypes.put(mt, new ArrayList<>());
            }
            matcherTypeToTextEntityTypes.get(mt).add(et);
        }

        changeCurrentSuggestionsProvider(TextEntityType.MAIN, "");
    }

    public List<ParsedPart> onTextChange(String text, int selectionIndex) {
        List<ParsedPart> parsedParts = parse(text, selectionIndex);
        ParsedPart partialPart = findPartialPart(parsedParts);

        TextEntityType newType = partialPart != null ? partialPart.type : TextEntityType.MAIN;
        String parsedTypeText = partialPart != null ? StringUtils.substring(text, partialPart.startIdx, partialPart.endIdx) : "";
        changeCurrentSuggestionsProvider(newType, parsedTypeText);
        return parsedParts;
    }

    public List<ParsedPart> parse(String text) {
        return parse(text, text.length());
    }

    public List<ParsedPart> parse(String text, int selectionIndex) {
        List<ParsedPart> parts = new ArrayList<>();
        parsePreviouslyMatchedParts(text, parts);
        parseNewParts(text, selectionIndex, parts);
        sortPartsByStartIndex(parts);
        return parts;
    }

    private void parseNewParts(String text, int selectionIndex, List<ParsedPart> parts) {
        for (TextEntityType t : getUnusedTextEntityTypes()) {
            QuestTextMatcher matcher = typeToMatcher.get(t);
            Match partialMatch = matcher.partialMatch(text.substring(0, selectionIndex));
            if (canBeUsedAsPartialMatch(partialMatch, parts)) {
                int start = partialMatch.text.startsWith(" ") ? partialMatch.start + 1 : partialMatch.start;
                int end = partialMatch.end;
                ParsedPart currPartial = findPartialPart(parts);

                if (shouldAddNewPartialPart(start, end, currPartial)) {
                    parts.add(new ParsedPart(start, end, t, true));
                }

                if (isBetterMatch(start, end, currPartial)) {
                    parts.remove(currPartial);
                }

            } else {

                Match match = typeToMatcher.get(t).match(text);
                if (match == null) {
                    continue;
                }

                addUsedType(getMatcherType(t), t);
                int start = match.text.startsWith(" ") ? match.start + 1 : match.start;
                int end = match.text.endsWith(" ") ? match.end - 1 : match.end;
                parts.add(new ParsedPart(start, end, t, false));
            }
        }
    }

    private void parsePreviouslyMatchedParts(String text, List<ParsedPart> parts) {
        List<MatcherType> matcherTypesToRemove = new ArrayList<>();
        for (MatcherType matcherType : usedMatcherTypesToUsedTextEntityType.keySet()) {
            TextEntityType textEntityType = usedMatcherTypesToUsedTextEntityType.get(matcherType);
            QuestTextMatcher matcher = typeToMatcher.get(textEntityType);
            Match match = matcher.match(text);
            if (match == null) {
                matcherTypesToRemove.add(matcherType);
            } else {
                int start = match.text.startsWith(" ") ? match.start + 1 : match.start;
                int end = match.text.endsWith(" ") ? match.end - 1 : match.end;
                parts.add(new ParsedPart(start, end, textEntityType, false));
            }
        }

        for (MatcherType t : matcherTypesToRemove) {
            usedMatcherTypesToUsedTextEntityType.remove(t);
        }
    }

    @NonNull
    private Set<TextEntityType> getUnusedTextEntityTypes() {
        Set<TextEntityType> unusedTextEntityTypes = new TreeSet<>(Arrays.asList(TextEntityType.values()));

        unusedTextEntityTypes.remove(TextEntityType.MAIN);

        for (MatcherType mt : usedMatcherTypesToUsedTextEntityType.keySet()) {
            unusedTextEntityTypes.removeAll(matcherTypeToTextEntityTypes.get(mt));
        }

        for(TextEntityType type : excludedTypes) {
            unusedTextEntityTypes.remove(type);
        }
        return unusedTextEntityTypes;
    }

    private MatcherType getMatcherType(TextEntityType textEntityType) {
        return typeToMatcher.get(textEntityType).getMatcherType();
    }

    private void sortPartsByStartIndex(List<ParsedPart> parts) {
        Collections.sort(parts, new Comparator<ParsedPart>() {

            @Override
            public int compare(ParsedPart lhs, ParsedPart rhs) {
                return lhs.startIdx < rhs.startIdx ? -1 : 1;
            }
        });
    }


    private boolean shouldAddNewPartialPart(int start, int end, ParsedPart currPartial) {
        return hasNoPartialPart(currPartial) || isBetterMatch(start, end, currPartial);
    }

    private boolean isBetterMatch(int start, int end, ParsedPart currPartial) {
        if (hasNoPartialPart(currPartial)) {
            return true;
        }
        return currPartial.endIdx - currPartial.startIdx < end - start;
    }

    private boolean hasNoPartialPart(ParsedPart currPartial) {
        return currPartial == null;
    }

    private boolean canBeUsedAsPartialMatch(Match partialMatch, List<ParsedPart> parts) {
        return partialMatch != null && doesNotIntersectWithParsedNotPartialPart(parts, partialMatch);
    }

    private boolean doesNotIntersectWithParsedNotPartialPart(List<ParsedPart> parts, Match partialMatch) {
        for (ParsedPart p : parts) {
            if (p.isPartial) {
                continue;
            }
            if ((p.startIdx >= partialMatch.start && p.startIdx <= partialMatch.end) || (p.endIdx >= partialMatch.start && p.endIdx <= partialMatch.end)) {
                return false;
            }
        }
        return true;
    }

    public TextTransformResult deleteText(String preDeleteText, int deleteStartIndex) {
        ParsedPart partToDelete = findNotPartialParsedPartContainingIdx(deleteStartIndex, parse(preDeleteText, deleteStartIndex));
        if (partToDelete == null) {
            return new TextTransformResult(StringUtils.cut(preDeleteText, deleteStartIndex, deleteStartIndex), deleteStartIndex);
        }
        removeUsedType(getMatcherType(partToDelete.type));

        return new TextTransformResult(StringUtils.cut(preDeleteText, partToDelete.startIdx, partToDelete.endIdx), partToDelete.startIdx);
    }

    public TextTransformResult onSuggestionItemClick(String text, SuggestionDropDownItem suggestion, int selectionIndex) {
        TextTransformResult result;
        if (suggestion.shouldReplace) {
            result = replace(text, suggestion.text, selectionIndex);

            if (suggestion.shouldFinishMatch) {
                addUsedType(getMatcherType(currentType), currentType);
            }

        } else {
            result = append(text, suggestion.text, selectionIndex);
        }

        return result;
    }

    public int getSelectionIndex(String text, int selectedIndex) {
        ParsedPart p = findNotPartialParsedPartContainingIdx(selectedIndex, parse(text));
        if (p == null) {
            return selectedIndex;
        }
        if (selectedIndex - p.startIdx < (p.endIdx + 1) - selectedIndex) {
            return p.startIdx;
        } else {
            return Math.min(p.endIdx + 1, text.length() - 1);
        }
    }

    public TextTransformResult replace(String text, String replaceText, int selectionIndex) {
        ParsedPart partialPart = findPartialPart(parse(text, selectionIndex));
        String start, end;
        if (partialPart == null) {
            start = text.substring(0, selectionIndex);
            end = text.substring(selectionIndex);
        } else {
            start = text.substring(0, partialPart.startIdx);
            end = partialPart.endIdx + 1 < text.length() ? text.substring(partialPart.endIdx + 1) : "";
        }

        replaceText += replaceText.isEmpty() ? "" : " ";
        start = start.isEmpty() || start.endsWith(" ") ? start : start + " ";
        end = end.isEmpty() || end.startsWith(" ") ? end : " " + end;
        return new TextTransformResult(start + replaceText + end, (start + replaceText).length());
    }

    public TextTransformResult append(String text, String appendText, int selectionIndex) {
        String start = text.substring(0, selectionIndex);
        String end = text.substring(selectionIndex);
        appendText += appendText.isEmpty() ? "" : " ";
        start = start.isEmpty() || start.endsWith(" ") ? start : start + " ";
        end = end.isEmpty() || end.startsWith(" ") ? end : " " + end;
        return new TextTransformResult(start + appendText + end, (start + appendText).length());
    }

    public void changeCurrentSuggestionsProvider(TextEntityType type, String parsedText) {
        currentType = type;
        currentSuggestions = getCurrentSuggestionsProvider().filter(parsedText);
        if (suggestionsUpdatedListener != null) {
            suggestionsUpdatedListener.onSuggestionsUpdated();
        }
    }

    public void setSuggestionsUpdatedListener(OnSuggestionsUpdatedListener suggestionsUpdatedListener) {
        this.suggestionsUpdatedListener = suggestionsUpdatedListener;
    }

    public List<SuggestionDropDownItem> getSuggestions() {
        return currentSuggestions;
    }

    public SuggestionsProvider getCurrentSuggestionsProvider() {
        return typeToMatcher.get(currentType).getSuggestionsProvider();
    }

    public TextEntityType getCurrentSuggestionsProviderType() {
        return currentType;
    }

    public ParsedPart findPartialPart(List<ParsedPart> parsedParts) {
        for (ParsedPart p : parsedParts) {
            if (p.isPartial) {
                return p;
            }
        }
        return null;
    }

    private ParsedPart findNotPartialParsedPartContainingIdx(int index, List<ParsedPart> parsedParts) {
        for (ParsedPart p : parsedParts) {
            if (p.startIdx <= index && index <= p.endIdx && !p.isPartial) {
                return p;
            }
        }
        return null;
    }

    private void addUsedType(MatcherType type, TextEntityType textEntityType) {
        if (type == MatcherType.MAIN) {
            return;
        }
        usedMatcherTypesToUsedTextEntityType.put(type, textEntityType);
        ((MainSuggestionsProvider) typeToMatcher.get(TextEntityType.MAIN).getSuggestionsProvider()).addUsedMatcherType(type);
    }

    private void removeUsedType(MatcherType type) {
        usedMatcherTypesToUsedTextEntityType.remove(type);
        ((MainSuggestionsProvider) typeToMatcher.get(TextEntityType.MAIN).getSuggestionsProvider()).removeUsedMatcherType(type);
    }

    public class TextTransformResult {
        public String text;
        public int selectionIndex;

        public TextTransformResult(String text, int selectionIndex) {
            this.text = text;
            this.selectionIndex = selectionIndex;
        }
    }

}
