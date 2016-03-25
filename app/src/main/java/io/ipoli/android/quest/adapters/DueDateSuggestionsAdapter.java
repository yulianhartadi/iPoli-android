package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.view.View;

import com.squareup.otto.Bus;

import io.ipoli.android.R;
import io.ipoli.android.quest.AddQuestSuggestion;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/24/16.
 */
public class DueDateSuggestionsAdapter extends BaseSuggestionsAdapter {

    private int icon = R.drawable.ic_event_black_18dp;

    public DueDateSuggestionsAdapter(Context context, Bus eventBus) {
        super(context, eventBus);
        this.suggestions.add(new AddQuestSuggestion(icon, "today"));
        this.suggestions.add(new AddQuestSuggestion(icon, "tomorrow"));
        this.suggestions.add(new AddQuestSuggestion(icon, "on 12 Feb"));
        this.suggestions.add(new AddQuestSuggestion(icon, "next Monday"));
        this.suggestions.add(new AddQuestSuggestion(icon, "after 3 days"));
        this.suggestions.add(new AddQuestSuggestion(icon, "in 2 months"));
    }

    @Override
    protected View.OnClickListener getClickListener(int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventBus.post(new SuggestionAdapterItemClickEvent(suggestions.get(position)));
            }
        };
    }
}
