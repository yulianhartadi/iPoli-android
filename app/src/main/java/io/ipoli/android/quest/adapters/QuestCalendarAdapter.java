package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.ui.calendar.BaseCalendarAdapter;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.events.RescheduleQuestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.SuggestionAcceptedEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.ui.events.EditCalendarEventEvent;
import io.ipoli.android.quest.ui.menus.CalendarQuestPopupMenu;
import io.ipoli.android.quest.viewmodels.QuestCalendarViewModel;

import static io.ipoli.android.R.id.quest_more_menu;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarAdapter extends BaseCalendarAdapter<QuestCalendarViewModel> {

    private static final HashMap<Category, Integer> QUEST_CATEGORY_TO_CHECKBOX_STYLE = new HashMap<Category, Integer>() {{
        put(Category.LEARNING, R.style.LearningCheckbox);
        put(Category.WELLNESS, R.style.WellnessCheckbox);
        put(Category.PERSONAL, R.style.PersonalCheckbox);
        put(Category.WORK, R.style.WorkCheckbox);
        put(Category.FUN, R.style.FunCheckbox);
        put(Category.CHORES, R.style.ChoresCheckbox);
    }};

    private final boolean use24HourFormat;

    private List<QuestCalendarViewModel> questCalendarViewModels;
    private final Bus eventBus;

    public QuestCalendarAdapter(List<QuestCalendarViewModel> questCalendarViewModels, boolean use24HourFormat, Bus eventBus) {
        this.questCalendarViewModels = questCalendarViewModels;
        this.use24HourFormat = use24HourFormat;
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

        if (calendarEvent.shouldDisplayAsProposedSlot()) {
            return createProposedSlot(parent, q, calendarEvent, inflater);
        }
        return createQuest(parent, calendarEvent, q, inflater);
    }

    private View createProposedSlot(ViewGroup parent, Quest quest, QuestCalendarViewModel vm, LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.calendar_proposed_quest_item, parent, false);
        TextView name = (TextView) v.findViewById(R.id.quest_text);
        name.setText(quest.getName());
        v.findViewById(R.id.accept_quest).setOnClickListener(v1 -> eventBus.post(new SuggestionAcceptedEvent(quest, vm.getStartMinute())));
        v.findViewById(R.id.reschedule_quest).setOnClickListener(b -> eventBus.post(new RescheduleQuestEvent(vm)));
        return v;
    }

    @NonNull
    private View createQuest(ViewGroup parent, QuestCalendarViewModel calendarEvent, Quest q, LayoutInflater inflater) {
        final View v = inflater.inflate(R.layout.calendar_quest_item, parent, false);

        Context context = parent.getContext();

        Category category = q.getCategoryType();
        v.findViewById(R.id.quest_background).setBackgroundResource(category.color500);
        v.findViewById(R.id.quest_category_indicator).setBackgroundResource(category.color500);

        TextView name = (TextView) v.findViewById(R.id.quest_text);
        name.setText(q.getName());

        ViewGroup detailsRoot = (ViewGroup) v.findViewById(R.id.quest_details_container);

        CheckBox checkBox = createCheckBox(q, context);
        detailsRoot.addView(checkBox, 0);

        View moreMenu = v.findViewById(quest_more_menu);

        if (!q.isPlaceholder()) {

            v.setOnClickListener(view -> {

                if (!q.isCompleted()) {
                    eventBus.post(new ShowQuestEvent(q, EventSource.CALENDAR));
                } else {
                    Toast.makeText(context, R.string.cannot_edit_completed_quests, Toast.LENGTH_SHORT).show();
                }
            });

            v.setOnLongClickListener(view -> {
                eventBus.post(new EditCalendarEventEvent(view, q));
                return true;
            });

            if (q.isCompleted()) {
                checkBox.setChecked(true);
            }

            checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {
                if (checked) {
                    eventBus.post(new CompleteQuestRequestEvent(q, EventSource.CALENDAR_DAY_VIEW));
                } else {
                    if (q.isScheduledForThePast()) {
                        removeEvent(calendarEvent);
                    }
                    eventBus.post(new UndoCompletedQuestRequestEvent(q));
                }
            });

            moreMenu.setOnClickListener(view -> CalendarQuestPopupMenu.show(view, q, eventBus, EventSource.CALENDAR_DAY_VIEW));

        } else {
            checkBox.setVisibility(View.GONE);
            v.findViewById(R.id.quest_more_menu_container).setVisibility(View.GONE);
        }

        if (q.getActualDuration() <= Constants.CALENDAR_EVENT_MIN_DURATION) {
            adjustQuestDetailsView(v);
        }

        if (q.getActualDuration() <= Constants.CALENDAR_EVENT_MIN_SINGLE_LINE_DURATION) {
            name.setSingleLine(true);
        } else if(q.getActualDuration() <= Constants.CALENDAR_EVENT_MIN_TWO_LINES_DURATION) {
            name.setMaxLines(2);
        }

        v.findViewById(R.id.quest_repeating_indicator).setVisibility(calendarEvent.isRepeating() ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.quest_priority_indicator).setVisibility(calendarEvent.isMostImportant() ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.quest_challenge_indicator).setVisibility(calendarEvent.isForChallenge() ? View.VISIBLE : View.GONE);


        return v;
    }

    @NonNull
    private CheckBox createCheckBox(Quest q, Context context) {
        CheckBox check = new CheckBox(new ContextThemeWrapper(context, QUEST_CATEGORY_TO_CHECKBOX_STYLE.get(q.getCategoryType())));
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
        eventBus.post(new QuestAddedToCalendarEvent(calendarEvent));
        notifyDataSetChanged();
    }

    @Override
    public void updateEvents(List<QuestCalendarViewModel> calendarEvents) {
        this.questCalendarViewModels = calendarEvents;
        notifyDataSetChanged();
    }

    @Override
    public void onDragStarted(View dragView, Time time) {
        View background = dragView.findViewById(R.id.quest_background);
        background.setAlpha(1.0F);
        TextView questText = (TextView) dragView.findViewById(R.id.quest_text);
        questText.setTextColor(ContextCompat.getColor(dragView.getContext(), R.color.md_light_text_87));
        dragView.findViewById(R.id.quest_more_menu_container).setVisibility(View.GONE);
        dragView.findViewById(R.id.quest_check).setVisibility(View.GONE);
        dragView.findViewById(R.id.quest_repeating_indicator).setVisibility(View.GONE);
        TextView currentTimeIndicator = (TextView) dragView.findViewById(R.id.quest_current_time_indicator);
        currentTimeIndicator.setText(time.toString(use24HourFormat));
    }

    @Override
    public void onDragMoved(View dragView, Time time) {
        TextView currentTimeIndicator = (TextView) dragView.findViewById(R.id.quest_current_time_indicator);
        currentTimeIndicator.setText(time.toString(use24HourFormat));
    }

    @Override
    public void onDragEnded(View dragView) {
        TextView currentTimeIndicator = (TextView) dragView.findViewById(R.id.quest_current_time_indicator);
        currentTimeIndicator.setText("");
    }

    @Override
    public void removeEvent(QuestCalendarViewModel calendarEvent) {
        questCalendarViewModels.remove(calendarEvent);
        notifyDataSetChanged();
    }

    public List<QuestCalendarViewModel> getEventsWithProposedSlots() {
        List<QuestCalendarViewModel> result = new ArrayList<>();
        for (QuestCalendarViewModel vm : questCalendarViewModels) {
            if (vm.shouldDisplayAsProposedSlot()) {
                result.add(vm);
            }
        }
        return result;
    }
}
