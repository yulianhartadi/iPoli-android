package io.ipoli.android.quest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.StartTimeFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {

    private final Context context;

    private List<Quest> quests;

    public QuestAdapter(Context context, List<Quest> quests) {
        this.context = context;
        this.quests = quests;
    }

    @Override
    public QuestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(holder.getAdapterPosition());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, QuestActivity.class);
                i.putExtra(Constants.QUEST_ID_EXTRA_KEY, q.getId());
                context.startActivity(i);
            }
        });

        holder.name.setText(q.getName());

        if (q.getStartTime() == null) {
            holder.startTime.setVisibility(View.INVISIBLE);
        } else {
            holder.startTime.setVisibility(View.VISIBLE);
            holder.startTime.setText(StartTimeFormatter.format(q.getStartTime()));
        }

        if (q.getDuration() <= 0) {
            holder.duration.setVisibility(View.INVISIBLE);
        } else {
            holder.duration.setVisibility(View.VISIBLE);
            holder.duration.setText(DurationFormatter.format(context, q.getDuration()));
        }
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public void putQuest(int position, Quest quest) {
        quests.add(position, quest);
        notifyItemInserted(position);
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public void addQuest(Quest quest) {
        quests.add(quest);
        notifyItemInserted(quests.size() - 1);
    }

    public void removeQuest(int position) {
        quests.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.quest_name)
        public TextView name;

        @Bind(R.id.quest_start_time)
        public TextView startTime;

        @Bind(R.id.quest_duration)
        public TextView duration;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}