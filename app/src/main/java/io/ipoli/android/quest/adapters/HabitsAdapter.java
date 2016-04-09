package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.viewmodels.RecurrentQuestViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class HabitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {
    private final Context context;

    private List<RecurrentQuestViewModel> viewModels;
    private Bus eventBus;

    public HabitsAdapter(Context context, List<RecurrentQuestViewModel> viewModels, Bus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        this.viewModels = viewModels;
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

        final RecurrentQuestViewModel vm = viewModels.get(questHolder.getAdapterPosition());

//        questHolder.itemView.setOnClickListener(view -> eventBus.post(new ShowQuestEvent(q)));

        questHolder.name.setText(vm.getName());

        GradientDrawable drawable = (GradientDrawable) questHolder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, vm.getContextColor()));

        questHolder.contextIndicatorImage.setImageResource(vm.getContextImage());


        LayoutInflater inflater = LayoutInflater.from(context);

        questHolder.nextDateTime.setText(vm.getNextText());

        questHolder.repeatFrequency.setText(vm.getRepeatText());

        for (int i = 1; i <= vm.getCompletedCount(); i++) {
            View progressView = inflater.inflate(R.layout.habit_progress_context_indicator, questHolder.progressContainer, false);
            GradientDrawable progressViewBackground = (GradientDrawable) progressView.getBackground();
            progressViewBackground.setColor(ContextCompat.getColor(context, vm.getContextColor()));
            questHolder.progressContainer.addView(progressView);
        }

        for (int i = 1; i <= vm.getRemainingCount(); i++) {
            View progressViewEmpty = inflater.inflate(R.layout.habit_progress_context_indicator_empty, questHolder.progressContainer, false);
            GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();

            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1, context.getResources()), ContextCompat.getColor(context, vm.getContextColor()));
            questHolder.progressContainer.addView(progressViewEmpty);
        }


    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void removeQuest(int position) {
        viewModels.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismissed(int position, int direction) {
//        RecurrentQuest q = viewModels.get(position);
//        viewModels.remove(position);
//        notifyItemRemoved(position);
//        if (direction == ItemTouchHelper.END) {
////            eventBus.post(new CompleteQuestRequestEvent(q));
//        } else if (direction == ItemTouchHelper.START) {
////            eventBus.post(new ScheduleQuestForTodayEvent(q));
//        }
    }

    public void updateQuests(List<RecurrentQuestViewModel> newViewModels) {
        viewModels = newViewModels;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        @Bind(R.id.quest_text)
        public TextView name;

        @Bind(R.id.quest_context_indicator_background)
        public View contextIndicatorBackground;

        @Bind(R.id.quest_context_indicator_image)
        public ImageView contextIndicatorImage;

        @Bind(R.id.progress_container)
        public ViewGroup progressContainer;

        @Bind(R.id.quest_habit_next_datetime)
        public TextView nextDateTime;

        @Bind(R.id.quest_habit_repeat_frequency)
        public TextView repeatFrequency;

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