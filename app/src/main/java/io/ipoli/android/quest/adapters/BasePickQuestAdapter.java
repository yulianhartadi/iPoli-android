package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.app.tutorial.PickQuestViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/28/16.
 */
public abstract class BasePickQuestAdapter extends RecyclerView.Adapter<BasePickQuestAdapter.ViewHolder> {
    protected Context context;
    protected final Bus evenBus;
    protected List<PickQuestViewModel> viewModels;
    private final boolean isRepeatingIndicatorVisible;

    public BasePickQuestAdapter(Context context, Bus evenBus, List<PickQuestViewModel> viewModels) {
        this(context, evenBus, viewModels, false);
    }

    public BasePickQuestAdapter(Context context, Bus evenBus, List<PickQuestViewModel> viewModels, boolean isRepeatingIndicatorVisible) {
        this.context = context;
        this.evenBus = evenBus;
        this.viewModels = viewModels;
        this.isRepeatingIndicatorVisible = isRepeatingIndicatorVisible;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PickQuestViewModel vm = viewModels.get(holder.getAdapterPosition());

        Category category = vm.getCategory();
        GradientDrawable drawable = (GradientDrawable) holder.categoryIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, category.color500));
        holder.categoryIndicatorImage.setImageResource(category.whiteImage);

        holder.name.setText(vm.getText());
        holder.repeatingIndicator.setVisibility(isRepeatingIndicatorVisible && vm.isRepeating() ? View.VISIBLE : View.GONE);

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(vm.isSelected());
        if (vm.isCompleted()) {
            holder.check.setEnabled(false);
        } else {
            holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    vm.select();
                    sendQuestSelectedEvent(holder.getAdapterPosition());
                } else {
                    vm.deselect();
                    sendQuestDeselectEvent(holder.getAdapterPosition());

                }
            });
            holder.itemView.setOnClickListener(view -> {
                CheckBox cb = holder.check;
                cb.setChecked(!cb.isChecked());

            });
        }
    }

    protected abstract void sendQuestDeselectEvent(int adapterPosition);

    protected abstract void sendQuestSelectedEvent(int adapterPosition);

    public List<BaseQuest> getSelectedBaseQuests() {
        List<BaseQuest> selectedQuests = new ArrayList<>();
        for (PickQuestViewModel vm : viewModels) {
            if (vm.isSelected()) {
                selectedQuests.add(vm.getBaseQuest());
            }
        }
        return selectedQuests;
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void setViewModels(List<PickQuestViewModel> viewModels) {
        this.viewModels = viewModels;
        notifyDataSetChanged();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.quest_check)
        CheckBox check;

        @BindView(R.id.quest_text)
        TextView name;

        @BindView(R.id.quest_category_indicator_background)
        View categoryIndicatorBackground;

        @BindView(R.id.quest_category_indicator_image)
        ImageView categoryIndicatorImage;

        @BindView(R.id.quest_repeating_indicator)
        ImageView repeatingIndicator;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
