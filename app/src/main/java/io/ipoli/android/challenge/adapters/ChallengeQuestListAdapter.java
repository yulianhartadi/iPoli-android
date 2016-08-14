package io.ipoli.android.challenge.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.challenge.events.RemoveBaseQuestFromChallengeEvent;
import io.ipoli.android.challenge.viewmodels.ChallengeQuestViewModel;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/13/16.
 */
public class ChallengeQuestListAdapter extends RecyclerView.Adapter<ChallengeQuestListAdapter.ViewHolder> {

    private final Context context;
    private List<ChallengeQuestViewModel> viewModels;
    private final Bus eventBus;

    public ChallengeQuestListAdapter(Context context, List<ChallengeQuestViewModel> viewModels, Bus eventBus) {
        this.context = context;
        this.viewModels = viewModels;
        this.eventBus = eventBus;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.challenge_quest_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ChallengeQuestViewModel vm = viewModels.get(position);

        Category category = vm.getCategory();
        GradientDrawable drawable = (GradientDrawable) holder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, category.color500));

        holder.contextIndicatorImage.setImageResource(category.whiteImage);

        holder.name.setText(vm.getName());

        holder.moreMenu.setOnClickListener(v -> {
            eventBus.post(new ItemActionsShownEvent(EventSource.CHALLENGE_QUEST_LIST));
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.challenge_quests_actions_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.delete_challenge_quest:
                        eventBus.post(new RemoveBaseQuestFromChallengeEvent(vm.getBaseQuest()));
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });

        holder.repeatingIndicator.setVisibility(vm.isRepeating() ? View.VISIBLE : View.GONE);
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

        @BindView(R.id.challenge_quest_more_menu)
        ImageButton moreMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
