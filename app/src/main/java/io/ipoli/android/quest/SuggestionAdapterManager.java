package io.ipoli.android.quest;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.adapters.BaseSuggestionsAdapter;
import io.ipoli.android.quest.adapters.MainSuggestionsAdapter;
import io.ipoli.android.quest.events.SuggestionsAdapterChangedEvent;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/24/16.
 */
public class SuggestionAdapterManager {
    @Inject
    Bus eventBus;

    Context context;

    Map<QuestPartType, Boolean> typeToIsUsed = new HashMap<>();
    BaseSuggestionsAdapter currentAdapter;

    public SuggestionAdapterManager(Context context) {
        this.context = context;
        App.getAppComponent(context).inject(this);
        currentAdapter = new MainSuggestionsAdapter(context, eventBus, getMainSuggestions());
        currentAdapter.setType(QuestPartType.MAIN);
    }

    public BaseSuggestionsAdapter getAdapter() {
        return currentAdapter;
    }

    public void changeAdapterSuggestions(String text) {
        QuestPartType sa = QuestPartType.get(text);
        if(sa == null) {
            currentAdapter.setSuggestions(getMainSuggestions());
        } else if (!typeToIsUsed.containsKey(sa) || !typeToIsUsed.get(sa)) {
            switch (sa) {
                case DUE_DATE:
                    currentAdapter.setSuggestions(getDueDateSuggestions());
                    currentAdapter.setType(QuestPartType.DUE_DATE);
                    break;
                case DURATION:
                    currentAdapter.setSuggestions(getDurationSuggestions());
                    currentAdapter.setType(QuestPartType.DURATION);
                    break;
                default:
                    currentAdapter.setSuggestions(getMainSuggestions());
                    currentAdapter.setType(QuestPartType.MAIN);
            }
        }
        eventBus.post(new SuggestionsAdapterChangedEvent());
    }

    public void markAdapterAsUsed(QuestPartType adapter) {
        typeToIsUsed.put(adapter, true);
        changeAdapterSuggestions("");
    }

    public void markAdapterAsNotUsed(QuestPartType adapter) {
        typeToIsUsed.put(adapter, false);
        changeAdapterSuggestions("");
    }

    private List<AddQuestSuggestion> getMainSuggestions() {
        List<AddQuestSuggestion> suggestions = new ArrayList<>();
        suggestions.add(new AddQuestSuggestion(R.drawable.ic_event_black_18dp, "on ...", "on"));
        suggestions.add(new AddQuestSuggestion(R.drawable.ic_alarm_black_18dp, "at ...", "at"));
        suggestions.add(new AddQuestSuggestion(R.drawable.ic_timer_black_18dp, "for ...", "for"));
        suggestions.add(new AddQuestSuggestion(R.drawable.ic_repeat_black_24dp, "every ...", "every"));
        suggestions.add(new AddQuestSuggestion(R.drawable.ic_multiply_black_24dp, "times per day ...", "times per day"));

        List<AddQuestSuggestion> aqs = new ArrayList<>();
        for(AddQuestSuggestion s : suggestions) {
            QuestPartType t = QuestPartType.get(s.text);
            if(t == null || !typeToIsUsed.containsKey(t) || !typeToIsUsed.get(t)) {
                aqs.add(s);
            }
        }

        return aqs;
    }

    private List<AddQuestSuggestion> getDueDateSuggestions() {
        int icon = R.drawable.ic_event_black_18dp;
        List<AddQuestSuggestion> suggestions = new ArrayList<>();
        suggestions.add(new AddQuestSuggestion(icon, "today"));
        suggestions.add(new AddQuestSuggestion(icon, "tomorrow"));
        suggestions.add(new AddQuestSuggestion(icon, "12 Feb", "on 12 Feb"));
        suggestions.add(new AddQuestSuggestion(icon, "next Monday"));
        suggestions.add(new AddQuestSuggestion(icon, "after 3 days"));
        suggestions.add(new AddQuestSuggestion(icon, "in 2 months"));
        return suggestions;
    }

    private List<AddQuestSuggestion> getDurationSuggestions() {
        int icon = R.drawable.ic_timer_black_18dp;
        List<AddQuestSuggestion> suggestions = new ArrayList<>();
        suggestions.add(new AddQuestSuggestion(icon, "15 min", "for 15 min"));
        suggestions.add(new AddQuestSuggestion(icon, "30 min", "for 30 min"));
        suggestions.add(new AddQuestSuggestion(icon, "1 hour", "for 1 hour"));
        suggestions.add(new AddQuestSuggestion(icon, "1h and 30m", "for 1h and 30m"));
        return suggestions;
    }
}
