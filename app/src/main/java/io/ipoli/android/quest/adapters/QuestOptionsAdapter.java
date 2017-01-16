package io.ipoli.android.quest.adapters;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */
public class QuestOptionsAdapter extends RecyclerView.Adapter {

    private final List<Pair<String, View.OnClickListener>> options;

    public QuestOptionsAdapter(List<Pair<String, View.OnClickListener>> options) {
        this.options = options;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new DateOptionsViewHolder(inflater.inflate(R.layout.wizard_option_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        Pair<String, View.OnClickListener> pair = options.get(holder.getAdapterPosition());
        textView.setText(pair.first);
        textView.setOnClickListener(pair.second);
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    private static class DateOptionsViewHolder extends RecyclerView.ViewHolder {

        DateOptionsViewHolder(View itemView) {
            super(itemView);
        }
    }
}
