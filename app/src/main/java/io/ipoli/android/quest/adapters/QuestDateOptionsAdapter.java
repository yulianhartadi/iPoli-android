package io.ipoli.android.quest.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */
public class QuestDateOptionsAdapter extends RecyclerView.Adapter {

    private final List<String> items = new ArrayList<>();

    public QuestDateOptionsAdapter() {
        items.add("By the end of the week");
        items.add("By the end of the month");
        items.add("Someday by...");
        items.add("Today");
        items.add("Tomorrow");
        items.add("On...");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new DateOptionsViewHolder(inflater.inflate(R.layout.wizard_option_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setText(items.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class DateOptionsViewHolder extends RecyclerView.ViewHolder {

        DateOptionsViewHolder(View itemView) {
            super(itemView);
        }
    }
}
