package com.curiousily.ipoli.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.QuestDetailActivity;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.events.DailyQuestsLoadedEvent;
import com.curiousily.ipoli.quest.events.LoadDailyQuestsEvent;
import com.curiousily.ipoli.ui.events.StartQuestEvent;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.Collections;
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
        post(new LoadDailyQuestsEvent());
        return view;
    }

    @Subscribe
    public void setupRecyclerView(DailyQuestsLoadedEvent e) {
        view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        view.addItemDecoration(new LineDividerItemDecorator(getActivity()));
        QuestViewAdapter adapter = new QuestViewAdapter(e.quests);
        view.setAdapter(adapter);
        QuestItemTouchCallback touchCallback = new QuestItemTouchCallback(adapter);
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
        public void onItemDismiss(int position) {
            onQuestDone(quests.get(position));
            quests.remove(position);
            notifyItemRemoved(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            public final TextView name;
            public final TextView duration;
            public final View iconBackground;
            public final MaterialIconView startButton;
            public final TextView description;
            private final MaterialIconView icon;

            public ViewHolder(View view) {
                super(view);
                iconBackground = view.findViewById(R.id.quest_context_indicator);
                icon = (MaterialIconView) view.findViewById(R.id.quest_icon);
                name = (TextView) view.findViewById(R.id.quest_name);
                duration = (TextView) view.findViewById(R.id.quest_duration);
                startButton = (MaterialIconView) view.findViewById(R.id.quest_start_button);
                description = (TextView) view.findViewById(R.id.quest_description);
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
                    startQuestDetailsActivity();
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startQuestDetailsActivity();
                }
            });
            holder.iconBackground.setBackgroundResource(quest.context.getPrimaryColor());
            holder.icon.setIcon(quest.context.getIcon());
            holder.name.setText(quest.name);
            holder.startButton.setColorResource(R.color.md_grey_700);
            holder.description.setText(quest.description);
            holder.duration.setText(quest.duration + " m");
        }

        @Override
        public int getItemCount() {
            return quests.size();
        }
    }

    private void startQuestDetailsActivity() {
        Intent intent = new Intent(getActivity(), QuestDetailActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private void onQuestDone(Quest quest) {
        DialogFragment newFragment = QuestDoneDialog.newInstance(quest);
        newFragment.show(getFragmentManager(), Constants.ALERT_DIALOG_TAG);
    }
}
