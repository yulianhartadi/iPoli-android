package io.ipoli.android.quest.suggestions.providers;

import android.text.TextUtils;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class DueDateSuggestionsProvider implements SuggestionsProvider {
    private static final int icon = R.drawable.ic_event_black_18dp;

    private final List<String> daysOfMonth = new ArrayList<>();
    private final List<String> months = new ArrayList<>();

    private List<SuggestionDropDownItem> defaultSuggestionItems = new ArrayList<>();

    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Avg", "Sep", "Oct", "Nov", "Dec"};

    public DueDateSuggestionsProvider() {
        SimpleDateFormat dayMonthFormatter = new SimpleDateFormat("dd MMM", Locale.getDefault());
        String after3days = dayMonthFormatter.format(LocalDate.now().plusDays(3).toDate());
        defaultSuggestionItems.add(new SuggestionDropDownItem(icon, "today"));
        defaultSuggestionItems.add(new SuggestionDropDownItem(icon, "tomorrow"));
        defaultSuggestionItems.add(new SuggestionDropDownItem(icon, "after 2 days"));
        defaultSuggestionItems.add(new SuggestionDropDownItem(icon, after3days, "on " + after3days));
        defaultSuggestionItems.add(new SuggestionDropDownItem(icon, "this Friday"));
        defaultSuggestionItems.add(new SuggestionDropDownItem(icon, "next Monday"));
        defaultSuggestionItems.add(new SuggestionDropDownItem(icon, "in 2 months"));

        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        for (int i = today; i <= 31; i++) {
            daysOfMonth.add(String.valueOf(i));
        }

        for (int i = 1; i < today; i++) {
            daysOfMonth.add(String.valueOf(i));
        }

        int thisMonth = Calendar.getInstance().get(Calendar.MONTH);

        months.addAll(Arrays.asList(MONTHS).subList(thisMonth, MONTHS.length));
        months.addAll(Arrays.asList(MONTHS).subList(0, thisMonth));
    }

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        if ("on ".contains(text.toLowerCase())) {
            return defaultSuggestionItems;
        }

        if (text.toLowerCase().startsWith("on ")) {
            text = text.replaceFirst("on\\s", "");
        }

        List<SuggestionDropDownItem> items = new ArrayList<>();

        if ("today".startsWith(text) || "tomorrow".startsWith(text)) {
            for (String t : new String[]{"today", "tomorrow"}) {
                if (t.startsWith(text.toLowerCase())) {
                    items.add(new SuggestionDropDownItem(icon, t));
                }
            }
            return items;
        }

        if (Character.isDigit(text.charAt(0))) {

            String[] parts = text.split("\\s");
            if (parts.length == 1) {
                for (String month : months) {
                    for (String day : daysOfMonth) {
                        if ((day + " ").startsWith(text.toLowerCase())) {
                            String visibleText = day + " " + month;
                            items.add(new SuggestionDropDownItem(icon, visibleText, "on " + visibleText));
                        }
                    }
                }
                return items;
            } else {
                String monthPart = parts[parts.length - 1];
                String startPart = TextUtils.join(" ", Arrays.asList(parts).subList(0, parts.length - 1));
                for(String m : months) {
                    if(m.toLowerCase().startsWith(monthPart.toLowerCase())) {
                        String visibleText = startPart + " " + m;
                        items.add(new SuggestionDropDownItem(icon, visibleText, "on " + visibleText));
                    }
                }
            }
        }

        return items;
    }

}
