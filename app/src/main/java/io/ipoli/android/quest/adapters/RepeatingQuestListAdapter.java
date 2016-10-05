package io.ipoli.android.quest.adapters;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
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

        questHolder.schedule.setText(vm.getScheduleText());
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }


    public void add(RepeatingQuestViewModel viewModel) {
        viewModels.add(viewModel);
        notifyItemInserted(viewModels.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quest_name)
        public TextView name;

        @BindView(R.id.quest_category_indicator_background)
        public View contextIndicatorBackground;

        @BindView(R.id.quest_category_indicator_image)
        public ImageView contextIndicatorImage;

        @BindView(R.id.content_layout)
        public RelativeLayout contentLayout;

        @BindView(R.id.quest_schedule)
        public TextView schedule;

        @BindView(R.id.repeating_quest_more_menu)
        public ImageButton moreMenu;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}