package com.curiousily.ipoli.schedule.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.services.events.QuestUpdatedEvent;
import com.curiousily.ipoli.schedule.DailySchedule;
import com.curiousily.ipoli.schedule.events.DailyScheduleLoadedEvent;
import com.curiousily.ipoli.schedule.events.LoadDailyScheduleEvent;
import com.curiousily.ipoli.schedule.events.UpdateDailyScheduleEvent;
import com.curiousily.ipoli.schedule.ui.events.FinishQuestEvent;
import com.curiousily.ipoli.schedule.ui.events.PostponeQuestEvent;
import com.curiousily.ipoli.schedule.ui.events.ShowQuestEvent;
import com.curiousily.ipoli.user.User;
import com.curiousily.ipoli.user.events.LoadUserEvent;
import com.curiousily.ipoli.user.events.UserLoadedEvent;
import com.curiousily.ipoli.utils.ui.ItemTouchCallback;
import com.curiousily.ipoli.utils.ui.ItemTouchHelperAdapter;
import com.curiousily.ipoli.utils.ui.ItemTouchHelperViewHolder;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class DailyScheduleFragment extends Fragment {

    @Bind(R.id.schedule_list)
    RecyclerView scheduleList;

    @Bind(R.id.schedule_loader)
    ProgressWheel loader;

    @Bind(R.id.schedule_empty_layout)
    View emptySchedule;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_daily_schedule, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Subscribe
    public void onUserLoadedEvent(UserLoadedEvent e) {
        loadSchedule();
    }

    @Subscribe
    public void onDailyScheduleLoaded(DailyScheduleLoadedEvent e) {
        loader.setVisibility(View.GONE);
        DailySchedule schedule = e.schedule;
        if (schedule.quests.isEmpty()) {
            displayEmptySchedule();
        } else {
            displaySchedule(schedule);
        }
    }

    private void displayEmptySchedule() {
        emptySchedule.setVisibility(View.VISIBLE);
    }

    private void displaySchedule(DailySchedule schedule) {
        emptySchedule.setVisibility(View.GONE);
        scheduleList.setVisibility(View.VISIBLE);
        scheduleList.setLayoutManager(new LinearLayoutManager(getContext()));
        DailyScheduleViewAdapter adapter = new DailyScheduleViewAdapter(schedule);
        scheduleList.setAdapter(adapter);
        ItemTouchCallback touchCallback = new ItemTouchCallback(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(scheduleList);
    }

    private void post(Object event) {
        EventBus.get().post(event);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.get().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.get().register(this);
        loader.setVisibility(View.VISIBLE);
        scheduleList.setVisibility(View.GONE);
        post(new LoadUserEvent());
    }

    @Subscribe
    public void onQuestUpdated(QuestUpdatedEvent e) {
        loadSchedule();
    }

    private void loadSchedule() {
        loader.setVisibility(View.VISIBLE);
        scheduleList.setVisibility(View.GONE);
        Calendar calendar = Calendar.getInstance();
        Date scheduledFor = calendar.getTime();
        post(new LoadDailyScheduleEvent(scheduledFor, User.getCurrent(getContext()).id));
    }

    public class DailyScheduleViewAdapter
            extends RecyclerView.Adapter<DailyScheduleViewAdapter.ViewHolder> implements ItemTouchHelperAdapter {
        private DailySchedule schedule;

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(schedule.quests, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(schedule.quests, i, i - 1);
                }
            }
            post(new UpdateDailyScheduleEvent(schedule));
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDismiss(int position, int direction) {
            Quest quest = schedule.quests.get(position);
            if (direction == ItemTouchHelper.START) {
                post(new PostponeQuestEvent(quest));
            } else {
                post(new FinishQuestEvent(quest));
            }
            schedule.quests.remove(quest);
            notifyItemRemoved(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            public final TextView name;
            public final TextView duration;
            public final TextView tags;
            public final View iconBackground;
            private final MaterialIconView icon;

            public ViewHolder(View view) {
                super(view);
                iconBackground = view.findViewById(R.id.quest_context_indicator);
                icon = (MaterialIconView) view.findViewById(R.id.quest_icon);
                name = (TextView) view.findViewById(R.id.quest_name);
                duration = (TextView) view.findViewById(R.id.quest_duration);
                tags = (TextView) view.findViewById(R.id.quest_description);
            }

            @Override
            public void onItemSelected() {
                itemView.setBackgroundResource(R.color.md_blue_a100);
            }

            @Override
            public void onItemClear() {
                itemView.setBackgroundColor(0);
            }
        }

        public DailyScheduleViewAdapter(DailySchedule schedule) {
            this.schedule = schedule;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_list_item_daily_quest, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Quest quest = schedule.quests.get(position);
            if (quest.status == Quest.Status.SCHEDULED || quest.status == Quest.Status.RUNNING) {
                holder.itemView.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    post(new ShowQuestEvent(quest));
                }
            });
            GradientDrawable drawable = (GradientDrawable) holder.iconBackground.getBackground();
            drawable.setColor(getResources().getColor(quest.context.getPrimaryColor()));
            holder.icon.setIcon(quest.context.getIcon());
            holder.name.setText(quest.name);
            holder.tags.setText(quest.description);
            holder.duration.setText(String.format("%d min", quest.duration));
        }

        @Override
        public int getItemCount() {
            return schedule.quests.size();
        }
    }
}
