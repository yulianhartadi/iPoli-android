package io.ipoli.android.quest.adapters;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.ui.calendar.BaseCalendarAdapter;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.events.RescheduleQuestEvent;
import io.ipoli.android.quest.events.ShareQuestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.SuggestionAcceptedEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.ui.events.EditCalendarEventEvent;
import io.ipoli.android.quest.viewmodels.QuestCalendarViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarAdapter extends BaseCalendarAdapter<QuestCalendarViewModel> {

    private static final HashMap<QuestContext, Integer> QUEST_CONTEXT_TO_CHECKBOX_STYLE = new HashMap<QuestContext, Integer>() {{
        put(QuestContext.LEARNING, R.style.LearningCheckbox);
        put(QuestContext.WELLNESS, R.style.WellnessCheckbox);
        put(QuestContext.PERSONAL, R.style.PersonalCheckbox);
        put(QuestContext.WORK, R.style.WorkCheckbox);
        put(QuestContext.FUN, R.style.FunCheckbox);
        put(QuestContext.CHORES, R.style.ChoresCheckbox);
    }};

    private List<QuestCalendarViewModel> questCalendarViewModels;
    private final Bus eventBus;

    public QuestCalendarAdapter(List<QuestCalendarViewModel> questCalendarViewModels, Bus eventBus) {
        this.questCalendarViewModels = questCalendarViewModels;
        this.eventBus = eventBus;
    }

    @Override
    public List<QuestCalendarViewModel> getEvents() {
        return questCalendarViewModels;
    }

    @Override
    public View getView(ViewGroup parent, int position) {
        final QuestCalendarViewModel calendarEvent = questCalendarViewModels.get(position);
        final Quest q = calendarEvent.getQuest();

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (calendarEvent.shouldDisplayAsSuggestion()) {
            return createSuggestion(parent, calendarEvent, inflater);
        }
        if (calendarEvent.shouldDisplayAsIndicator()) {
            return createIndicator(parent, calendarEvent, inflater);
        }
        return createQuest(parent, calendarEvent, q, inflater);
    }

    @NonNull
    private View createSuggestion(ViewGroup parent, final QuestCalendarViewModel calendarEvent, LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.calendar_suggestion_item, parent, false);
        TextView suggestionName = (TextView) v.findViewById(R.id.quest_suggestion_text);
        suggestionName.setText(calendarEvent.getName());
        v.findViewById(R.id.quest_suggestion_next).setOnClickListener(v1 -> eventBus.post(new RescheduleQuestEvent(calendarEvent)));
        v.setOnClickListener(v1 -> eventBus.post(new SuggestionAcceptedEvent(calendarEvent)));
        return v;
    }

    @NonNull
    private View createIndicator(ViewGroup parent, QuestCalendarViewModel calendarEvent, LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.calendar_habit_completed_item, parent, false);
        ImageView indicatorView = (ImageView) v.findViewById(R.id.habit_indicator);
        indicatorView.setImageResource(calendarEvent.getContextImage());
        return v;
    }

    @NonNull
    private View createQuest(ViewGroup parent, QuestCalendarViewModel calendarEvent, Quest q, LayoutInflater inflater) {
        final View v = inflater.inflate(R.layout.calendar_quest_item, parent, false);

        QuestContext ctx = Quest.getContext(q);
        v.findViewById(R.id.quest_background).setBackgroundResource(ctx.resLightColor);
        v.findViewById(R.id.quest_context_indicator).setBackgroundResource(ctx.resLightColor);

        TextView name = (TextView) v.findViewById(R.id.quest_text);
        name.setText(q.getName());

        ViewGroup detailsRoot = (ViewGroup) v.findViewById(R.id.quest_details_container);

        CheckBox checkBox = createCheckBox(q, v.getContext());
        detailsRoot.addView(checkBox, 0);
        v.setOnClickListener(view -> {

            if(Quest.isCompleted(q)) {
                eventBus.post(new EditQuestRequestEvent(q, EventSource.CALENDAR));
            } else {
                eventBus.post(new ShowQuestEvent(q, EventSource.CALENDAR));
            }
        });

        v.setOnLongClickListener(view -> {
            eventBus.post(new EditCalendarEventEvent(view, q));
            return true;
        });

        if (Quest.isCompleted(q)) {
            checkBox.setChecked(true);
        }

        checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                eventBus.post(new CompleteQuestRequestEvent(q, EventSource.CALENDAR_DAY_VIEW));
            } else {
                eventBus.post(new UndoCompletedQuestRequestEvent(q));
            }
        });

        if (q.getDuration() <= Constants.QUEST_CALENDAR_EVENT_MIN_DURATION) {
            adjustQuestDetailsView(v);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.END);
        }

        v.findViewById(R.id.quest_recurrent_indicator).setVisibility(calendarEvent.isRecurrent() ? View.VISIBLE : View.GONE);

        ImageView share = (ImageView) v.findViewById(R.id.quest_share);
        share.setVisibility(Quest.isCompleted(q) ? View.VISIBLE : View.GONE);
        share.setOnClickListener(v1 -> {
            eventBus.post(new ShareQuestEvent(q, EventSource.CALENDAR_DAY_VIEW));
        });

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
        check.setId(R.id.quest_check);
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
    public void onStartTimeUpdated(QuestCalendarViewModel calendarEvent, int oldStartTime) {
        if (canAddEvent(calendarEvent)) {
            eventBus.post(new QuestAddedToCalendarEvent(calendarEvent));
        } else {
            calendarEvent.setStartMinute(oldStartTime);
        }
        notifyDataSetChanged();
    }

    @Override
    public void updateEvents(List<QuestCalendarViewModel> calendarEvents) {
        this.questCalendarViewModels = calendarEvents;
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

    public void addEvent(QuestCalendarViewModel calendarEvent) {
        questCalendarViewModels.add(calendarEvent);
        notifyDataSetChanged();
    }

    public void removeEvent(QuestCalendarViewModel calendarEvent) {
        questCalendarViewModels.remove(calendarEvent);
        notifyDataSetChanged();
    }

    public boolean canAddEvent(QuestCalendarViewModel calendarEvent) {
        int newStartMin = calendarEvent.getStartMinute();
        int newEndMin = newStartMin + calendarEvent.getDuration();
        for (QuestCalendarViewModel e : getEvents()) {
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
