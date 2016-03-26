package io.ipoli.android.quest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.calendar.BaseCalendarAdapter;
import io.ipoli.android.quest.data.Quest;
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

    private static final HashMap<QuestContext, Integer> QUEST_CONTEXT_TO_CHECKBOX_STYLE = new HashMap<QuestContext, Integer>() {{
        put(QuestContext.LEARNING, R.style.LearningCheckbox);
        put(QuestContext.WELLNESS, R.style.WellnessCheckbox);
        put(QuestContext.PERSONAL, R.style.PersonalCheckbox);
        put(QuestContext.WORK, R.style.WorkCheckbox);
        put(QuestContext.FUN, R.style.FunCheckbox);
        put(QuestContext.CHORES, R.style.ChoresCheckbox);
    }};

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

        ViewGroup detailsRoot = (ViewGroup) v.findViewById(R.id.quest_details_container);

        CheckBox checkBox = createCheckBox(q, v.getContext());
        detailsRoot.addView(checkBox, 0);
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

        if (Quest.isCompleted(q)) {
            checkBox.setChecked(true);
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    eventBus.post(new CompleteQuestRequestEvent(q));
                } else {
                    eventBus.post(new UndoCompletedQuestRequestEvent(q));
                }
            }
        });

        if (q.getDuration() <= Constants.QUEST_CALENDAR_EVENT_MIN_DURATION) {
            adjustQuestDetailsView(v);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.END);
        }

        return v;
    }

    @NonNull
    private CheckBox createCheckBox(Quest q, Context context) {
        CheckBox check = new CheckBox(new ContextThemeWrapper(context, QUEST_CONTEXT_TO_CHECKBOX_STYLE.get(Quest.getContext(q))));
        LinearLayout.LayoutParams checkLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int marginEndDP = 16;
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                marginEndDP,
                context.getResources().getDisplayMetrics()
        );
        check.setScaleX(1.3f);
        check.setScaleY(1.3f);

        checkLP.setMarginEnd(px);
        checkLP.gravity = Gravity.CENTER_VERTICAL;
        check.setLayoutParams(checkLP);
        return check;
    }

    private void adjustQuestDetailsView(View v) {
        LinearLayout detailsContainer = (LinearLayout) v.findViewById(R.id.quest_details_container);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) detailsContainer.getLayoutParams();
        params.topMargin = 0;
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        detailsContainer.setLayoutParams(params);
    }

    @Override
    public void onStartTimeUpdated(QuestCalendarEvent calendarEvent, int oldStartTime) {
        if (canAddEvent(calendarEvent)) {
            eventBus.post(new QuestAddedToCalendarEvent(calendarEvent));
        } else {
            calendarEvent.setStartMinute(oldStartTime);
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
        int newStartMin = calendarEvent.getStartMinute();
        int newEndMin = newStartMin + calendarEvent.getDuration();
        for (QuestCalendarEvent e : getEvents()) {
            if (e == calendarEvent) {
                continue;
            }
            int curStartMin = e.getStartMinute();
            int curEndMin = curStartMin + e.getDuration();

            if ((newStartMin < curEndMin) && (newEndMin > curStartMin)) {
                return false;
            }
        }
        return true;
    }
}
