package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.formatters.ReminderTimeFormatter;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.adapters.EditQuestSubQuestListAdapter;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.events.ChangeQuestDateRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestNameRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestTimeRequestEvent;
import io.ipoli.android.quest.events.NewQuestDurationPickedEvent;
import io.ipoli.android.quest.events.subquests.NewSubQuestEvent;
import io.ipoli.android.quest.ui.AddSubQuestView;
import io.ipoli.android.quest.ui.dialogs.DurationPickerFragment;
import io.ipoli.android.quest.ui.dialogs.EditReminderFragment;
import io.ipoli.android.reminder.ReminderMinutesParser;
import io.ipoli.android.reminder.TimeOffsetType;
import io.ipoli.android.reminder.data.Reminder;

import static io.ipoli.android.app.events.EventSource.EDIT_QUEST;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/17.
 */
public class AddQuestSummaryFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    private Unbinder unbinder;

    @BindView(R.id.add_quest_summary_date_container)
    ViewGroup dateContainer;

    @BindView(R.id.add_quest_reminders_container)
    ViewGroup questRemindersContainer;

    @BindView(R.id.sub_quests_container)
    ViewGroup subQuestsContainer;

    @BindView(R.id.sub_quests_list)
    RecyclerView subQuestsList;

    @BindView(R.id.add_sub_quest_container)
    AddSubQuestView addSubQuestView;

    @BindView(R.id.add_sub_quest_clear)
    ImageButton clearAddSubQuest;

    private int notificationId;
    private EditQuestSubQuestListAdapter subQuestListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_summary, container, false);
        unbinder = ButterKnife.bind(this, view);

//        if (quest.getReminders() == null || quest.getReminders().isEmpty()) {
        notificationId = new Random().nextInt();
        addReminder(new Reminder(0, notificationId));
        addReminder(new Reminder(10, notificationId));
//        } else {
//            notificationId = quest.getReminders().get(0).getNotificationId();
//            for (Reminder reminder : quest.getReminders()) {
//                addReminder(reminder);
//            }
//        }

        initSubQuestsUI();

        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }


    @OnClick(R.id.add_quest_summary_reminders)
    public void onRemindersClicked(View view) {
        EditReminderFragment f = EditReminderFragment.newInstance(notificationId, (reminder, editMode) -> {
            if (reminder != null) {
                addReminder(reminder);
            }
        });
        f.show(getActivity().getSupportFragmentManager());
    }

    private void addReminder(Reminder reminder) {
        if (reminderWithSameTimeExists(reminder)) {
            return;
        }
        View v = getActivity().getLayoutInflater().inflate(R.layout.add_quest_reminder_item, questRemindersContainer, false);
        populateReminder(reminder, v);
        questRemindersContainer.addView(v);

        v.setOnClickListener(view -> {
            EditReminderFragment f = EditReminderFragment.newInstance((Reminder) v.getTag(), (editedReminder, mode) -> {
                if (editedReminder == null || reminderWithSameTimeExists(editedReminder)) {
                    questRemindersContainer.removeView(v);
                    return;
                }
                populateReminder(editedReminder, v);
            });
            f.show(getActivity().getSupportFragmentManager());
        });
    }

    private boolean reminderWithSameTimeExists(Reminder reminder) {
        for (Reminder r : getReminders()) {
            if (reminder.getMinutesFromStart() == r.getMinutesFromStart()) {
                return true;
            }
        }
        return false;
    }

    private void populateReminder(Reminder reminder, View reminderView) {
        String text = "";
        Pair<Long, TimeOffsetType> parsedResult = ReminderMinutesParser.parseCustomMinutes(Math.abs(reminder.getMinutesFromStart()));
        if (parsedResult != null) {
            text = ReminderTimeFormatter.formatTimeOffset(parsedResult.first, parsedResult.second);
        }
        ((TextView) reminderView.findViewById(R.id.reminder_text)).setText(text);
        reminderView.setTag(reminder);
    }

    private List<Reminder> getReminders() {
        List<Reminder> reminders = new ArrayList<>();
        for (int i = 0; i < questRemindersContainer.getChildCount(); i++) {
            reminders.add((Reminder) questRemindersContainer.getChildAt(i).getTag());
        }
        return reminders;
    }

    private void initSubQuestsUI() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        subQuestsList.setLayoutManager(layoutManager);

        subQuestListAdapter = new EditQuestSubQuestListAdapter(getActivity(), eventBus, new ArrayList<>(), R.layout.add_quest_sub_quest_list_item);
        subQuestsList.setAdapter(subQuestListAdapter);

        addSubQuestView.setSubQuestAddedListener(this::addSubQuest);
        addSubQuestView.setOnClosedListener(() -> addSubQuestView.setVisibility(View.GONE));
    }

    @OnClick(R.id.add_quest_summary_name)
    public void onNameClicked(View v) {
        postEvent(new ChangeQuestNameRequestEvent());
    }

    @OnClick(R.id.add_quest_summary_date_container)
    public void onDateClicked(View v) {
        postEvent(new ChangeQuestDateRequestEvent());
    }

    @OnClick(R.id.add_quest_summary_time_container)
    public void onTimeClicked(View v) {
        postEvent(new ChangeQuestTimeRequestEvent());
    }

    @OnClick(R.id.add_quest_summary_duration_container)
    public void onDurationClicked(View v) {
        DurationPickerFragment fragment = DurationPickerFragment.newInstance(10, duration -> {
            postEvent(new NewQuestDurationPickedEvent(duration));
        });
        fragment.show(getFragmentManager());
    }

    @OnClick(R.id.sub_quests_container)
    public void onAddSubQuestClicked(View v) {
        addSubQuestView.setVisibility(View.VISIBLE);
        KeyboardUtils.showKeyboard(getContext());
        addSubQuestView.setInEditMode();
    }

    private void addSubQuest(String name) {
        if (StringUtils.isEmpty(name)) {
            return;
        }

        SubQuest sq = new SubQuest(name);
        subQuestListAdapter.addSubQuest(sq);
        eventBus.post(new NewSubQuestEvent(sq, EDIT_QUEST));
    }
}
