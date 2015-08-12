package com.curiousily.ipoli.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.NotificationManager;
import com.curiousily.ipoli.QuestDetailActivity;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.models.RecurrentQuest;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.Arrays;
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
        view = (RecyclerView) inflater.inflate(
                R.layout.fragment_daily_schedule, container, false);
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        List<Quest> quests = new ArrayList<>();
        quests.add(new Quest("Morning Routine", "Rise, smile and start a fresh new day", "9:00", 1, Quest.Context.Wellness, Arrays.asList("brushing", "happiness", "smile")));
        quests.add(new Quest("Clear the fridge", "", "9:15", 30, Quest.Context.Home, Arrays.asList("cleaning", "errand")));
        quests.add(new Quest("Call John", "", "10:00", 15, Quest.Context.Personal, Arrays.asList("phone", "work", "errand")));
        quests.add(new RecurrentQuest("Workout", "", "10:30", 60, Quest.Context.Activity));
        quests.add(new Quest("Meditate", "", "11:15", 10, Quest.Context.Wellness, Arrays.asList("zen", "mindfulness", "relax")));
        quests.add(new Quest("Study Math", "", "12:45", 90, Quest.Context.Education, Arrays.asList("book", "linear algebra")));
        quests.add(new Quest("Write Bayesian Learning paper for conference", "", "16:00", 120, Quest.Context.Work, Arrays.asList("university", "paper", "writing")));
        quests.add(new Quest("Watch Breaking Bad", "", "18:00", 45, Quest.Context.Fun, Arrays.asList("movie", "tv")));
        quests.add(new Quest("Have dinner wth friends", "", "19:00", 120, Quest.Context.Personal, Arrays.asList("dinner", "shopping", "cooking")));
        QuestViewAdapter adapter = new QuestViewAdapter(quests);
//        NotificationManager.from(getActivity()).startQuest(quests.get(0));
        view.setAdapter(adapter);
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
    }

    public class QuestViewAdapter
            extends RecyclerView.Adapter<QuestViewAdapter.ViewHolder> {
        private List<Quest> values;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final TextView name;
            public final TextView startTime;
            public final TextView duration;
            public final View recurrenceLayout;
            public final TextView recurrenceText;
            public final MaterialIconView icon;
            public final Button startButton;
            public final TextView tags;

            public ViewHolder(View view) {
                super(view);
                icon = (MaterialIconView) view.findViewById(R.id.quest_context_icon);
                name = (TextView) view.findViewById(R.id.quest_name);
                startTime = (TextView) view.findViewById(R.id.quest_start_time);
                duration = (TextView) view.findViewById(R.id.quest_duration);
                recurrenceLayout = view.findViewById(R.id.quest_recurrence);
                recurrenceText = (TextView) view.findViewById(R.id.quest_recurrence_text);
                startButton = (Button) view.findViewById(R.id.quest_start);
                tags = (TextView) view.findViewById(R.id.quest_tags);
            }
        }

        public QuestViewAdapter(List<Quest> items) {
            values = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_item_daily_quest, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Quest quest = values.get(position);
            CardView cardView = (CardView) holder.itemView;
            cardView.setCardBackgroundColor(getResources().getColor(quest.context.getPrimaryColor()));
            holder.startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), QuestDetailActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });

            if (quest instanceof RecurrentQuest) {
                RecurrentQuest recurrentQuest = (RecurrentQuest) quest;
                holder.recurrenceLayout.setVisibility(View.VISIBLE);
                int remainingTimes = recurrentQuest.weeklyRecurrence.totalTimes - recurrentQuest.weeklyRecurrence.timesCompleted;
                holder.recurrenceText.setText(remainingTimes + " more this week");
            } else {
                holder.recurrenceLayout.setVisibility(View.GONE);
            }

            holder.icon.setIcon(quest.context.getIcon());
            holder.name.setText(quest.name);
            if (quest.tags.isEmpty()) {
                holder.tags.setVisibility(View.GONE);
            } else {
                holder.tags.setVisibility(View.VISIBLE);
            }
            holder.tags.setText(TextUtils.join(", ", quest.tags));
            holder.startTime.setText(quest.time);
            holder.duration.setText(quest.duration + "m");
        }

        @Override
        public int getItemCount() {
            return values.size();
        }
    }
}
