package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.view.View;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/16.
 */
public class SuggestionsAdapter extends BaseSuggestionsAdapter {

    public SuggestionsAdapter(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    public SuggestionsAdapter(Context context, Bus eventBus, List<SuggestionDropDownItem> suggestions) {
        super(context, eventBus, suggestions);
    }

    @Override
    protected View.OnClickListener getClickListener(int position) {
        return v -> eventBus.post(new SuggestionAdapterItemClickEvent(suggestions.get(position)));
    }

}
