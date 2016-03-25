package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.view.View;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.quest.AddQuestSuggestion;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/23/16.
 */
public class MainSuggestionsAdapter extends BaseSuggestionsAdapter {

    public MainSuggestionsAdapter(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    public MainSuggestionsAdapter(Context context, Bus eventBus, List<AddQuestSuggestion> suggestions) {
        super(context, eventBus, suggestions);
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
