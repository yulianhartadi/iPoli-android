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
        quests.add(new Quest("Morning Routine", "Smile and brush", "9:00", 10, R.color.md_green_500, R.color.md_green_700, MaterialDrawableBuilder.IconValue.EMOTICON_HAPPY));
        quests.add(new Quest("Spend some time together", "10:00", 5, R.color.md_orange_500, R.color.md_orange_700, MaterialDrawableBuilder.IconValue.HEART));
        quests.add(new Quest("Workout", "Do some squats", "10:30", 60, R.color.md_red_500, R.color.md_red_700, MaterialDrawableBuilder.IconValue.RUN));
        quests.add(new Quest("Meditate", "11:15", 10, R.color.md_green_500, R.color.md_green_700, MaterialDrawableBuilder.IconValue.EMOTICON_HAPPY));
        quests.add(new Quest("Study Math", "Read Linear Algebra and solve 5 exercises", "12:45", 90, R.color.md_blue_500, R.color.md_blue_700, MaterialDrawableBuilder.IconValue.SCHOOL));
        quests.add(new Quest("Have dinner together", "19:00", 120, R.color.md_orange_500, R.color.md_orange_700, MaterialDrawableBuilder.IconValue.HEART));
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
            public final MaterialIconView icon;

            public ViewHolder(View view) {
                super(view);
                name = (TextView) view.findViewById(R.id.quest_name);
                snippet = (TextView) view.findViewById(R.id.quest_snippet);
                separatorLine = view.findViewById(R.id.separator_line);
                startTime = (TextView) view.findViewById(R.id.quest_start_time);
                duration = (TextView) view.findViewById(R.id.quest_duration);
                icon = (MaterialIconView) view.findViewById(R.id.quest_context_icon);
            }
        }

        public QuestViewAdapter(List<Quest> items) {
            values = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_item_quest, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Quest quest = values.get(position);
            CardView cardView = (CardView) holder.itemView;
            cardView.setCardBackgroundColor(getResources().getColor(quest.backgroundColor));
            holder.separatorLine.setBackgroundColor(getResources().getColor(quest.separatorColor));
            holder.name.setText(quest.name);
            if (TextUtils.isEmpty(quest.snippet)) {
                holder.snippet.setVisibility(View.GONE);
            } else {
                holder.snippet.setVisibility(View.VISIBLE);
                holder.snippet.setText(quest.snippet);
            }
            holder.startTime.setText(quest.time);
            holder.duration.setText(quest.duration + "m");
            holder.icon.setIcon(quest.icon);
        }

        @Override
        public int getItemCount() {
            return values.size();
        }
    }
}
