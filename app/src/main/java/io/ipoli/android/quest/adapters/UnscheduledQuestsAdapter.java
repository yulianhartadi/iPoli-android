package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class UnscheduledQuestsAdapter extends RecyclerView.Adapter<UnscheduledQuestsAdapter.ViewHolder> {

    private Context context;
    private List<Quest> quests;
    private final Bus eventBus;

    public UnscheduledQuestsAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
        this.quests = quests;
        this.eventBus = eventBus;
    }

    @Override
    public UnscheduledQuestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.unscheduled_quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new ShowQuestEvent(q));
            }
        });

        GradientDrawable drawable = (GradientDrawable) holder.indicator.getBackground();
        drawable.setColor(ContextCompat.getColor(context, Quest.getContext(q).resLightColor));

        if (Quest.isStarted(q)) {
            Animation blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
            holder.indicator.startAnimation(blinkAnimation);
        }

        holder.name.setText(q.getName() + " (x2)");
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                eventBus.post(new MoveQuestToCalendarRequestEvent(q));
                return true;
            }
        });

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(false);
        holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    eventBus.post(new CompleteUnscheduledQuestRequestEvent(q));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public void addQuest(int position, Quest quest) {
        quests.add(position, quest);
        notifyItemInserted(position);
    }

    public void removeQuest(Quest quest) {
        int position = quests.indexOf(quest);
        quests.remove(quest);
        notifyItemRemoved(position);
    }

    public int indexOf(Quest quest) {
        return quests.indexOf(quest);
    }

    public void updateQuests(List<Quest> quests) {
        this.quests = quests;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.quest_check)
        CheckBox check;

        @Bind(R.id.quest_text)
        TextView name;

        @Bind(R.id.quest_context_indicator)
        View indicator;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}