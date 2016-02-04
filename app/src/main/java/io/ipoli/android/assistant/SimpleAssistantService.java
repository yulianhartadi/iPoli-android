package io.ipoli.android.assistant;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.services.Command;
import io.ipoli.android.app.services.CommandParserService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.assistant.events.AssistantReplyEvent;
import io.ipoli.android.assistant.events.AssistantStartActivityEvent;
import io.ipoli.android.assistant.events.HelpEvent;
import io.ipoli.android.assistant.events.NewFeedbackEvent;
import io.ipoli.android.assistant.events.NewTodayQuestEvent;
import io.ipoli.android.assistant.events.PlanTodayEvent;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.assistant.events.ReviewTodayEvent;
import io.ipoli.android.assistant.events.ShowExamplesEvent;
import io.ipoli.android.assistant.events.ShowQuestsEvent;
import io.ipoli.android.assistant.events.UnknownCommandEvent;
import io.ipoli.android.assistant.persistence.AssistantPersistenceService;
import io.ipoli.android.quest.activities.PlanDayActivity;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.activities.QuestListActivity;
import io.ipoli.android.quest.Status;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.persistence.events.QuestsSavedEvent;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class SimpleAssistantService implements AssistantService {

    private final AssistantPersistenceService assistantPersistenceService;
    private final QuestPersistenceService questPersistenceService;
    private final CommandParserService commandParserService;
    private final Bus eventBus;
    private final Context context;
    private Assistant assistant;

    public SimpleAssistantService(AssistantPersistenceService assistantPersistenceService, QuestPersistenceService questPersistenceService, CommandParserService commandParserService, Bus eventBus, Context context) {
        this.assistantPersistenceService = assistantPersistenceService;
        this.questPersistenceService = questPersistenceService;
        this.commandParserService = commandParserService;
        this.eventBus = eventBus;
        this.context = context;
        assistant = assistantPersistenceService.find();
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        Quest quest = new Quest(e.name);
        quest.setDuration(e.duration);
        quest.setStartTime(e.startTime);
        if (e.dueDate != null) {
            quest.setStatus(Status.PLANNED.name());
            quest.setDue(e.dueDate);
        }
        questPersistenceService.save(quest);
        reply(context.getString(R.string.new_quest_reply));
    }

    @Subscribe
    public void onNewTodayQuest(NewTodayQuestEvent e) {
        Quest quest = new Quest(e.name, Status.PLANNED.name(), e.dueDate);
        quest.setDuration(e.duration);
        quest.setStartTime(e.startTime);
        questPersistenceService.save(quest);
        reply(context.getString(R.string.new_today_quest_reply));
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        scheduleNextReminder();
    }

    @Subscribe
    public void onQuestsSaved(QuestsSavedEvent e) {
        scheduleNextReminder();
    }

    @Subscribe
    public void onQuestDeleted(QuestDeletedEvent e) {
        scheduleNextReminder();
    }

    private void scheduleNextReminder() {
        context.sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }

    @Subscribe
    public void onPlanToday(PlanTodayEvent e) {
        if (questPersistenceService.countAllUncompleted() > 0) {
            reply(context.getString(R.string.ok_reply));
            eventBus.post(new AssistantStartActivityEvent(PlanDayActivity.class));
        } else {
            reply(context.getString(R.string.empty_uncomplete_quests_reply));
        }
    }

    @Subscribe
    public void onShowQuests(ShowQuestsEvent e) {
        if (questPersistenceService.countAllPlannedForToday() > 0) {
            reply(context.getString(R.string.ok_reply));
            eventBus.post(new AssistantStartActivityEvent(QuestListActivity.class));
        } else {
            reply(context.getString(R.string.empty_quests_for_today_reply));
        }
    }

    @Subscribe
    public void onRenameAssistant(RenameAssistantEvent e) {
        assistant.setName(e.name);
        assistant = assistantPersistenceService.save(assistant);
        reply(String.format(context.getString(R.string.new_name_reply), e.name));
    }

    @Subscribe
    public void onReviewToday(ReviewTodayEvent e) {
        List<Quest> quests = questPersistenceService.findAllForToday();
        if (quests.isEmpty()) {
            reply(R.string.empty_review_day_reply);
            return;
        }
        List<String> questStatuses = new ArrayList<>();
        String txt = "Your day in review: <br/><br/>";
        for (Quest q : quests) {
            Status s = Status.valueOf(q.getStatus());
            String qs = (s == Status.COMPLETED) ? "[x]" : "[ ]";
            qs += " " + q.getName();
            questStatuses.add(qs);
        }
        txt += TextUtils.join("<br/>", questStatuses);
        reply(txt);
        tryAskingForFeedback();
    }

    private void tryAskingForFeedback() {
        if (assistant.getStateType() == Assistant.State.NORMAL) {
            Random r = new Random();
            int n = r.nextInt(100);
            if (n < Constants.ASK_FOR_FEEDBACK_PROBABILITY) {
                changeState(Assistant.State.FEEDBACK);
            }
        }
    }

    @Subscribe
    public void onShowExamples(ShowExamplesEvent e) {
        String[] exampleQuests = context.getResources().getStringArray(R.array.example_quests);
        String helpText = "Examples for <b>add quest</b> command:<br/><br/>";
        for (String q : exampleQuests) {
            helpText += "* " + q + "<br/>";
        }
        reply(helpText);
    }

    @Subscribe
    public void onHelp(HelpEvent e) {
        List<Command> commands = new ArrayList<>(Arrays.asList(Command.values()));
        commands.remove(Command.UNKNOWN);

        String helpText = "Command me with these:<br/><br/>";
        for (Command cmd : commands) {
            helpText += "* <b>" + cmd.toString()
                    + " (" + context.getString(cmd.getShortCommandText()) + ")" + "</b>"
                    + " - " + context.getString(cmd.getHelpText()) + "<br/>";
        }
        reply(helpText);
    }

    @Subscribe
    public void onUnknownCommand(UnknownCommandEvent e) {
        String[] excuses = context.getResources().getStringArray(R.array.excuses);
        Random r = new Random();
        String excuse = excuses[r.nextInt(excuses.length)];
        reply(excuse);
        reply(R.string.excuse_help_reminder);
    }

    @Override
    public Assistant getAssistant() {
        return assistant;
    }

    @Override
    public void changeAvatar(String newAvatar) {
        assistant.setAvatar(newAvatar);
        assistant = assistantPersistenceService.save(assistant);
    }

    @Override
    public void onPlayerMessage(String text) {
        switch (Assistant.State.valueOf(assistant.getState())) {
            case TUTORIAL_START:
                reply(R.string.tutorial_start_reply);
                changeState(Assistant.State.TUTORIAL_RENAME);
                break;
            case TUTORIAL_RENAME:
                boolean isValidCommand = commandParserService.parse(text, Command.RENAME);
                if (isValidCommand) {
                    changeState(Assistant.State.TUTORIAL_CHANGE_ASSISTANT_AVATAR);
                } else {
                    reply(R.string.tutorial_rename_invalid_command);
                }
                break;
            case TUTORIAL_CHANGE_ASSISTANT_AVATAR:
                reply(R.string.tutorial_change_assistant_reply);
                changeState(Assistant.State.TUTORIAL_CHANGE_PLAYER_AVATAR);
                break;
            case TUTORIAL_CHANGE_PLAYER_AVATAR:
                reply(R.string.tutorial_change_player_reply);
                changeState(Assistant.State.TUTORIAL_ADD_QUEST);
                break;
            case TUTORIAL_ADD_QUEST:
                isValidCommand = commandParserService.parse(text, Command.ADD_QUEST);
                if (isValidCommand) {
                    addPlanTodayTutorialQuests();
                    changeState(Assistant.State.TUTORIAL_PLAN_TODAY);
                } else {
                    reply(R.string.tutorial_add_quest_invalid_command);
                }
                break;
            case TUTORIAL_PLAN_TODAY:
                isValidCommand = commandParserService.parse(text, Command.PLAN_TODAY);
                if (isValidCommand) {
                    changeState(Assistant.State.TUTORIAL_ADD_TODAY_QUEST);
                } else {
                    reply(R.string.tutorial_plan_today_invalid_command);
                }
                break;
            case TUTORIAL_ADD_TODAY_QUEST:
                isValidCommand = commandParserService.parse(text, Command.ADD_TODAY_QUEST);
                if (isValidCommand) {
                    deletePlanTodayTutorialQuests();
                    addShowQuestsTutorialQuests();
                    changeState(Assistant.State.TUTORIAL_SHOW_QUESTS);
                } else {
                    reply(R.string.tutorial_add_today_quest_invalid_command);
                }
                break;
            case TUTORIAL_SHOW_QUESTS:
                isValidCommand = commandParserService.parse(text, Command.SHOW_QUESTS);
                if (isValidCommand) {
                    reply(R.string.tutorial_show_quests_reply);
                    changeState(Assistant.State.TUTORIAL_REVIEW_TODAY);
                } else {
                    reply(R.string.tutorial_show_quests_invalid_command);
                }
                break;
            case TUTORIAL_REVIEW_TODAY:
                isValidCommand = commandParserService.parse(text, Command.REVIEW_TODAY);
                if (isValidCommand) {
                    deleteShowQuestsTutorialQuests();
                    changeState(Assistant.State.TUTORIAL_SHOW_EXAMPLES);
                } else {
                    reply(R.string.tutorial_review_today_invalid_command);
                }
                break;
            case TUTORIAL_SHOW_EXAMPLES:
                isValidCommand = commandParserService.parse(text, Command.SHOW_EXAMPLES);
                if (isValidCommand) {
                    changeState(Assistant.State.TUTORIAL_HELP);
                } else {
                    reply(R.string.tutorial_show_examples_invalid_command);
                }
                break;
            case TUTORIAL_HELP:
                isValidCommand = commandParserService.parse(text, Command.HELP);
                if (isValidCommand) {
                    reply(R.string.tutorial_help_reply);
                    changeState(Assistant.State.NORMAL);
                } else {
                    reply(R.string.tutorial_help_invalid_command);
                }
                break;
            case FEEDBACK:
                reply(R.string.tutorial_feedback_reply);
                eventBus.post(new NewFeedbackEvent(text));
                changeState(Assistant.State.NORMAL);
                break;
            case NORMAL:
                commandParserService.parse(text);
                break;
        }
    }

    private void deleteShowQuestsTutorialQuests() {
        questPersistenceService.deleteByNames(
                context.getString(R.string.tutorial_show_quest_1),
                context.getString(R.string.tutorial_show_quest_2),
                context.getString(R.string.tutorial_show_quest_3)
        );
    }

    private void deletePlanTodayTutorialQuests() {
        questPersistenceService.deleteByNames(
                context.getString(R.string.tutorial_planned_quest_1),
                context.getString(R.string.tutorial_planned_quest_2),
                context.getString(R.string.tutorial_planned_quest_3)
        );
    }

    private void addPlanTodayTutorialQuests() {
        List<Quest> planQuests = new ArrayList<>();
        planQuests.add(new Quest(context.getString(R.string.tutorial_planned_quest_1)));
        planQuests.add(new Quest(context.getString(R.string.tutorial_planned_quest_2)));
        planQuests.add(new Quest(context.getString(R.string.tutorial_planned_quest_3)));
        questPersistenceService.saveAll(planQuests);
    }

    private void addShowQuestsTutorialQuests() {
        List<Quest> showQuests = new ArrayList<>();
        showQuests.add(createFirstTutorialShowQuest());
        showQuests.add(createSecondTutorialShowQuest());
        showQuests.add(createThirdTutorialShowQuest());
        questPersistenceService.saveAll(showQuests);
    }

    @NonNull
    private Quest createFirstTutorialShowQuest() {
        Quest q = new Quest(context.getString(R.string.tutorial_show_quest_1), Status.PLANNED.name(), new Date());
        Calendar st = DateUtils.getTodayAtMidnight();
        st.set(Calendar.HOUR_OF_DAY, 12);
        q.setStartTime(st.getTime());
        return q;
    }

    @NonNull
    private Quest createSecondTutorialShowQuest() {
        Quest q = new Quest(context.getString(R.string.tutorial_show_quest_2), Status.PLANNED.name(), new Date());
        q.setDuration(30);

        Calendar st = DateUtils.getTodayAtMidnight();
        st.set(Calendar.HOUR_OF_DAY, 8);
        st.set(Calendar.MINUTE, 30);
        q.setStartTime(st.getTime());
        return q;
    }

    @NonNull
    private Quest createThirdTutorialShowQuest() {
        Quest q = new Quest(context.getString(R.string.tutorial_show_quest_3), Status.PLANNED.name(), new Date());
        q.setDuration(15);
        Calendar st = DateUtils.getTodayAtMidnight();
        st.set(Calendar.HOUR_OF_DAY, 19);
        st.set(Calendar.MINUTE, 30);
        q.setStartTime(st.getTime());
        return q;
    }

    private void reply(String message) {
        eventBus.post(new AssistantReplyEvent(message));
    }

    private void reply(@StringRes int stringResource) {
        eventBus.post(new AssistantReplyEvent(context.getString(stringResource)));
    }

    private void onStateChanged(Assistant.State newState) {
        switch (newState) {
            case TUTORIAL_RENAME:
                reply(R.string.tutorial_rename_info);
                break;
            case TUTORIAL_CHANGE_ASSISTANT_AVATAR:
                reply(R.string.tutorial_change_assistant_info);
                break;
            case TUTORIAL_CHANGE_PLAYER_AVATAR:
                reply(R.string.tutorial_change_player_info);
                break;
            case TUTORIAL_ADD_QUEST:
                reply(R.string.tutorial_add_quest_info);
                break;
            case TUTORIAL_PLAN_TODAY:
                reply(R.string.tutorial_plan_today_1_info);
                reply(R.string.tutorial_plan_today_2_info);
                break;
            case TUTORIAL_ADD_TODAY_QUEST:
                reply(R.string.tutorial_add_today_quest_info);
                break;
            case TUTORIAL_SHOW_QUESTS:
                reply(R.string.tutorial_show_quests_1_info);
                break;
            case TUTORIAL_REVIEW_TODAY:
                reply(R.string.tutorial_review_today_info);
                break;
            case TUTORIAL_SHOW_EXAMPLES:
                reply(R.string.tutorial_show_examples_info);
                break;
            case TUTORIAL_HELP:
                reply(R.string.tutorial_help_info);
                break;
            case FEEDBACK:
                reply(R.string.tutorial_feedback_info);
                break;
        }
    }

    private void changeState(Assistant.State newState) {
        assistant.setState(newState.name());
        assistant = assistantPersistenceService.save(assistant);
        onStateChanged(newState);
    }

    @Override
    public void start() {
        switch (Assistant.State.valueOf(assistant.getState())) {
            case TUTORIAL_START:
                reply(R.string.tutorial_start_1_info);
                reply(R.string.tutorial_start_2_info);
                reply(R.string.tutorial_start_3_info);
                break;
            default:
                reply(R.string.welcome_back);
        }
    }

}
