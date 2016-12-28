package io.ipoli.android.quest.persistence;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.QuestData;
import io.ipoli.android.quest.data.QuestReminder;
import io.ipoli.android.reminder.data.Reminder;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDay;
import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseQuestPersistenceService extends BaseFirebasePersistenceService<Quest> implements QuestPersistenceService {

    public FirebaseQuestPersistenceService(Bus eventBus, Gson gson) {
        super(eventBus, gson);
    }

    @Override
    protected GenericTypeIndicator<Map<String, Quest>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, Quest>>() {
        };
    }

    @Override
    protected GenericTypeIndicator<List<Quest>> getGenericListIndicator() {
        return new GenericTypeIndicator<List<Quest>>() {
        };
    }

    @Override
    protected Class<Quest> getModelClass() {
        return Quest.class;
    }

    @Override
    protected String getCollectionName() {
        return "quests";
    }

    @Override
    protected DatabaseReference getCollectionReference() {
        return getPlayerReference().child(getCollectionName());
    }

    @Override
    public void listenForInboxQuests(OnDataChangedListener<List<Quest>> listener) {
        listenForListChange(getPlayerReference().child("inboxQuests"), listener);
    }

    @Override
    public void listenForPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("end").startAt(toStartOfDayUTC(startDate).getTime()).endAt(toStartOfDayUTC(endDate).getTime());
        listenForListChange(query, listener, data -> data.filter(q -> q.getCompletedAtDate() == null));
    }

    @Override
    public void findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("completedAt").startAt(toStartOfDay(startDate).getTime()).endAt(toStartOfDay(endDate).getTime());
        listenForSingleListChange(query, listener);
    }

    @Override
    public void findAllPlannedAndStartedToday(OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("end").equalTo(toStartOfDayUTC(LocalDate.now()).getTime());
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getCompletedAtDate() == null));
    }

    @Override
    public void findAllIncompleteToDosBefore(LocalDate date, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("end").endAt(toStartOfDayUTC(date.minusDays(1)).getTime());
        listenForSingleListChange(query, listener, data -> data.filter(q -> !q.isFromRepeatingQuest() && q.getCompletedAtDate() == null && q.getEnd() != null));
    }

    @Override
    public void listenForAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        String dateString = Constants.DAY_QUESTS_DATE_FORMATTER.format(toStartOfDayUTC(currentDate));
        Query query = getPlayerReference().child("dayQuests").child(dateString);
        listenForListChange(query, listener);
    }

    @Override
    public void findAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        Date startDateUTC = toStartOfDayUTC(currentDate);

        Query endAt = getCollectionReference().orderByChild("end").equalTo(startDateUTC.getTime());

        listenForSingleListChange(endAt, endAtQuests -> {

            Date startDate = toStartOfDay(currentDate);
            Date endDate = toStartOfDay(currentDate.plusDays(1));

            Query completedAt = getCollectionReference().orderByChild("completedAt").startAt(startDate.getTime()).endAt(endDate.getTime());

            listenForSingleListChange(completedAt, completedAtQuests -> {
                endAtQuests.addAll(completedAtQuests);
                listener.onDataChanged(endAtQuests);
            });

        }, data -> data.filter(q -> !q.isCompleted()));
    }

    @Override
    public void listenForAllNonAllDayCompletedForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        String dateString = Constants.DAY_QUESTS_DATE_FORMATTER.format(toStartOfDayUTC(currentDate));
        Query query = getPlayerReference().child("dayQuests").child(dateString);
        listenForListChange(query, listener, data -> data.filter(Quest::isCompleted));
    }

    @Override
    public void listenForAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        String dateString = Constants.DAY_QUESTS_DATE_FORMATTER.format(toStartOfDayUTC(currentDate));
        Query query = getPlayerReference().child("dayQuests").child(dateString);
        listenForListChange(query, listener, data -> data.filter(q -> !q.isCompleted()), (q1, q2) -> {
            int q1Start = q1.getStartMinute();
            if (q1Start < 0) {
                return -1;
            }
            int q2Start = q2.getStartMinute();
            if (q2Start < 0) {
                return 1;
            }
            return q1Start - q2Start;
        });
    }

    @Override
    public void findAllNotCompletedForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuestId").equalTo(repeatingQuestId);
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getCompletedAt() == null));
    }

    @Override
    public void findAllUpcomingForRepeatingQuest(LocalDate startDate, String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuestId").equalTo(repeatingQuestId);
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getEndDate() == null || !q.getEndDate().before(toStartOfDayUTC(startDate))));
    }

    @Override
    public void countAllCompletedWithPriorityForDate(int priority, LocalDate date, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("end").equalTo(toStartOfDayUTC(date).getTime());
        listenForSingleCountChange(query, listener, data -> data.filter(q -> q.getCompletedAtDate() != null && q.getPriority() == priority));
    }

    @Override
    public void findNextQuestIdsToRemind(OnDataChangedListener<ReminderStart> listener) {
        Query query = getPlayerReference().child("reminders").orderByKey().startAt(String.valueOf(new Date().getTime())).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    listener.onDataChanged(null);
                    return;
                }
                GenericTypeIndicator<Map<String, Map<String, Boolean>>> indicator = new GenericTypeIndicator<Map<String, Map<String, Boolean>>>() {
                };
                Map<String, Map<String, Boolean>> value = dataSnapshot.getValue(indicator);
                String startTimeKey = value.keySet().iterator().next();
                ReminderStart reminderStart = new ReminderStart(Long.valueOf(startTimeKey), new ArrayList<>(value.get(startTimeKey).keySet()));
                listener.onDataChanged(reminderStart);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void findAllIncompleteOrMostImportantForDate(LocalDate date, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("end").equalTo(toStartOfDayUTC(date).getTime());
        listenForListChange(query, listener, data -> data
                        .filter(q -> !q.isAllDay())
                        .filter(q -> q.getCompletedAtDate() == null || q.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY),
                (q1, q2) -> Integer.compare(q1.getStartMinute(), q2.getStartMinute()));
    }

    @Override
    public void findIncompleteNotRepeatingNotForChallenge(String searchText, String challengeId, OnDataChangedListener<List<Quest>> listener) {
        listenForListChange(getCollectionReference(), listener, data -> data
                .filter(q -> !challengeId.equals(q.getChallengeId()))
                .filter(q -> q.getCompletedAtDate() == null)
                .filter(q -> !q.isFromRepeatingQuest())
                .filter(rq -> rq.getName().toLowerCase().contains(searchText.toLowerCase())));
    }

    @Override
    public void listenForReminderChange(OnChangeListener<Void> onChangeListener) {
        Query query = getPlayerReference().child("reminders");

        ChildEventListener childListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousName) {
                onChangeListener.onNew(null);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousName) {
                onChangeListener.onChanged(null);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                onChangeListener.onDeleted();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        childListeners.put(childListener, query);
        query.addChildEventListener(childListener);
    }

    @Override
    public void deleteRemindersAtTime(long startTime, OnOperationCompletedListener listener) {
        getPlayerReference().child("reminders").child(String.valueOf(startTime)).setValue(null);
        FirebaseCompletionListener.listen(listener);
    }

    @Override
    public void saveNewQuest(Quest quest) {
        Map<String, Object> data = new HashMap<>();
        populateNewQuestData(quest, data);
        getPlayerReference().updateChildren(data);
    }

    @Override
    public void populateNewQuestData(Quest quest, Map<String, Object> data) {
        DatabaseReference questRef = getCollectionReference().push();
        quest.setId(questRef.getKey());
        if (quest.isCompleted()) {
            addDayQuest(quest, data);
        } else if (shouldMoveToInbox(quest)) {
            data.put("/inboxQuests/" + quest.getId(), quest);
        } else {
            quest.setPreviousScheduledDate(quest.getEnd());

            addDayQuest(quest, data);

            if (shouldAddQuestReminders(quest)) {
                addQuestReminders(quest, data);
            }
        }
        if (!StringUtils.isEmpty(quest.getChallengeId())) {
            data.put("/challenges/" + quest.getChallengeId() + "/questsData/" + quest.getId(), new QuestData(quest));
            if (StringUtils.isEmpty(quest.getRepeatingQuestId())) {
                data.put("/challenges/" + quest.getChallengeId() + "/challengeQuests/" + quest.getId(), quest);
            }
        }

        data.put("/quests/" + quest.getId(), quest);
    }

    @Override
    public void populateDeleteQuestData(Quest quest, Map<String, Object> data) {
        data.put("/inboxQuests/" + quest.getId(), null);
        String dateString = Constants.DAY_QUESTS_DATE_FORMATTER.format(quest.getEndDate());
        data.put("/dayQuests/" + dateString + "/" + quest.getId(), null);

        for (Reminder reminder : quest.getReminders()) {
            data.put("/questReminders/" + String.valueOf(reminder.getStart()) + "/" + quest.getId(), null);
        }

        if (quest.isFromChallenge()) {
            data.put("/challenges/" + quest.getChallengeId() + "/questsData/" + quest.getId(), null);
            data.put("/challenges/" + quest.getChallengeId() + "/challengeQuests/" + quest.getId(), null);
        }

        data.put("/quests/" + quest.getId(), null);
    }

    @Override
    public void saveNewQuests(List<Quest> quests) {
        Map<String, Object> data = new HashMap<>();
        for (Quest quest : quests) {
            populateNewQuestData(quest, data);
        }
        getPlayerReference().updateChildren(data);
    }

    @Override
    public void updateNewQuests(List<Quest> quests) {
        Map<String, Object> data = new HashMap<>();
        for (Quest quest : quests) {
            populateUpdateQuest(quest, data);
        }
        getPlayerReference().updateChildren(data);
    }

    private boolean shouldAddQuestReminders(Quest quest) {
        return !quest.isCompleted() && quest.isScheduled() && !quest.getReminders().isEmpty();
    }

    @Override
    public void deleteNewQuest(Quest quest) {
        Map<String, Object> data = new HashMap<>();
        populateDeleteQuestData(quest, data);
        getPlayerReference().updateChildren(data);
    }

    @Override
    public void updateNewQuest(Quest quest) {
        Map<String, Object> data = new HashMap<>();

        populateUpdateQuest(quest, data);
        getPlayerReference().updateChildren(data);
    }

    private void populateUpdateQuest(Quest quest, Map<String, Object> data) {
        Long lastScheduled = quest.getPreviousScheduledDate();

        // remove old day quest
        if (lastScheduled != null) {
            removeOldScheduledDate(quest, data, lastScheduled);
        }

        if (shouldMoveToInbox(quest)) {

            // add inbox
            data.put("/inboxQuests/" + quest.getId(), quest);

        } else {

            // remove inbox
            data.put("/inboxQuests/" + quest.getId(), null);

            addDayQuest(quest, data);
        }

        // remove reminders
        removeOldReminders(quest, data);

        quest.setReminderStartTimes(new ArrayList<>());
        if (shouldAddQuestReminders(quest)) {
            addQuestReminders(quest, data);
        }

        if (quest.getPreviousChallengeId() != null) {
            String challengeId = quest.getPreviousChallengeId();
            data.put("/challenges/" + challengeId + "/questsData/" + quest.getId(), null);
            data.put("/challenges/" + challengeId + "/challengeQuests/" + quest.getId(), null);
        }

        if (quest.getChallengeId() != null) {
            String challengeId = quest.getChallengeId();
            data.put("/challenges/" + challengeId + "/questsData/" + quest.getId(), new QuestData(quest));
            data.put("/challenges/" + challengeId + "/challengeQuests/" + quest.getId(), quest);
        }

        if (quest.isFromRepeatingQuest()) {
            String repeatingQuestId = quest.getRepeatingQuestId();
            data.put("/repeatingQuests/" + repeatingQuestId + "/questsData/" + quest.getId(), new QuestData(quest));
        }

        quest.setPreviousScheduledDate(quest.getEnd());
        data.put("/quests/" + quest.getId(), quest);
    }

    private void addQuestReminders(Quest quest, Map<String, Object> data) {
        for (Reminder reminder : quest.getReminders()) {
            reminder.calculateStartTime(quest);
            quest.addReminderStartTime(reminder.getStart());
            data.put("/questReminders/" + reminder.getStart() + "/" + quest.getId(), new QuestReminder(quest, reminder));
        }
    }

    private void addDayQuest(Quest quest, Map<String, Object> data) {
        String dateString = Constants.DAY_QUESTS_DATE_FORMATTER.format(quest.getEndDate());
        data.put("/dayQuests/" + dateString + "/" + quest.getId(), quest);
    }

    private void removeOldReminders(Quest quest, Map<String, Object> data) {
        for (long startTime : quest.getReminderStartTimes()) {
            data.put("/questReminders/" + String.valueOf(startTime) + "/" + quest.getId(), null);
        }
    }

    private boolean shouldMoveToInbox(Quest quest) {
        return quest.getEndDate() == null;
    }

    private void removeOldScheduledDate(Quest quest, Map<String, Object> data, long lastScheduledDate) {
        String dateString = Constants.DAY_QUESTS_DATE_FORMATTER.format(new Date(lastScheduledDate));
        data.put("/dayQuests/" + dateString + "/" + quest.getId(), null);
    }
}
