package io.ipoli.android.quest;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.ui.formatters.DueDateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.StartTimeFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class PlanDayQuestAdapter extends RecyclerView.Adapter<PlanDayQuestAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM", Locale.getDefault());

    private final PrettyTime prettyTime;
    private Context context;
    private List<Quest> quests;
    private final Bus eventBus;

    public PlanDayQuestAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
        this.quests = quests;
        this.eventBus = eventBus;
        prettyTime = new PrettyTime();
    }

    @Override
    public PlanDayQuestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new EditQuestRequestEvent(q.getId(), q.getName(), holder.getAdapterPosition(), q.getDue()));
            }
        });

        GradientDrawable drawable = (GradientDrawable) holder.indicator.getBackground();
        drawable.setColor(ContextCompat.getColor(context, Quest.getContext(q).resLightColor));

        holder.name.setText(q.getName());
        holder.createdAt.setText(prettyTime.format(q.getCreatedAt()));
        holder.delete.findViewById(R.id.quest_delete).setVisibility(View.GONE);
        holder.createdAt.setVisibility(View.VISIBLE);

        setCheck(holder.check, q);
        setDueDate(holder.dueDate, q);
        setStartTime(holder.startTime, q);
        setDuration(holder.duration, q);

    }

    private void setCheck(CheckBox check, final Quest q) {
        check.setOnCheckedChangeListener(null);

        if (DateUtils.isToday(q.getDue())) {
            check.setChecked(true);
        } else {
            check.setChecked(false);
        }

        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    q.setStatus(Status.PLANNED.name());
                    q.setDue(new Date());
                } else {
                    q.setStatus(Status.UNPLANNED.name());
                    q.setDue(null);
                }
            }
        });
    }

    private void setDueDate(TextView due, Quest q) {
        if (q.getDue() == null) {
            due.setVisibility(View.GONE);
        } else {
            due.setText(DueDateFormatter.formatWithoutYear(q.getDue()));
            due.setVisibility(View.VISIBLE);
        }
    }

    private void setStartTime(TextView startTime, Quest q) {
        if (q.getStartTime() == null) {
            startTime.setVisibility(View.GONE);
        } else {
            startTime.setText(StartTimeFormatter.format(q.getStartTime()));
            startTime.setVisibility(View.VISIBLE);
        }
    }

    private void setDuration(TextView duration, Quest q) {
        if (q.getDuration() <= 0) {
            duration.setVisibility(View.GONE);
        } else {
            duration.setText(DurationFormatter.format(context, q.getDuration()));
            duration.setVisibility(View.VISIBLE);
        }
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
        eventBus.post(new DeleteQuestRequestEvent(q, position));
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
        @Bind(R.id.quest_check)
        CheckBox check;

        @Bind(R.id.quest_name)
        TextView name;

        @Bind(R.id.quest_created_at)
        TextView createdAt;

        @Bind(R.id.quest_due_date)
        TextView dueDate;

        @Bind(R.id.quest_start_time)
        TextView startTime;

        @Bind(R.id.quest_duration)
        TextView duration;

        @Bind(R.id.quest_delete)
        ImageView delete;

        @Bind(R.id.quest_indicator)
        View indicator;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundResource(R.color.md_grey_100);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onItemSwipeStart() {
            itemView.findViewById(R.id.quest_delete).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.quest_created_at).setVisibility(View.INVISIBLE);
        }

        @Override
        public void onItemSwipeStopped() {
            itemView.findViewById(R.id.quest_delete).setVisibility(View.GONE);
            itemView.findViewById(R.id.quest_created_at).setVisibility(View.VISIBLE);
        }
    }
}