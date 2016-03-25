package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.AddQuestSuggestion;
import io.ipoli.android.quest.QuestPartType;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/23/16.
 */
public abstract class BaseSuggestionsAdapter extends ArrayAdapter<AddQuestSuggestion> {
    protected Bus eventBus;

    protected List<AddQuestSuggestion> suggestions;
    protected QuestPartType type;

    public BaseSuggestionsAdapter(Context context, Bus eventBus, List<AddQuestSuggestion> suggestions) {
        super(context, R.layout.add_quest_suggestion_item, R.id.suggestion_text);
        this.suggestions = suggestions;
        this.eventBus = eventBus;
    }

    public BaseSuggestionsAdapter(Context context, Bus eventBus) {
        super(context, R.layout.add_quest_suggestion_item, R.id.suggestion_text);
        this.suggestions = new ArrayList<>();
        this.eventBus = eventBus;
    }

    public QuestPartType getType() {
        return type;
    }

    public void setType(QuestPartType type) {
        this.type = type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if( convertView == null ){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.add_quest_suggestion_item, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.suggestion_text);
            holder.icon = (ImageView) convertView.findViewById(R.id.suggestion_icon);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.text.setText(suggestions.get(position).visibleText);
        holder.icon.setImageResource(suggestions.get(position).icon);
        convertView.setOnClickListener(getClickListener(position));
        return convertView;
    }

    protected abstract View.OnClickListener getClickListener(int position);

    class ViewHolder {
        TextView text;
        ImageView icon;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public AddQuestSuggestion getItem(int position) {
        return suggestions.get(position);
    }

    public void setSuggestions(List<AddQuestSuggestion> suggestions) {
        this.suggestions = suggestions;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return textFilter;
    }

    protected Filter textFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue){
            return ((AddQuestSuggestion)resultValue).visibleText;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged();
            }
            else {
                notifyDataSetInvalidated();
            }
        }
    };
}
