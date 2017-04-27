package io.ipoli.android.quest.adapters;

import android.content.Context;
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

import org.threeten.bp.LocalDate;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private Context context;
    private List<Quest> quests;
    private final Bus eventBus;
    private final LocalDate today;

    public InboxAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
        this.quests = quests;
        this.eventBus = eventBus;
        this.today = LocalDate.now();
    }

    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(position);

        holder.contentLayout.setOnClickListener(view ->
                eventBus.post(new EditQuestRequestEvent(q.getId(), EventSource.INBOX)));

        Category category = q.getCategoryType();
        holder.categoryIndicatorImage.setImageResource(category.colorfulImage);

        holder.name.setText(q.getName());

        if (q.getEnd() != null) {
            LocalDate endDate = q.getEndDate();
            if (endDate.isBefore(today)) {
                long overdueDays = DAYS.between(endDate, today);
                String daysText = overdueDays > 1 ? "days" : "day";
                holder.dueIn.setText("Overdue by " + overdueDays + " " + daysText);
                holder.dueIn.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            } else {
                holder.dueIn.setText("Due " + DateFormatter.formatWithoutYear(endDate, today));
                holder.dueIn.setTextColor(ContextCompat.getColor(context, R.color.md_dark_text_54));
            }
        } else {
            holder.dueIn.setText("No due date");
            holder.dueIn.setTextColor(ContextCompat.getColor(context, R.color.md_dark_text_54));
        }

        holder.moreMenu.setOnClickListener(v -> {
            eventBus.post(new ItemActionsShownEvent(EventSource.INBOX));
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.quest_actions_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.schedule_quest:
                        eventBus.post(new ScheduleQuestForTodayEvent(q, EventSource.INBOX));
                        return true;
                    case R.id.complete_quest:
                        eventBus.post(new CompleteQuestRequestEvent(q, EventSource.INBOX));
                        return true;
                    case R.id.edit_quest:
                        eventBus.post(new EditQuestRequestEvent(q.getId(), EventSource.INBOX));
                        return true;
                    case R.id.delete_quest:
                        eventBus.post(new DeleteQuestRequestEvent(q, EventSource.INBOX));
                        return true;
                }
                return false;
            });
            popupMenu.show();
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

        @BindView(R.id.content_layout)
        View contentLayout;

        @BindView(R.id.quest_text)
        TextView name;

        @BindView(R.id.quest_due_in)
        TextView dueIn;

        @BindView(R.id.quest_category_indicator_image)
        ImageView categoryIndicatorImage;

        @BindView(R.id.quest_more_menu)
        ImageButton moreMenu;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}