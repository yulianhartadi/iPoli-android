package io.ipoli.android.quest;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.StartTimeFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class QuestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    public static final int HEADER_ITEM_VIEW_TYPE = 0;
    public static final int QUEST_ITEM_VIEW_TYPE = 1;
    private final Context context;

    private List<Object> items;
    private Bus eventBus;

    private int[] headerIndices = new int[3];

    public QuestAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        setItems(quests);
    }

    private void setItems(List<Quest> quests) {
        calculateHeaderIndices(quests);
        items = new ArrayList<>();
        items.addAll(quests);
        if (headerIndices[0] >= 0) {
            items.add(headerIndices[0], R.string.today);
        }
        if (headerIndices[1] >= 0) {
            items.add(headerIndices[1], R.string.tomorrow);
        }
        if (headerIndices[2] >= 0) {
            items.add(headerIndices[2], R.string.upcoming);
        }
    }

    private void calculateHeaderIndices(List<Quest> quests) {
        Calendar c = Calendar.getInstance();
        int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
        int year = c.get(Calendar.YEAR);
        int headerIndex = 0;
        while (isDueFor(quests.get(headerIndex), dayOfYear, year)) {
            headerIndex++;
        }
        if (headerIndex == 0) {
            headerIndices[0] = -1;
        }
        if (headerIndex > quests.size()) {
            headerIndices[1] = -1;
            return;
        }
        headerIndices[1] = headerIndex + 1;

        c.add(Calendar.DAY_OF_YEAR, 1);
        dayOfYear = c.get(Calendar.DAY_OF_YEAR);
        year = c.get(Calendar.YEAR);
        while (headerIndex < quests.size() && isDueFor(quests.get(headerIndex), dayOfYear, year)) {
            headerIndex++;
        }
        if (headerIndex > quests.size()) {
            headerIndices[2] = -1;
            return;
        }
        headerIndices[2] = headerIndex + 1;
    }

    private boolean isDueFor(Quest quest, int dayOfYear, int year) {
        if (quest.getDue() == null) {
            return false;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(quest.getDue());
        return c.get(Calendar.YEAR) == year && c.get(Calendar.DAY_OF_YEAR) == dayOfYear;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Integer) {
            return HEADER_ITEM_VIEW_TYPE;
        }
        return QUEST_ITEM_VIEW_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {

            case HEADER_ITEM_VIEW_TYPE:
                return new HeaderViewHolder(inflater.inflate(R.layout.quest_header_item, parent, false));

            default:
                return new QuestViewHolder(inflater.inflate(R.layout.quest_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == QUEST_ITEM_VIEW_TYPE) {

            QuestViewHolder questHolder = (QuestViewHolder) holder;

            final Quest q = (Quest) items.get(questHolder.getAdapterPosition());

            questHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, QuestActivity.class);
                    i.putExtra(Constants.QUEST_ID_EXTRA_KEY, q.getId());
                    context.startActivity(i);
                }
            });

            questHolder.name.setText(q.getName());

            GradientDrawable drawable = (GradientDrawable) questHolder.indicator.getBackground();
            drawable.setColor(ContextCompat.getColor(context, Quest.getContext(q).resLightColor));

            if (Quest.getStatus(q) == Status.STARTED) {
                Animation blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
                questHolder.indicator.startAnimation(blinkAnimation);
            }

            if (q.getStartTime() == null) {
                questHolder.startTime.setVisibility(View.INVISIBLE);
            } else {
                questHolder.startTime.setVisibility(View.VISIBLE);
                questHolder.startTime.setText(StartTimeFormatter.format(q.getStartTime()));
            }

            if (q.getDuration() <= 0) {
                questHolder.duration.setVisibility(View.INVISIBLE);
            } else {
                questHolder.duration.setVisibility(View.VISIBLE);
                questHolder.duration.setText(DurationFormatter.format(context, q.getDuration()));
            }
        } else if (holder.getItemViewType() == HEADER_ITEM_VIEW_TYPE) {
            TextView header = (TextView) holder.itemView;
            Integer textRes = (Integer) items.get(position);
            header.setText(textRes);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void removeQuest(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismissed(int position, int direction) {
        Quest q = (Quest) items.get(position);
        items.remove(position);
        notifyItemRemoved(position);
        eventBus.post(new CompleteQuestRequestEvent(q));
    }

    public void updateQuests(List<Quest> newQuests) {
        items.clear();
        setItems(newQuests);
        notifyDataSetChanged();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class QuestViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        @Bind(R.id.quest_name)
        public TextView name;

        @Bind(R.id.quest_start_time)
        public TextView startTime;

        @Bind(R.id.quest_duration)
        public TextView duration;

        @Bind(R.id.quest_indicator)
        public View indicator;

        public QuestViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundResource(R.color.md_blue_100);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onItemSwipeStart() {
            itemView.findViewById(R.id.quest_complete_check).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.quest_start_time).setVisibility(View.GONE);
        }

        @Override
        public void onItemSwipeStopped() {
            itemView.findViewById(R.id.quest_complete_check).setVisibility(View.GONE);
            itemView.findViewById(R.id.quest_start_time).setVisibility(View.VISIBLE);
        }
    }
}