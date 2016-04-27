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
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.ScheduleQuestRequestEvent;
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

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final UnscheduledQuestViewModel vm = viewModels.get(position);
        holder.itemView.setOnClickListener(view -> eventBus.post(new ShowQuestEvent(vm.getQuest(), "unscheduled_calendar_section")));

        GradientDrawable drawable = (GradientDrawable) holder.indicator.getBackground();
        drawable.setColor(ContextCompat.getColor(context, vm.getContextColor()));

        if (vm.isStarted()) {
            Animation blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
            holder.indicator.startAnimation(blinkAnimation);
        }

        holder.name.setText(vm.getName());
        holder.itemView.setOnLongClickListener(view -> {
            eventBus.post(new MoveQuestToCalendarRequestEvent(vm, holder.getAdapterPosition()));
            return true;
        });

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(false);
        holder.check.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                eventBus.post(new CompleteUnscheduledQuestRequestEvent(vm));
            }
        });

        holder.schedule.setOnClickListener(v -> {
            eventBus.post(new ScheduleQuestRequestEvent(vm));
        });
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
        if (viewModel.getRemainingCount() > 1) {
            viewModel.decreaseRemainingCount();
            notifyItemChanged(position);
            return;
        }
        viewModels.remove(viewModel);
        notifyItemRemoved(position);
    }

    public void updateQuests(List<UnscheduledQuestViewModel> viewModels) {
        this.viewModels = viewModels;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.quest_check)
        CheckBox check;

        @Bind(R.id.quest_text)
        TextView name;

        @Bind(R.id.quest_context_indicator)
        View indicator;

        @Bind(R.id.quest_schedule)
        View schedule;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}