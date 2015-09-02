package com.curiousily.ipoli.schedule.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.schedule.events.DailyQuestsLoadedEvent;
import com.curiousily.ipoli.schedule.events.LoadDailyQuestsEvent;
import com.curiousily.ipoli.schedule.ui.events.QuestRatedEvent;
import com.curiousily.ipoli.schedule.ui.events.ShowQuestEvent;
import com.curiousily.ipoli.ui.events.StartQuestEvent;
import com.curiousily.ipoli.user.User;
import com.curiousily.ipoli.user.events.LoadUserEvent;
import com.curiousily.ipoli.user.events.UserLoadedEvent;
import com.curiousily.ipoli.utils.ui.ItemTouchCallback;
import com.curiousily.ipoli.utils.ui.ItemTouchHelperAdapter;
import com.curiousily.ipoli.utils.ui.ItemTouchHelperViewHolder;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class DailyScheduleFragment extends Fragment {

    private RecyclerView view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = (RecyclerView) inflater.inflate(
                R.layout.fragment_daily_schedule, container, false);
        return view;
    }

    @Subscribe
    public void onUserLoadedEvent(UserLoadedEvent e) {
        Calendar calendar = Calendar.getInstance();
        Date scheduledFor = calendar.getTime();
        post(new LoadDailyQuestsEvent(scheduledFor, User.getCurrent(getContext()).id));
    }

    @Subscribe
    public void onDailyQuestsLoaded(DailyQuestsLoadedEvent e) {
        view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        QuestViewAdapter adapter = new QuestViewAdapter(e.quests);
        view.setAdapter(adapter);
        ItemTouchCallback touchCallback = new ItemTouchCallback(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(view);
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
        post(new LoadUserEvent());
    }

    public class QuestViewAdapter
            extends RecyclerView.Adapter<QuestViewAdapter.ViewHolder> implements ItemTouchHelperAdapter {
        private List<Quest> quests;

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(quests, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(quests, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDismiss(int position, int direction) {
            if (direction == ItemTouchHelper.START) {
                onRescheduleQuest(quests.get(position));
            } else {
                onQuestDone(quests.get(position));
            }
            quests.remove(position);
            notifyItemRemoved(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            public final TextView name;
            public final TextView duration;
            public final TextView tags;
            public final View iconBackground;
            public final MaterialIconView startButton;
            private final MaterialIconView icon;

            public ViewHolder(View view) {
                super(view);
                iconBackground = view.findViewById(R.id.quest_context_indicator);
                icon = (MaterialIconView) view.findViewById(R.id.quest_icon);
                name = (TextView) view.findViewById(R.id.quest_name);
                duration = (TextView) view.findViewById(R.id.quest_duration);
                startButton = (MaterialIconView) view.findViewById(R.id.quest_start_button);
                tags = (TextView) view.findViewById(R.id.quest_tags);
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

        public QuestViewAdapter(List<Quest> quests) {
            this.quests = quests;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_list_item_daily_quest, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Quest quest = quests.get(position);
            holder.startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    post(new StartQuestEvent(quest));
                    post(new ShowQuestEvent(quest));
                }
            });
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
            holder.tags.setText(TextUtils.join(", ", quest.tags));
            holder.duration.setText(quest.duration + "");
        }

        @Override
        public int getItemCount() {
            return quests.size();
        }
    }

    private void onRescheduleQuest(Quest quest) {

    }

    private void onQuestDone(Quest quest) {
        QuestDoneDialog newFragment = QuestDoneDialog.newInstance();
        newFragment.setQuest(quest);
        newFragment.show(getFragmentManager(), Constants.ALERT_DIALOG_TAG);
    }

    @Subscribe
    public void onQuestRated(QuestRatedEvent e) {

    }
}
