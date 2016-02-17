package io.ipoli.android.quest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.app.ui.calendar.CalendarAdapter;
import io.ipoli.android.quest.ui.QuestCalendarEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarAdapter extends CalendarAdapter<QuestCalendarEvent> {

    private final List<QuestCalendarEvent> questCalendarEvents;

    public QuestCalendarAdapter(List<QuestCalendarEvent> questCalendarEvents) {
        this.questCalendarEvents = questCalendarEvents;
    }

    @Override
    public List<QuestCalendarEvent> getEvents() {
        return questCalendarEvents;
    }

    @Override
    public View getView(ViewGroup parent, int position) {
        QuestCalendarEvent calendarEvent = questCalendarEvents.get(position);
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_quest_item, parent, false);
        v.setBackgroundResource(calendarEvent.getBackgroundColor());
        TextView name = (TextView) v.findViewById(R.id.quest_name);
        name.setText(questCalendarEvents.get(position).getName());
        return v;
    }

    @Override
    public void onStartTimeUpdated(QuestCalendarEvent calendarEvent, Date oldStartTime) {
        if (canAddEvent(calendarEvent)) {
            // save new quest start time
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
