package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.DeleteRepeatingQuestRequestEvent;
import io.ipoli.android.quest.events.ShowRepeatingQuestEvent;
import io.ipoli.android.quest.ui.events.EditRepeatingQuestRequestEvent;
import io.ipoli.android.quest.viewmodels.RepeatingQuestViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class RepeatingQuestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;

    private List<RepeatingQuestViewModel> viewModels;
    private Bus eventBus;

    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public RepeatingQuestListAdapter(Context context, List<RepeatingQuestViewModel> viewModels, Bus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        this.viewModels = viewModels;
        viewBinderHelper.setOpenOnlyOne(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.repeating_quest_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ViewHolder questHolder = (ViewHolder) holder;
        final RepeatingQuestViewModel vm = viewModels.get(questHolder.getAdapterPosition());
        RepeatingQuest rq = vm.getRepeatingQuest();

        viewBinderHelper.bind(questHolder.swipeLayout, rq.getId());
        questHolder.swipeLayout.close(false);

        questHolder.swipeLayout.setSwipeListener(new SwipeRevealLayout.SimpleSwipeListener() {
            @Override
            public void onOpened(SwipeRevealLayout view) {
                super.onOpened(view);
                eventBus.post(new ItemActionsShownEvent(EventSource.REPEATING_QUESTS));
            }
        });

        questHolder.deleteQuest.setOnClickListener(v -> eventBus.post(new DeleteRepeatingQuestRequestEvent(rq, EventSource.REPEATING_QUESTS)));

        questHolder.editQuest.setOnClickListener(v -> {
            questHolder.swipeLayout.close(true);
            eventBus.post(new EditRepeatingQuestRequestEvent(rq, EventSource.REPEATING_QUESTS));
        });

        questHolder.contentLayout.setOnClickListener(view ->
                eventBus.post(new ShowRepeatingQuestEvent(rq, EventSource.REPEATING_QUESTS)));

        questHolder.name.setText(vm.getName());

        GradientDrawable drawable = (GradientDrawable) questHolder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, vm.getCategoryColor()));

        questHolder.contextIndicatorImage.setImageResource(vm.getCategoryImage());

        questHolder.nextDateTime.setText(vm.getNextText());

        questHolder.repeatFrequency.setText(vm.getRepeatText());

        questHolder.progressContainer.removeAllViews();

        if (vm.getTotalCount() == 0) {
            questHolder.progressSpace.setVisibility(View.GONE);
        } else {
            questHolder.progressSpace.setVisibility(View.VISIBLE);
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        for (int i = 1; i <= vm.getCompletedDailyCount(); i++) {
            View progressView = inflater.inflate(R.layout.repeating_quest_progress_context_indicator, questHolder.progressContainer, false);
            GradientDrawable progressViewBackground = (GradientDrawable) progressView.getBackground();
            progressViewBackground.setColor(ContextCompat.getColor(context, vm.getCategoryColor()));
            questHolder.progressContainer.addView(progressView);
        }

        for (int i = 1; i <= vm.getRemainingDailyCount(); i++) {
            View progressViewEmpty = inflater.inflate(R.layout.repeating_quest_progress_context_indicator_empty, questHolder.progressContainer, false);
            GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();

            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1, context.getResources()), ContextCompat.getColor(context, vm.getCategoryColor()));
            questHolder.progressContainer.addView(progressViewEmpty);
        }
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void updateQuests(List<RepeatingQuestViewModel> newViewModels) {
        viewModels = newViewModels;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quest_name)
        public TextView name;

        @BindView(R.id.quest_category_indicator_background)
        public View contextIndicatorBackground;

        @BindView(R.id.quest_category_indicator_image)
        public ImageView contextIndicatorImage;

        @BindView(R.id.quest_progress_container)
        public ViewGroup progressContainer;

        @BindView(R.id.quest_next_datetime)
        public TextView nextDateTime;

        @BindView(R.id.quest_remaining)
        public TextView repeatFrequency;

        @BindView(R.id.quest_progress_space)
        public Space progressSpace;

        @BindView(R.id.content_layout)
        public RelativeLayout contentLayout;

        @BindView(R.id.swipe_layout)
        public SwipeRevealLayout swipeLayout;

        @BindView(R.id.edit_quest)
        public ImageButton editQuest;

        @BindView(R.id.delete_quest)
        public ImageButton deleteQuest;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}