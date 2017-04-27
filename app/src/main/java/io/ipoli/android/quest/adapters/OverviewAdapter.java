package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DuplicateQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.SnoozeQuestRequestEvent;
import io.ipoli.android.quest.events.StartQuestRequestEvent;
import io.ipoli.android.quest.events.StopQuestRequestEvent;
import io.ipoli.android.quest.ui.menus.DuplicateDateItem;
import io.ipoli.android.quest.ui.menus.DuplicateQuestItemsHelper;
import io.ipoli.android.quest.ui.menus.SnoozeQuestItemsHelper;
import io.ipoli.android.quest.ui.menus.SnoozeTimeItem;
import io.ipoli.android.quest.viewmodels.QuestViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_ITEM_VIEW_TYPE = 0;
    private static final int QUEST_ITEM_VIEW_TYPE = 1;
    private static final int COMPLETED_QUEST_ITEM_VIEW_TYPE = 2;

    private final Context context;
    private final LayoutInflater inflater;

    private List<Object> items;
    private final Bus eventBus;
    private final LocalDate today;

    public OverviewAdapter(Context context, Bus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        this.inflater = LayoutInflater.from(context);
        this.today = LocalDate.now();
        items = new ArrayList<>();
    }

    private void setItems(SortedMap<LocalDate, List<QuestViewModel>> viewModels) {
        items = new ArrayList<>();
        List<QuestViewModel> completed = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        if (viewModels.containsKey(today) && !viewModels.get(today).isEmpty()) {
            List<QuestViewModel> vms = viewModels.get(today);
            for (QuestViewModel vm : vms) {
                if (!vm.isCompleted()) {
                    items.add(vm);
                } else {
                    completed.add(vm);
                }
            }
            if (!items.isEmpty()) {
                items.add(0, R.string.today);
            }
            viewModels.remove(today);
        }

        Collections.sort(completed, createCompletedQuestsComparator());

        if (viewModels.containsKey(tomorrow)) {
            items.add(R.string.tomorrow);
            List<QuestViewModel> vms = viewModels.get(tomorrow);
            for (QuestViewModel vm : vms) {
                if (!vm.hasTimesADay()) {
                    items.add(vm);
                }
            }
            viewModels.remove(tomorrow);
        }

        if (viewModels.containsKey(yesterday)) {
            List<QuestViewModel> completedYesterday = new ArrayList<>();
            for (QuestViewModel vm : viewModels.get(yesterday)) {
                if (vm.isCompleted()) {
                    completedYesterday.add(vm);
                }
            }
            Collections.sort(completedYesterday, createCompletedQuestsComparator());
            completed.addAll(completedYesterday);
            viewModels.remove(yesterday);
        }

        if (!viewModels.isEmpty()) {
            items.add(R.string.next_7_days);
            for (List<QuestViewModel> questsForDay : viewModels.values()) {
                for (QuestViewModel vm : questsForDay) {
                    if (!vm.hasTimesADay()) {
                        items.add(vm);
                    }
                }
            }
        }

        if (!completed.isEmpty()) {
            items.add(R.string.completed);
            items.addAll(completed);
        }
    }

    @NonNull
    private Comparator<QuestViewModel> createCompletedQuestsComparator() {
        return Collections.reverseOrder((vm1, vm2) -> Integer.compare(vm1.getCompletedAtMinute(), vm2.getCompletedAtMinute()));
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof Integer) {
            return HEADER_ITEM_VIEW_TYPE;
        }
        QuestViewModel vm = (QuestViewModel) item;
        if (vm.isCompleted()) {
            return COMPLETED_QUEST_ITEM_VIEW_TYPE;
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

            case COMPLETED_QUEST_ITEM_VIEW_TYPE:
                return new CompletedQuestViewHolder(inflater.inflate(R.layout.overview_completed_quest_item, parent, false));

            default:
                return new QuestViewHolder(inflater.inflate(R.layout.overview_quest_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == QUEST_ITEM_VIEW_TYPE) {
            bindQuestViewHolder((QuestViewHolder) holder);
        } else if (holder.getItemViewType() == COMPLETED_QUEST_ITEM_VIEW_TYPE) {
            bindCompletedQuestViewHolder((CompletedQuestViewHolder) holder);
        } else if (holder.getItemViewType() == HEADER_ITEM_VIEW_TYPE) {
            bindHeaderView(holder, position);
        }
    }

    private void bindCompletedQuestViewHolder(CompletedQuestViewHolder holder) {
        final QuestViewModel vm = (QuestViewModel) items.get(holder.getAdapterPosition());
        holder.name.setText(vm.getName());
        holder.dueDate.setText(vm.getDueDateText(today));
    }

    private void bindHeaderView(RecyclerView.ViewHolder holder, int position) {
        TextView header = (TextView) holder.itemView;
        Integer textRes = (Integer) items.get(position);
        header.setText(textRes);
    }

    private void bindQuestViewHolder(QuestViewHolder holder) {

        final QuestViewModel vm = (QuestViewModel) items.get(holder.getAdapterPosition());

        Quest q = vm.getQuest();

        holder.moreMenu.setOnClickListener(v -> showPopupMenu(q, v));

        holder.contentLayout.setOnClickListener(view -> eventBus.post(new ShowQuestEvent(vm.getQuest(), EventSource.OVERVIEW)));
        holder.name.setText(vm.getName());

        if (vm.isStarted()) {
            GradientDrawable drawable = (GradientDrawable) holder.runningIndicator.getBackground();
            drawable.setColor(ContextCompat.getColor(context, vm.getCategoryColor()));
            Animation blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
            holder.runningIndicator.startAnimation(blinkAnimation);
            holder.runningIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.runningIndicator.setVisibility(View.GONE);
        }

        holder.contextIndicatorImage.setImageResource(vm.getCategoryImage());
        if(q.getScheduledDate().isEqual(today) || q.getScheduledDate().isEqual(today.plusDays(1))) {
            holder.dueDate.setVisibility(View.GONE);
        } else {
            holder.dueDate.setVisibility(View.VISIBLE);
            holder.dueDate.setText(vm.getDueDateText(today));
        }

        String scheduleText = vm.getScheduleText();

        holder.progressContainer.removeAllViews();

        holder.repeatingIndicator.setVisibility(vm.isRecurrent() ? View.VISIBLE : View.GONE);
        holder.priorityIndicator.setVisibility(vm.isMostImportant() ? View.VISIBLE : View.GONE);
        holder.challengeIndicator.setVisibility(vm.isForChallenge() ? View.VISIBLE : View.GONE);

        String remainingText = vm.getRemainingText();
        if (TextUtils.isEmpty(scheduleText) && TextUtils.isEmpty(remainingText)) {
            holder.detailsContainer.setVisibility(View.GONE);
            return;
        }

        holder.detailsContainer.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(scheduleText)) {
            holder.scheduleText.setVisibility(View.GONE);
        } else {
            holder.scheduleText.setVisibility(View.VISIBLE);
            holder.scheduleText.setText(scheduleText);
        }
        holder.remainingText.setText(remainingText);

        if (!vm.hasTimesADay()) {
            return;
        }

        for (int i = 1; i <= vm.getCompletedCount(); i++) {
            View progressView = inflater.inflate(R.layout.repeating_quest_progress_context_indicator, holder.progressContainer, false);
            GradientDrawable progressViewBackground = (GradientDrawable) progressView.getBackground();
            progressViewBackground.setColor(ContextCompat.getColor(context, vm.getCategoryColor()));
            holder.progressContainer.addView(progressView);
        }

        for (int i = 1; i <= vm.getRemainingCount(); i++) {
            View progressViewEmpty = inflater.inflate(R.layout.repeating_quest_progress_context_indicator_empty, holder.progressContainer, false);
            GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();

            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1, context.getResources()), ContextCompat.getColor(context, vm.getCategoryColor()));
            holder.progressContainer.addView(progressViewEmpty);
        }
    }

    private void showPopupMenu(Quest quest, View view) {
        eventBus.post(new ItemActionsShownEvent(EventSource.OVERVIEW));
        PopupMenu pm = new PopupMenu(context, view);
        pm.inflate(R.menu.overview_quest_actions_menu);

        MenuItem startItem = pm.getMenu().findItem(R.id.quest_start);
        if (quest.isScheduledForToday()) {
            startItem.setTitle(quest.isStarted() ? R.string.stop : R.string.start);
        } else {
            startItem.setVisible(false);
        }

        MenuItem scheduleQuestItem = pm.getMenu().findItem(R.id.schedule_quest);
        scheduleQuestItem.setTitle(quest.isScheduledForToday() ? context.getString(R.string.snooze_for_tomorrow) : context.getString(R.string.do_today));

        Map<Integer, DuplicateDateItem> itemIdToDuplicateDateItem = DuplicateQuestItemsHelper
                .createDuplicateDateMap(context, pm.getMenu().findItem(R.id.quest_duplicate));

        Map<Integer, SnoozeTimeItem> itemIdToSnoozeTimeItem = SnoozeQuestItemsHelper
                .createSnoozeTimeMap(quest, pm.getMenu().findItem(R.id.quest_snooze));

        pm.setOnMenuItemClickListener(item -> {
            if (itemIdToDuplicateDateItem.containsKey(item.getItemId())) {
                eventBus.post(new DuplicateQuestRequestEvent(quest, itemIdToDuplicateDateItem.get(item.getItemId()).date, EventSource.OVERVIEW));
                return true;
            }

            if (itemIdToSnoozeTimeItem.containsKey(item.getItemId())) {
                SnoozeTimeItem snoozeTimeItem = itemIdToSnoozeTimeItem.get(item.getItemId());
                eventBus.post(new SnoozeQuestRequestEvent(quest, snoozeTimeItem.minutes, snoozeTimeItem.date, snoozeTimeItem.pickTime, snoozeTimeItem.pickDate, EventSource.OVERVIEW));
                return true;
            }

            switch (item.getItemId()) {
                case R.id.quest_start:
                    if (!quest.isStarted()) {
                        eventBus.post(new StartQuestRequestEvent(quest));
                    } else {
                        eventBus.post(new StopQuestRequestEvent(quest));
                    }
                    return true;
                case R.id.complete_quest:
                    eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.OVERVIEW));
                    return true;
                case R.id.schedule_quest:
                    eventBus.post(new ScheduleQuestForTodayEvent(quest, EventSource.OVERVIEW));
                    return true;
                case R.id.edit_quest:
                    eventBus.post(new EditQuestRequestEvent(quest.getId(), EventSource.OVERVIEW));
                    return true;
                case R.id.delete_quest:
                    eventBus.post(new DeleteQuestRequestEvent(quest, EventSource.OVERVIEW));
                    return true;
            }
            return false;
        });
        pm.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateQuests(SortedMap<LocalDate, List<QuestViewModel>> viewModels) {
        setItems(viewModels);
        notifyDataSetChanged();
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class CompletedQuestViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quest_name)
        TextView name;

        @BindView(R.id.quest_due_date)
        TextView dueDate;

        CompletedQuestViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quest_name)
        TextView name;

        @BindView(R.id.quest_running_indicator)
        View runningIndicator;

        @BindView(R.id.quest_category_indicator_image)
        ImageView contextIndicatorImage;

        @BindView(R.id.quest_duration)
        TextView scheduleText;

        @BindView(R.id.quest_due_date)
        TextView dueDate;

        @BindView(R.id.quest_details_container)
        ViewGroup detailsContainer;

        @BindView(R.id.quest_remaining)
        TextView remainingText;

        @BindView(R.id.quest_progress_container)
        ViewGroup progressContainer;

        @BindView(R.id.quest_repeating_indicator)
        ImageView repeatingIndicator;

        @BindView(R.id.quest_priority_indicator)
        ImageView priorityIndicator;

        @BindView(R.id.quest_challenge_indicator)
        ImageView challengeIndicator;

        @BindView(R.id.content_layout)
        RelativeLayout contentLayout;

        @BindView(R.id.quest_more_menu)
        ImageButton moreMenu;


        QuestViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}