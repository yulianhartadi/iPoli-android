package io.ipoli.android.quest;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.app.utils.DateUtils;
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
public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    public static final int HEADER_ITEM_VIEW_TYPE = 0;
    public static final int QUEST_ITEM_VIEW_TYPE = 1;
    private final Context context;

    private List<Object> items;
    private Bus eventBus;

    private int[] headerIndices;

    public OverviewAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        setItems(quests);
    }

    private void setItems(List<Quest> quests) {
        calculateHeaderIndices(quests);
        items = new ArrayList<>();
        items.addAll(quests);
        if (headerIndices[0] >= 0) {
            items.add(headerIndices[0], R.string.today);
        }
        if (headerIndices[1] >= 0) {
            items.add(headerIndices[1], R.string.tomorrow);
        }
        if (headerIndices[2] >= 0) {
            items.add(headerIndices[2], R.string.upcoming);
        }
    }

    private void calculateHeaderIndices(List<Quest> quests) {
        headerIndices = new int[]{-1, -1, -1};
        List<Quest> todayQuests = new ArrayList<>();
        List<Quest> tomorrowQuests = new ArrayList<>();
        List<Quest> upcomingQuests = new ArrayList<>();

        for (Quest q : quests) {
            if (DateUtils.isToday(q.getDue())) {
                todayQuests.add(q);
            } else if (DateUtils.isTomorrow(q.getDue())) {
                tomorrowQuests.add(q);
            } else {
                upcomingQuests.add(q);
            }
        }

        int freeIndex = 0;
        if (!todayQuests.isEmpty()) {
            headerIndices[0] = 0;
            freeIndex = todayQuests.size() + 1;
        }

        if (!tomorrowQuests.isEmpty()) {
            headerIndices[1] = freeIndex;
            freeIndex = freeIndex + tomorrowQuests.size() + 1;
        }

        if (!upcomingQuests.isEmpty()) {
            headerIndices[2] = freeIndex;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Integer) {
            return HEADER_ITEM_VIEW_TYPE;
        }
        return QUEST_ITEM_VIEW_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {

            case HEADER_ITEM_VIEW_TYPE:
                return new HeaderViewHolder(inflater.inflate(R.layout.quest_header_item, parent, false));

            default:
                return new QuestViewHolder(inflater.inflate(R.layout.quest_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == QUEST_ITEM_VIEW_TYPE) {

            QuestViewHolder questHolder = (QuestViewHolder) holder;

            final Quest q = (Quest) items.get(questHolder.getAdapterPosition());

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
        } else if (holder.getItemViewType() == HEADER_ITEM_VIEW_TYPE) {
            TextView header = (TextView) holder.itemView;
            Integer textRes = (Integer) items.get(position);
            header.setText(textRes);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void removeQuest(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismissed(int position, int direction) {
        Quest q = (Quest) items.get(position);
        items.remove(position);
        notifyItemRemoved(position);
        if (direction == ItemTouchHelper.END) {
            eventBus.post(new CompleteQuestRequestEvent(q));
        } else if (direction == ItemTouchHelper.START) {
            eventBus.post(new ScheduleQuestForTodayEvent(q));
        }
    }

    public void updateQuests(List<Quest> newQuests) {
        items.clear();
        setItems(newQuests);
        notifyDataSetChanged();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class QuestViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        @Bind(R.id.quest_text)
        public TextView name;

        @Bind(R.id.quest_start_time)
        public TextView startTime;

        @Bind(R.id.quest_duration)
        public TextView duration;

        @Bind(R.id.quest_context_indicator)
        public View indicator;

        @Bind(R.id.quest_due_date)
        public TextView dueDate;

        public QuestViewHolder(View v) {
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