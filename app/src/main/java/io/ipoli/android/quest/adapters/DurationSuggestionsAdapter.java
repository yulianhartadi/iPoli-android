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
public class DurationSuggestionsAdapter extends BaseSuggestionsAdapter {

    private int icon = R.drawable.ic_timer_black_18dp;

    public DurationSuggestionsAdapter(Context context, Bus eventBus) {
        super(context, eventBus);
        this.suggestions.add(new AddQuestSuggestion(icon, "15 minutes"));
        this.suggestions.add(new AddQuestSuggestion(icon, "30 minutes"));
        this.suggestions.add(new AddQuestSuggestion(icon, "1 hour"));
        this.suggestions.add(new AddQuestSuggestion(icon, "1h and 30m"));
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
