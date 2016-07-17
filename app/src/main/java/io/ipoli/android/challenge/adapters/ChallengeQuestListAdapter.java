package io.ipoli.android.challenge.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import io.ipoli.android.challenge.events.RemoveBaseQuestFromChallengeEvent;
import io.ipoli.android.challenge.viewmodels.ChallengeQuestViewModel;
import io.ipoli.android.quest.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/13/16.
 */
public class ChallengeQuestListAdapter extends RecyclerView.Adapter<ChallengeQuestListAdapter.ViewHolder> {

    private final Context context;
    private List<ChallengeQuestViewModel> viewModels;
    private final Bus eventBus;

    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public ChallengeQuestListAdapter(Context context, List<ChallengeQuestViewModel> viewModels, Bus eventBus) {
        this.context = context;
        this.viewModels = viewModels;
        this.eventBus = eventBus;
        viewBinderHelper.setOpenOnlyOne(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.challenge_quest_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ChallengeQuestViewModel vm = viewModels.get(position);
        viewBinderHelper.bind(holder.swipeLayout, vm.getBaseQuest().getId());

        Category category = vm.getCategory();
        GradientDrawable drawable = (GradientDrawable) holder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, category.color500));

        holder.contextIndicatorImage.setImageResource(category.whiteImage);

        holder.name.setText(vm.getName());
        holder.swipeLayout.setSwipeListener(new SwipeRevealLayout.SimpleSwipeListener() {
            @Override
            public void onOpened(SwipeRevealLayout view) {
                super.onOpened(view);
                eventBus.post(new ItemActionsShownEvent(EventSource.CHALLENGE_QUEST_LIST));
            }
        });
        holder.repeatingIndicator.setVisibility(vm.isRepeating() ? View.VISIBLE : View.GONE);

        holder.deleteChallengeQuests.setOnClickListener(v -> eventBus.post(new RemoveBaseQuestFromChallengeEvent(vm.getBaseQuest())));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void setViewModels(List<ChallengeQuestViewModel> viewModels) {
        this.viewModels = viewModels;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.challenge_quest_name)
        TextView name;

        @BindView(R.id.challenge_quest_repeating_indicator)
        ImageView repeatingIndicator;

        @BindView(R.id.challenge_quest_category_indicator_background)
        View contextIndicatorBackground;

        @BindView(R.id.challenge_quest_category_indicator_image)
        ImageView contextIndicatorImage;

        @BindView(R.id.swipe_layout)
        SwipeRevealLayout swipeLayout;

        @BindView(R.id.delete_challenge_quest)
        ImageButton deleteChallengeQuests;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
