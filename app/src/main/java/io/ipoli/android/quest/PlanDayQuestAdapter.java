package io.ipoli.android.quest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.quest.events.QuestDeleteRequestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class PlanDayQuestAdapter extends RecyclerView.Adapter<PlanDayQuestAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private final PrettyTime prettyTime;
    private List<Quest> quests;
    private final Bus eventBus;

    public PlanDayQuestAdapter(List<Quest> quests, Bus eventBus) {
        this.quests = quests;
        this.eventBus = eventBus;
        prettyTime = new PrettyTime();
    }

    @Override
    public PlanDayQuestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox cb = (CheckBox) holder.itemView.findViewById(R.id.quest_check);
                cb.setChecked(!cb.isChecked());
            }
        });

        CheckBox cb = (CheckBox) holder.itemView.findViewById(R.id.quest_check);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    q.setStatus(Quest.Status.PLANNED.name());
                } else {
                    q.setStatus(Quest.Status.UNPLANNED.name());
                }
            }
        });

        if (Quest.Status.valueOf(q.getStatus()) != Quest.Status.UNPLANNED) {
            cb.setChecked(true);
        }
        TextView tv = (TextView) holder.itemView.findViewById(R.id.quest_name);
        tv.setText(q.getName());

        TextView createdTV = (TextView) holder.itemView.findViewById(R.id.quest_created_at);
        createdTV.setText(prettyTime.format(q.getCreatedAt()));

        holder.itemView.findViewById(R.id.quest_delete).setVisibility(View.GONE);
        createdTV.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public List<Quest> getQuests() {
        return quests;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismissed(int position, int direction) {
        Quest q = quests.get(position);
        quests.remove(position);
        notifyItemRemoved(position);
        eventBus.post(new QuestDeleteRequestEvent(q, position));
    }

    public void addQuest(int position, Quest quest) {
        quests.add(position, quest);
        notifyDataSetChanged();
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
            itemView.findViewById(R.id.quest_delete).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.quest_created_at).setVisibility(View.GONE);
        }

        @Override
        public void onItemSwipeStopped() {
            itemView.findViewById(R.id.quest_delete).setVisibility(View.GONE);
            itemView.findViewById(R.id.quest_created_at).setVisibility(View.VISIBLE);
        }
    }
}