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

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private final PrettyTime prettyTime;
    private Context context;
    private List<Quest> quests;
    private final Bus eventBus;

    public InboxAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
        this.quests = quests;
        this.eventBus = eventBus;
        prettyTime = new PrettyTime();
    }

    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new EditQuestRequestEvent(q, "inbox"));
            }
        });

        QuestContext ctx = Quest.getContext(q);
        GradientDrawable drawable = (GradientDrawable) holder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, ctx.resLightColor));

        holder.contextIndicatorImage.setImageResource(ctx.whiteImage);

        holder.name.setText(q.getName());
        holder.createdAt.setText(prettyTime.format(q.getCreatedAt()));
        holder.createdAt.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public List<Quest> getQuests() {
        return quests;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismissed(int position, int direction) {
        Quest q = quests.get(position);
        quests.remove(position);
        notifyItemRemoved(position);
        if (direction == ItemTouchHelper.START) {
            eventBus.post(new DeleteQuestRequestEvent(q, position));
        } else if (direction == ItemTouchHelper.END) {
            eventBus.post(new ScheduleQuestForTodayEvent(q, "inbox"));
        }
    }

    public void addQuest(int position, Quest quest) {
        quests.add(position, quest);
        notifyDataSetChanged();
    }

    public void updateQuests(List<Quest> newQuests) {
        quests.clear();
        quests.addAll(newQuests);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        @Bind(R.id.quest_text)
        TextView name;

        @Bind(R.id.quest_created_at)
        TextView createdAt;


        @Bind(R.id.quest_context_indicator_background)
        public View contextIndicatorBackground;

        @Bind(R.id.quest_context_indicator_image)
        public ImageView contextIndicatorImage;

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
                showRemove();
                hideSchedule();
            } else if (direction == ItemTouchHelper.END) {
                showSchedule();
                hideRemove();
            }
        }

        private void showRemove() {
            changeRemoveVisibility(View.VISIBLE, View.GONE);
        }

        private void showSchedule() {
            changeScheduleVisibility(View.VISIBLE, View.GONE);
        }

        private void hideRemove() {
            changeRemoveVisibility(View.GONE, View.VISIBLE);
        }

        private void hideSchedule() {
            changeScheduleVisibility(View.GONE, View.VISIBLE);
        }

        private void changeRemoveVisibility(int iconVisibility, int durationVisibility) {
            itemView.findViewById(R.id.quest_remove).setVisibility(iconVisibility);
            itemView.findViewById(R.id.quest_created_at).setVisibility(durationVisibility);
        }

        private void changeScheduleVisibility(int iconVisibility, int startTimeVisibility) {
            itemView.findViewById(R.id.quest_schedule_for_today_container).setVisibility(iconVisibility);
            itemView.findViewById(R.id.quest_context_container).setVisibility(startTimeVisibility);
        }

        @Override
        public void onItemSwipeStopped(int direction) {
            if (direction == ItemTouchHelper.START) {
                hideRemove();
            } else if (direction == ItemTouchHelper.END) {
                hideSchedule();
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