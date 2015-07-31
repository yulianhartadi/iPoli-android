package com.curiousily.ipoli.ui;

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
import android.widget.TextView;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.models.Quest;
import com.curiousily.ipoli.models.RecurrentQuest;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
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
        quests.add(new Quest("Morning Routine", "Smile and brush", "9:00", 10, Quest.Context.Wellness));
        quests.add(new Quest("Clear the fridge", "9:15", 30, Quest.Context.Home));
        quests.add(new Quest("Call John", "10:00", 15, Quest.Context.Personal));
        quests.add(new RecurrentQuest("Workout", "10:30", 60, Quest.Context.Activity));
        quests.add(new Quest("Meditate", "11:15", 10, Quest.Context.Wellness));
        quests.add(new Quest("Study Math", "Read Linear Algebra and solve 5 exercises", "12:45", 90, Quest.Context.Education));
        quests.add(new Quest("Write Bayesian Learning paper for conference", "16:00", 120, Quest.Context.Work));
        quests.add(new Quest("Watch Breaking Bad", "18:00", 45, Quest.Context.Fun));
        quests.add(new Quest("Have dinner wth friends", "19:00", 120, Quest.Context.Personal));
        QuestViewAdapter adapter = new QuestViewAdapter(quests);
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
            public final TextView snippet;
            public final View separatorLine;
            public final TextView startTime;
            public final TextView duration;
            public final View recurrenceLayout;
            public final TextView recurrenceText;
            public final MaterialIconView icon;

            public ViewHolder(View view) {
                super(view);
                icon = (MaterialIconView) view.findViewById(R.id.quest_context_icon);
                name = (TextView) view.findViewById(R.id.quest_name);
                snippet = (TextView) view.findViewById(R.id.quest_snippet);
                separatorLine = view.findViewById(R.id.separator_line);
                startTime = (TextView) view.findViewById(R.id.quest_start_time);
                duration = (TextView) view.findViewById(R.id.quest_duration);
                recurrenceLayout = view.findViewById(R.id.quest_recurrence);
                recurrenceText = (TextView) view.findViewById(R.id.quest_recurrence_text);

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

            if (quest instanceof RecurrentQuest) {
                RecurrentQuest recurrentQuest = (RecurrentQuest) quest;
                holder.recurrenceLayout.setVisibility(View.VISIBLE);
                int remainingTimes = recurrentQuest.weeklyRecurrence.totalTimes - recurrentQuest.weeklyRecurrence.timesCompleted;
                holder.recurrenceText.setText(remainingTimes + " more this week");
            } else {
                holder.recurrenceLayout.setVisibility(View.GONE);
            }

            switch (quest.context) {
                case Wellness:
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.md_green_500));
                    holder.separatorLine.setBackgroundColor(getResources().getColor(R.color.md_green_700));
                    holder.icon.setIcon(MaterialDrawableBuilder.IconValue.HEART);
                    break;
                case Home:
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.md_pink_500));
                    holder.separatorLine.setBackgroundColor(getResources().getColor(R.color.md_pink_700));
                    holder.icon.setIcon(MaterialDrawableBuilder.IconValue.HOME);
                    break;
                case Work:
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.md_teal_500));
                    holder.separatorLine.setBackgroundColor(getResources().getColor(R.color.md_teal_700));
                    holder.icon.setIcon(MaterialDrawableBuilder.IconValue.BRIEFCASE);
                    break;
                case Personal:
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.md_orange_500));
                    holder.separatorLine.setBackgroundColor(getResources().getColor(R.color.md_orange_700));
                    holder.icon.setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT);
                    break;
                case Fun:
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.md_purple_500));
                    holder.separatorLine.setBackgroundColor(getResources().getColor(R.color.md_purple_700));
                    holder.icon.setIcon(MaterialDrawableBuilder.IconValue.EMOTICON_HAPPY);
                    break;
                case Activity:
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.md_red_500));
                    holder.separatorLine.setBackgroundColor(getResources().getColor(R.color.md_red_700));
                    holder.icon.setIcon(MaterialDrawableBuilder.IconValue.RUN);
                    break;
                case Education:
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.md_blue_500));
                    holder.separatorLine.setBackgroundColor(getResources().getColor(R.color.md_blue_700));
                    holder.icon.setIcon(MaterialDrawableBuilder.IconValue.SCHOOL);
                    break;
            }

            holder.name.setText(quest.name);
            if (TextUtils.isEmpty(quest.snippet)) {
                holder.snippet.setVisibility(View.GONE);
            } else {
                holder.snippet.setVisibility(View.VISIBLE);
                holder.snippet.setText(quest.snippet);
            }
            holder.startTime.setText(quest.time);
            holder.duration.setText(quest.duration + "m");
        }

        @Override
        public int getItemCount() {
            return values.size();
        }
    }
}
