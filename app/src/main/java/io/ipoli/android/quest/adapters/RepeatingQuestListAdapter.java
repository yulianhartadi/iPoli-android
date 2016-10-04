package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    public RepeatingQuestListAdapter(Context context, List<RepeatingQuestViewModel> viewModels, Bus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        this.viewModels = viewModels;
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

        questHolder.moreMenu.setOnClickListener(v -> {
            eventBus.post(new ItemActionsShownEvent(EventSource.REPEATING_QUESTS));
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.repeating_quest_actions_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.edit_repeating_quest:
                        eventBus.post(new EditRepeatingQuestRequestEvent(rq, EventSource.REPEATING_QUESTS));
                        return true;
                    case R.id.delete_repeating_quest:
                        eventBus.post(new DeleteRepeatingQuestRequestEvent(rq, EventSource.REPEATING_QUESTS));
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });

        questHolder.contentLayout.setOnClickListener(view ->
                eventBus.post(new ShowRepeatingQuestEvent(rq, EventSource.REPEATING_QUESTS)));

        questHolder.name.setText(vm.getName());

        GradientDrawable drawable = (GradientDrawable) questHolder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, vm.getCategoryColor()));

        questHolder.contextIndicatorImage.setImageResource(vm.getCategoryImage());

        if(!vm.isLoaded()) {
            showLoading(questHolder);
            return;
        }

        hideLoading(questHolder);

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

    private void hideLoading(ViewHolder questHolder) {
        questHolder.indicatorsContainer.setVisibility(View.VISIBLE);
        questHolder.nextDateTime.setVisibility(View.VISIBLE);
        questHolder.loader.setVisibility(View.GONE);
    }

    private void showLoading(ViewHolder questHolder) {
        questHolder.indicatorsContainer.setVisibility(View.GONE);
        questHolder.nextDateTime.setVisibility(View.GONE);
        questHolder.loader.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }


    public void add(RepeatingQuestViewModel viewModel) {
        viewModels.add(viewModel);
        notifyItemInserted(viewModels.size() - 1);
    }

    public void updateViewModel(RepeatingQuestViewModel viewModel) {
        int index = -1;
        for(int i = 0; i< viewModels.size(); i++) {
            if(viewModels.get(i).getRepeatingQuest().getId().equals(viewModel.getRepeatingQuest().getId())) {
                index = i;
                break;
            }
        }

        if(index > -1) {
            viewModels.set(index, viewModel);
            notifyItemChanged(index);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quest_name)
        public TextView name;

        @BindView(R.id.quest_category_indicator_background)
        public View contextIndicatorBackground;

        @BindView(R.id.quest_category_indicator_image)
        public ImageView contextIndicatorImage;

        @BindView(R.id.quest_details_loader)
        public TextView loader;

        @BindView(R.id.quest_repeating_quest_indicators_container)
        public ViewGroup indicatorsContainer;

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

        @BindView(R.id.repeating_quest_more_menu)
        public ImageButton moreMenu;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}