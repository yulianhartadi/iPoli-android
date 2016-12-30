package io.ipoli.android.quest.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class AgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Quest> quests;

    public AgendaAdapter(List<Quest> quests) {
        this.quests = quests;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new AgendaViewHolder(inflater.inflate(R.layout.agenda_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AgendaViewHolder vh = (AgendaViewHolder) holder;
        Quest quest = quests.get(position);
        vh.name.setText(quest.getName());
        vh.categoryIndicatorImage.setImageResource(quest.getCategoryType().colorfulImage);
        vh.startEnd.setText(Time.of(quest.getStartMinute()) + "\n" + Time.of(quest.getStartMinute() + quest.getDuration()));
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    static class AgendaViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quest_start_end_time)
        TextView startEnd;

        @BindView(R.id.quest_text)
        TextView name;

        @BindView(R.id.quest_category_indicator_image)
        ImageView categoryIndicatorImage;

        AgendaViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
