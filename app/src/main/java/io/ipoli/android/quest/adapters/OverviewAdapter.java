package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.viewmodels.QuestViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int HEADER_ITEM_VIEW_TYPE = 0;
    public static final int QUEST_ITEM_VIEW_TYPE = 1;
    private final Context context;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    private List<Object> items;
    private Bus eventBus;

    private int[] headerIndices;

    public OverviewAdapter(Context context, List<QuestViewModel> viewModels, Bus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        viewBinderHelper.setOpenOnlyOne(true);
        setItems(viewModels);
    }

    private void setItems(List<QuestViewModel> viewModels) {
        items = new ArrayList<>();
        List<QuestViewModel> visibleQuests = new ArrayList<>();
        for (QuestViewModel vm : viewModels) {
            Quest q = vm.getQuest();
            if (q.isScheduledForToday() || !q.isIndicator()) {
                visibleQuests.add(vm);
            }
        }
        if (visibleQuests.isEmpty()) {
            return;
        }
        calculateHeaderIndices(visibleQuests);
        items.addAll(visibleQuests);
        if (headerIndices[0] >= 0) {
            items.add(headerIndices[0], R.string.today);
        }
        if (headerIndices[1] >= 0) {
            items.add(headerIndices[1], R.string.tomorrow);
        }
        if (headerIndices[2] >= 0) {
            items.add(headerIndices[2], R.string.next_7_days);
        }
    }

    private void calculateHeaderIndices(List<QuestViewModel> quests) {
        headerIndices = new int[]{-1, -1, -1};
        List<Quest> todayQuests = new ArrayList<>();
        List<Quest> tomorrowQuests = new ArrayList<>();
        List<Quest> upcomingQuests = new ArrayList<>();

        for (QuestViewModel vm : quests) {
            Quest q = vm.getQuest();
            if (q.isScheduledForToday()) {
                todayQuests.add(q);
            } else if (q.isScheduledForTomorrow()) {
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
                return new HeaderViewHolder(inflater.inflate(R.layout.overview_quest_header_item, parent, false));

            default:
                return new QuestViewHolder(inflater.inflate(R.layout.overview_quest_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == QUEST_ITEM_VIEW_TYPE) {

            QuestViewHolder questHolder = (QuestViewHolder) holder;

            final QuestViewModel vm = (QuestViewModel) items.get(questHolder.getAdapterPosition());

            Quest q = vm.getQuest();
            viewBinderHelper.bind(questHolder.swipeLayout, q.getId());
            questHolder.swipeLayout.close(false);
            questHolder.swipeLayout.setSwipeListener(new SwipeRevealLayout.SimpleSwipeListener() {
                @Override
                public void onOpened(SwipeRevealLayout view) {
                    super.onOpened(view);
                    eventBus.post(new ItemActionsShownEvent(EventSource.OVERVIEW));
                }
            });
            questHolder.scheduleQuest.setOnClickListener(v -> {
                eventBus.post(new ScheduleQuestForTodayEvent(q, EventSource.OVERVIEW));
            });

            questHolder.completeQuest.setOnClickListener(v -> {
                eventBus.post(new CompleteQuestRequestEvent(q, EventSource.OVERVIEW));
            });

            questHolder.editQuest.setOnClickListener(v -> {
                questHolder.swipeLayout.close(true);
                eventBus.post(new EditQuestRequestEvent(q, EventSource.OVERVIEW));
            });

            questHolder.deleteQuest.setOnClickListener(v -> {
                eventBus.post(new DeleteQuestRequestEvent(q, EventSource.OVERVIEW));
            });

            questHolder.contentLayout.setOnClickListener(view -> eventBus.post(new ShowQuestEvent(vm.getQuest(), EventSource.OVERVIEW)));
            questHolder.name.setText(vm.getName());

            if (vm.isStarted()) {
                GradientDrawable drawable = (GradientDrawable) questHolder.runningIndicator.getBackground();
                drawable.setColor(ContextCompat.getColor(context, vm.getContextColor()));
                Animation blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
                questHolder.runningIndicator.startAnimation(blinkAnimation);
                questHolder.runningIndicator.setVisibility(View.VISIBLE);
            } else {
                questHolder.runningIndicator.setVisibility(View.GONE);
            }

            GradientDrawable drawable = (GradientDrawable) questHolder.contextIndicatorBackground.getBackground();
            drawable.setColor(ContextCompat.getColor(context, vm.getContextColor()));

            questHolder.contextIndicatorImage.setImageResource(vm.getContextImage());
            questHolder.dueDate.setText(vm.getDueDateText());

            String scheduleText = vm.getScheduleText();

            questHolder.progressContainer.removeAllViews();

            int recurIconVisibility = vm.isRecurrent() ? View.VISIBLE : View.GONE;
            questHolder.recurrentIcon.setVisibility(recurIconVisibility);

            if (TextUtils.isEmpty(scheduleText) && TextUtils.isEmpty(vm.getRemainingText())) {
                questHolder.detailsContainer.setVisibility(View.GONE);
                return;
            }

            questHolder.detailsContainer.setVisibility(View.VISIBLE);

            if (TextUtils.isEmpty(vm.getScheduleText())) {
                questHolder.scheduleText.setVisibility(View.GONE);
            } else {
                questHolder.scheduleText.setVisibility(View.VISIBLE);
                questHolder.scheduleText.setText(vm.getScheduleText());
            }
            questHolder.remainingText.setText(vm.getRemainingText());

            if (!vm.hasTimesPerDay()) {
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(context);

            for (int i = 1; i <= vm.getCompletedCount(); i++) {
                View progressView = inflater.inflate(R.layout.repeating_quest_progress_context_indicator, questHolder.progressContainer, false);
                GradientDrawable progressViewBackground = (GradientDrawable) progressView.getBackground();
                progressViewBackground.setColor(ContextCompat.getColor(context, vm.getContextColor()));
                questHolder.progressContainer.addView(progressView);
            }

            for (int i = 1; i <= vm.getRemainingCount(); i++) {
                View progressViewEmpty = inflater.inflate(R.layout.repeating_quest_progress_context_indicator_empty, questHolder.progressContainer, false);
                GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();

                progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1, context.getResources()), ContextCompat.getColor(context, vm.getContextColor()));
                questHolder.progressContainer.addView(progressViewEmpty);
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

    public void updateQuests(List<QuestViewModel> viewModels) {
        setItems(viewModels);
        notifyDataSetChanged();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class QuestViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quest_name)
        public TextView name;

        @BindView(R.id.quest_running_indicator)
        View runningIndicator;

        @BindView(R.id.quest_context_indicator_background)
        public View contextIndicatorBackground;

        @BindView(R.id.quest_context_indicator_image)
        public ImageView contextIndicatorImage;

        @BindView(R.id.quest_duration)
        public TextView scheduleText;

        @BindView(R.id.quest_due_date)
        public TextView dueDate;

        @BindView(R.id.quest_details_container)
        public ViewGroup detailsContainer;

        @BindView(R.id.quest_remaining)
        public TextView remainingText;

        @BindView(R.id.quest_progress_container)
        public ViewGroup progressContainer;

        @BindView(R.id.quest_recurrent_indicator)
        public ImageView recurrentIcon;

        @BindView(R.id.content_layout)
        public RelativeLayout contentLayout;

        @BindView(R.id.swipe_layout)
        public SwipeRevealLayout swipeLayout;

        @BindView(R.id.schedule_quest)
        public ImageButton scheduleQuest;

        @BindView(R.id.complete_quest)
        public ImageButton completeQuest;

        @BindView(R.id.edit_quest)
        public ImageButton editQuest;

        @BindView(R.id.delete_quest)
        public ImageButton deleteQuest;

        public QuestViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}