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
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.ui.menus.CalendarQuestPopupMenu;
import io.ipoli.android.quest.viewmodels.UnscheduledQuestViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class UnscheduledQuestsAdapter extends RecyclerView.Adapter<UnscheduledQuestsAdapter.ViewHolder> {

    private Context context;

    private List<UnscheduledQuestViewModel> viewModels;

    private final Bus eventBus;

    public UnscheduledQuestsAdapter(Context context, List<UnscheduledQuestViewModel> viewModels, Bus eventBus) {
        this.context = context;
        this.viewModels = viewModels;
        this.eventBus = eventBus;
    }

    @Override
    public UnscheduledQuestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.unscheduled_quest_item, parent, false);
        return new ViewHolder(v);
    }

    public List<UnscheduledQuestViewModel> getViewModels() {
        return viewModels;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final UnscheduledQuestViewModel vm = viewModels.get(holder.getAdapterPosition());
        Quest q = vm.getQuest();

        holder.name.setText(vm.getName());

        holder.repeatingIndicator.setVisibility(vm.isRepeating() ? View.VISIBLE : View.GONE);
        holder.priorityIndicator.setVisibility(vm.isMostImportant() ? View.VISIBLE : View.GONE);
        holder.challengeIndicator.setVisibility(vm.isForChallenge() ? View.VISIBLE : View.GONE);

        GradientDrawable drawable = (GradientDrawable) holder.categoryIndicator.getBackground();
        drawable.setColor(ContextCompat.getColor(context, vm.getCategoryColor()));

        if (vm.isStarted()) {
            Animation blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
            holder.categoryIndicator.startAnimation(blinkAnimation);
        }

        if (vm.getQuest().isPlaceholder()) {
            holder.itemView.setOnClickListener(null);
            holder.itemView.setOnLongClickListener(null);
            holder.check.setVisibility(View.GONE);
            holder.moreMenuContainer.setVisibility(View.GONE);
        } else {

            holder.check.setVisibility(View.VISIBLE);
            holder.moreMenuContainer.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(view -> {
                eventBus.post(new ShowQuestEvent(q, EventSource.CALENDAR_UNSCHEDULED_SECTION));
            });

            if (vm.getQuest().shouldBeDoneMultipleTimesPerDay()) {
                holder.itemView.setOnLongClickListener(null);
            } else {
                holder.itemView.setOnLongClickListener(view -> {
                    eventBus.post(new MoveQuestToCalendarRequestEvent(vm, holder.getAdapterPosition()));
                    return true;
                });
            }

            holder.check.setOnCheckedChangeListener(null);
            holder.check.setChecked(false);
            holder.check.setOnCheckedChangeListener((compoundButton, checked) -> {
                if (checked) {
                    eventBus.post(new CompleteUnscheduledQuestRequestEvent(vm));
                }
            });

            holder.moreMenu.setOnClickListener(view -> CalendarQuestPopupMenu.show(view, q, eventBus, EventSource.CALENDAR_UNSCHEDULED_SECTION));
        }
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void addQuest(int position, UnscheduledQuestViewModel viewModel) {
        viewModels.add(position, viewModel);
        notifyItemInserted(position);
    }

    public void removeQuest(UnscheduledQuestViewModel viewModel) {
        int position = viewModels.indexOf(viewModel);
        viewModels.remove(viewModel);
        notifyItemRemoved(position);
    }

    public void updateQuests(List<UnscheduledQuestViewModel> viewModels) {
        this.viewModels = viewModels;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.quest_check)
        CheckBox check;

        @BindView(R.id.quest_text)
        TextView name;

        @BindView(R.id.quest_category_indicator)
        View categoryIndicator;

        @BindView(R.id.quest_priority_indicator)
        View priorityIndicator;

        @BindView(R.id.quest_repeating_indicator)
        View repeatingIndicator;

        @BindView(R.id.quest_challenge_indicator)
        View challengeIndicator;

        @BindView(R.id.quest_more_menu)
        ImageButton moreMenu;

        @BindView(R.id.quest_more_menu_container)
        ViewGroup moreMenuContainer;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}