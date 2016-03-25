package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.ui.formatters.DueDateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.StartTimeFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class HabitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {
    private final Context context;

    private List<Quest> quests;
    private Bus eventBus;

    public HabitsAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        this.quests = quests;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.habits_quest_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ViewHolder questHolder = (ViewHolder) holder;

        final Quest q = quests.get(questHolder.getAdapterPosition());

        questHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new ShowQuestEvent(q));
            }
        });

        questHolder.name.setText(q.getName());

        GradientDrawable drawable = (GradientDrawable) questHolder.indicator.getBackground();
        drawable.setColor(ContextCompat.getColor(context, Quest.getContext(q).resLightColor));

        if (Quest.isStarted(q)) {
            Animation blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
            questHolder.indicator.startAnimation(blinkAnimation);
        }

        if (q.getStartTime() != null) {
            questHolder.startTime.setVisibility(View.VISIBLE);
            questHolder.startTime.setText(StartTimeFormatter.format(q.getStartTime()));
        } else {
            questHolder.startTime.setVisibility(View.INVISIBLE);
        }

        boolean isUpcoming = !DateUtils.isToday(q.getDue()) && !DateUtils.isTomorrow(q.getDue());
        if (q.getDue() != null && isUpcoming) {
            questHolder.dueDate.setVisibility(View.VISIBLE);
            questHolder.dueDate.setText(DueDateFormatter.formatWithoutYear(q.getDue()));
        } else {
            questHolder.dueDate.setVisibility(View.GONE);
        }

        if (q.getDuration() > 0) {
            questHolder.duration.setVisibility(View.VISIBLE);
            questHolder.duration.setText(DurationFormatter.format(context, q.getDuration()));
        } else {
            questHolder.duration.setVisibility(View.INVISIBLE);
        }

        if (new Random().nextFloat() < 0.5) {
            questHolder.habitIndicatorsContainer.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return quests.size();
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
        if (direction == ItemTouchHelper.END) {
            eventBus.post(new CompleteQuestRequestEvent(q));
        } else if (direction == ItemTouchHelper.START) {
            eventBus.post(new ScheduleQuestForTodayEvent(q));
        }
    }

    public void updateQuests(List<Quest> newQuests) {
        quests = newQuests;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        @Bind(R.id.quest_text)
        public TextView name;

        @Bind(R.id.quest_start_time)
        public TextView startTime;

        @Bind(R.id.quest_duration)
        public TextView duration;

        @Bind(R.id.quest_context_indicator)
        public View indicator;

        @Bind(R.id.quest_habit_indicators_container)
        public View habitIndicatorsContainer;

        @Bind(R.id.quest_due_date)
        public TextView dueDate;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }

        @Override
        public void onItemSwipeStart(int direction) {
            if (direction == ItemTouchHelper.START) {
                showScheduleForToday();
                hideQuestCompleteCheck();
            } else if (direction == ItemTouchHelper.END) {
                showQuestCompleteCheck();
                hideScheduleForToday();
            }
        }

        private void showScheduleForToday() {
            changeScheduleVisibility(View.VISIBLE, View.GONE);
        }

        private void showQuestCompleteCheck() {
            changeCheckVisibility(View.VISIBLE, View.GONE);
        }

        private void hideScheduleForToday() {
            changeScheduleVisibility(View.GONE, View.VISIBLE);
        }

        private void hideQuestCompleteCheck() {
            changeCheckVisibility(View.GONE, View.VISIBLE);
        }

        private void changeScheduleVisibility(int iconVisibility, int durationVisibility) {
            itemView.findViewById(R.id.quest_schedule_for_today).setVisibility(iconVisibility);
            itemView.findViewById(R.id.quest_duration).setVisibility(durationVisibility);
        }

        private void changeCheckVisibility(int iconVisibility, int startTimeVisibility) {
            itemView.findViewById(R.id.quest_complete_check).setVisibility(iconVisibility);
            itemView.findViewById(R.id.quest_start_date_time_container).setVisibility(startTimeVisibility);
        }

        @Override
        public void onItemSwipeStopped(int direction) {
            if (direction == ItemTouchHelper.START) {
                hideScheduleForToday();
            } else if (direction == ItemTouchHelper.END) {
                hideQuestCompleteCheck();
            }
        }

        @Override
        public boolean isEndSwipeEnabled() {
            return true;
        }

        @Override
        public boolean isStartSwipeEnabled() {
            return true;
        }
    }
}