package io.ipoli.android.quest;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.StartTimeFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private final Context context;

    private List<Quest> quests;
    private Bus eventBus;

    public QuestAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
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

        GradientDrawable drawable = (GradientDrawable) holder.indicator.getBackground();
        drawable.setColor(ContextCompat.getColor(context, Quest.getContext(q).resLightColor));

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

    public List<Quest> getQuests() {
        return quests;
    }

    public void removeQuest(int position) {
        quests.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismissed(int position, int direction) {
        Quest q = quests.get(position);
        quests.remove(position);
        notifyItemRemoved(position);
        eventBus.post(new CompleteQuestRequestEvent(q));
    }

    public void updateQuests(List<Quest> newQuests) {
        quests.clear();
        quests.addAll(newQuests);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        @Bind(R.id.quest_name)
        public TextView name;

        @Bind(R.id.quest_start_time)
        public TextView startTime;

        @Bind(R.id.quest_duration)
        public TextView duration;

        @Bind(R.id.quest_indicator)
        public View indicator;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
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
            itemView.findViewById(R.id.quest_complete_check).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.quest_start_time).setVisibility(View.GONE);
        }

        @Override
        public void onItemSwipeStopped() {
            itemView.findViewById(R.id.quest_complete_check).setVisibility(View.GONE);
            itemView.findViewById(R.id.quest_start_time).setVisibility(View.VISIBLE);
        }
    }
}