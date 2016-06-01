package io.ipoli.android.quest.adapters;

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

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private final PrettyTime prettyTime;
    private Context context;
    private List<Quest> quests;
    private final Bus eventBus;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public InboxAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
        this.quests = quests;
        this.eventBus = eventBus;
        prettyTime = new PrettyTime();
        viewBinderHelper.setOpenOnlyOne(true);
    }

    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_item, parent, false));
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(position);
        viewBinderHelper.bind(holder.swipeLayout, q.getId());

        QuestContext ctx = Quest.getContext(q);
        GradientDrawable drawable = (GradientDrawable) holder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, ctx.resLightColor));

        holder.contextIndicatorImage.setImageResource(ctx.whiteImage);

        holder.name.setText(q.getName());
        holder.createdAt.setText(prettyTime.format(q.getCreatedAt()));

        holder.scheduleQuest.setOnClickListener(v ->
                eventBus.post(new ScheduleQuestForTodayEvent(q, EventSource.INBOX)));

        holder.completeQuest.setOnClickListener(v ->
                eventBus.post(new CompleteQuestRequestEvent(q, EventSource.INBOX)));

        holder.editQuest.setOnClickListener(v ->
                eventBus.post(new EditQuestRequestEvent(q, EventSource.INBOX)));

        holder.deleteQuest.setOnClickListener(v -> {
            eventBus.post(new DeleteQuestRequestEvent(q));
        });
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quest_text)
        TextView name;

        @BindView(R.id.quest_created_at)
        TextView createdAt;


        @BindView(R.id.quest_context_indicator_background)
        public View contextIndicatorBackground;

        @BindView(R.id.quest_context_indicator_image)
        public ImageView contextIndicatorImage;

        @BindView(R.id.swipe_layout)
        public SwipeRevealLayout swipeLayout;

        @BindView(R.id.schedule_quest)
        public ImageButton scheduleQuest;

        @BindView(R.id.complete_quest)
        public ImageButton completeQuest;

        @BindView(R.id.edit_quest)
        public ImageButton editQuest;

        @BindView(R.id.delete_quest)
        public ImageButton deleteQuest;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}