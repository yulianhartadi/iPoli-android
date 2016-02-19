package io.ipoli.android.quest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.app.ui.calendar.BaseCalendarAdapter;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.ui.QuestCalendarEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarAdapter extends BaseCalendarAdapter<QuestCalendarEvent> {

    private final List<QuestCalendarEvent> questCalendarEvents;
    private final Bus eventBus;

    public QuestCalendarAdapter(List<QuestCalendarEvent> questCalendarEvents, Bus eventBus) {
        this.questCalendarEvents = questCalendarEvents;
        this.eventBus = eventBus;
    }

    @Override
    public List<QuestCalendarEvent> getEvents() {
        return questCalendarEvents;
    }

    @Override
    public View getView(ViewGroup parent, int position) {
        final QuestCalendarEvent calendarEvent = questCalendarEvents.get(position);
        final Quest q = calendarEvent.getQuest();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_quest_item, parent, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new ShowQuestEvent(q));
            }
        });
        v.setBackgroundResource(calendarEvent.getBackgroundColor());

        TextView name = (TextView) v.findViewById(R.id.quest_name);
        name.setText(q.getName());

        CheckBox check = (CheckBox) v.findViewById(R.id.quest_check);
        if (Quest.getStatus(q) == Status.COMPLETED) {
            check.setChecked(true);
        } else {
            check.setChecked(false);
        }
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    eventBus.post(new CompleteQuestRequestEvent(q));
                }
            }
        });
        return v;
    }

    @Override
    public void onStartTimeUpdated(QuestCalendarEvent calendarEvent, Date oldStartTime) {
        if (canAddEvent(calendarEvent)) {
            eventBus.post(new QuestAddedToCalendarEvent(calendarEvent));
        } else {
            calendarEvent.setStartTime(oldStartTime);
        }
        notifyDataSetChanged();
    }

    public void addEvent(QuestCalendarEvent calendarEvent) {
        questCalendarEvents.add(calendarEvent);
        notifyDataSetChanged();
    }

    public boolean canAddEvent(QuestCalendarEvent calendarEvent) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(calendarEvent.getStartTime());
        int newStartMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int newEndMin = newStartMin + calendarEvent.getDuration();
        for (QuestCalendarEvent e : getEvents()) {
            if (e == calendarEvent) {
                continue;
            }
            Calendar c = Calendar.getInstance();
            c.setTime(e.getStartTime());
            int curStartMin = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
            int curEndMin = curStartMin + e.getDuration();

            if ((newStartMin < curEndMin) && (newEndMin > curStartMin)) {
                return false;
            }
        }
        return true;
    }
}
