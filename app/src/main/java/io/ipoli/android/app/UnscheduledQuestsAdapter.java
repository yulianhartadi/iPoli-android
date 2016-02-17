package io.ipoli.android.app;

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
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.Status;
import io.ipoli.android.quest.events.EditQuestRequestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class UnscheduledQuestsAdapter extends RecyclerView.Adapter<UnscheduledQuestsAdapter.ViewHolder> {

    private Context context;
    private List<Quest> quests;
    private final Bus eventBus;
    private OnQuestLongClickListener questLongClickListener;

    public UnscheduledQuestsAdapter(Context context, List<Quest> quests, Bus eventBus, OnQuestLongClickListener questLongClickListener) {
        this.context = context;
        this.quests = quests;
        this.eventBus = eventBus;
        this.questLongClickListener = questLongClickListener;
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
                eventBus.post(new EditQuestRequestEvent(q.getId(), q.getName(), holder.getAdapterPosition(), q.getDue()));
            }
        });

        GradientDrawable drawable = (GradientDrawable) holder.indicator.getBackground();
        drawable.setColor(ContextCompat.getColor(context, Quest.getContext(q).resLightColor));

        if (Quest.getStatus(q) == Status.STARTED) {
            Animation blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
            holder.indicator.startAnimation(blinkAnimation);
        }

        holder.name.setText(q.getName());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                questLongClickListener.onLongClick(holder.getAdapterPosition(), q);
                quests.remove(q);
                notifyItemRemoved(holder.getAdapterPosition());
                return true;
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


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.quest_check)
        CheckBox check;

        @Bind(R.id.quest_name)
        TextView name;

        @Bind(R.id.quest_indicator)
        View indicator;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}