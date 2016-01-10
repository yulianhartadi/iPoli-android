package io.ipoli.android.quest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.Collections;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.quest.events.QuestCompleteRequestEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private List<Quest> quests;
    private final Bus eventBus;

    public QuestAdapter(List<Quest> quests, Bus eventBus) {
        this.quests = quests;
        this.eventBus = eventBus;
    }

    @Override
    public QuestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(position);
        TextView tv = (TextView) holder.itemView.findViewById(R.id.quest_name);
        tv.setText(q.getName());

        final ImageButton startBtn = (ImageButton) holder.itemView.findViewById(R.id.quest_start);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Quest.Status status = Quest.Status.valueOf(q.getStatus());
                if (status == Quest.Status.PLANNED) {
                    startBtn.setImageResource(R.drawable.ic_pause_circle_outline_accent_24dp);
                    q.setStatus(Quest.Status.STARTED.name());
                } else if (status == Quest.Status.STARTED) {
                    startBtn.setImageResource(R.drawable.ic_play_circle_outline_accent_24dp);
                    q.setStatus(Quest.Status.PLANNED.name());
                }
                eventBus.post(new QuestUpdatedEvent(q));
            }
        });
        holder.itemView.findViewById(R.id.quest_done_tick).setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public void addQuest(int position, Quest quest) {
        quests.add(position, quest);
        notifyDataSetChanged();
    }

    public List<Quest> getQuests() {
        return quests;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(quests, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(quests, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismissed(int position, int direction) {
        Quest q = quests.get(position);
        quests.remove(position);
        notifyItemRemoved(position);
        eventBus.post(new QuestCompleteRequestEvent(q, position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        public ViewHolder(View v) {
            super(v);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundResource(R.color.md_blue_100);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onItemSwipeStart() {
            itemView.findViewById(R.id.quest_done_tick).setVisibility(View.VISIBLE);
        }

        @Override
        public void onItemSwipeStopped() {
            itemView.findViewById(R.id.quest_done_tick).setVisibility(View.GONE);
        }
    }
}