package io.ipoli.android.quest;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.calendar.BaseCalendarAdapter;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.ui.QuestCalendarEvent;
import io.ipoli.android.quest.ui.events.EditCalendarEventEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarAdapter extends BaseCalendarAdapter<QuestCalendarEvent> {

    private List<QuestCalendarEvent> questCalendarEvents;
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

        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_quest_item, parent, false);

        QuestContext ctx = Quest.getContext(q);
        v.findViewById(R.id.quest_background).setBackgroundResource(ctx.resLightColor);
        v.findViewById(R.id.quest_context_indicator).setBackgroundResource(ctx.resLightColor);

        TextView name = (TextView) v.findViewById(R.id.quest_text);
        name.setText(q.getName());

        final CheckBox check = (CheckBox) v.findViewById(R.id.quest_check);
        final View checkDone = v.findViewById(R.id.quest_check_done);

        if (Quest.isCompleted(q)) {
            check.setVisibility(View.GONE);
            checkDone.setVisibility(View.VISIBLE);
            checkDone.setBackgroundResource(ctx.resLightColor);
            checkDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View _) {
                    eventBus.post(new UndoCompletedQuestRequestEvent(q));
                    enableCompleteQuest(q, v, check, checkDone);
                }
            });
        } else {
            enableCompleteQuest(q, v, check, checkDone);
        }

        if (q.getDuration() <= Constants.QUEST_CALENDAR_EVENT_MIN_DURATION) {
            adjustQuestDetailsView(v);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.END);
        }

        return v;
    }

    private void enableCompleteQuest(final Quest q, View v, CheckBox check, View checkDone) {
        check.setVisibility(View.VISIBLE);
        checkDone.setVisibility(View.GONE);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new ShowQuestEvent(q));
            }
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                eventBus.post(new EditCalendarEventEvent(view));
                return true;
            }
        });

        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    eventBus.post(new CompleteQuestRequestEvent(q));
                }
            }
        });
    }

    private void adjustQuestDetailsView(View v) {
        LinearLayout detailsContainer = (LinearLayout) v.findViewById(R.id.quest_details_container);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) detailsContainer.getLayoutParams();
        params.topMargin = 0;
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        detailsContainer.setLayoutParams(params);
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

    @Override
    public void updateEvents(List<QuestCalendarEvent> calendarEvents) {
        this.questCalendarEvents = calendarEvents;
        notifyDataSetChanged();
    }

    @Override
    public void onDragStarted(View draggedView) {
        View background = draggedView.findViewById(R.id.quest_background);
        background.setAlpha(0.26f);
    }

    @Override
    public void onDragEnded(View draggedView) {
        View background = draggedView.findViewById(R.id.quest_background);
        background.setAlpha(0.12f);
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
