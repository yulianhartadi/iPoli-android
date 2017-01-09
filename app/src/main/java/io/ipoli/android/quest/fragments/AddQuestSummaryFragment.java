package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import io.ipoli.android.quest.ui.dialogs.EditReminderFragment;
import io.ipoli.android.reminder.ReminderMinutesParser;
import io.ipoli.android.reminder.TimeOffsetType;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/17.
 */
public class AddQuestSummaryFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    private Unbinder unbinder;

    @BindView(R.id.add_quest_reminders_container)
    ViewGroup questRemindersContainer;
    private int notificationId;

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
}
