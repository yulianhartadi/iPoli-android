package io.ipoli.android.quest.persistence;

import android.support.v4.util.Pair;

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

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.reminders.data.Reminder;
import rx.Observable;

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
    public void listenForUnplanned(OnDataChangedListener<List<Quest>> listener) {
        listenForListChange(getCollectionReference(), listener, data -> data.filter(
                q -> q.getEndDate() == null && q.getActualStartDate() == null && q.getCompletedAtDate() == null
        ));
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
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getRepeatingQuest() == null && q.getCompletedAtDate() == null));
    }

    @Override
    public void findCompletedWithStartTimeForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().child("repeatingQuest").orderByChild("id").equalTo(repeatingQuestId);
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getActualStartDate() != null && q.getCompletedAtDate() != null));
    }

    @Override
    public void countCompletedForRepeatingQuest(String repeatingQuestId, LocalDate fromDate, LocalDate toDate, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuest/id").equalTo(repeatingQuestId);
        listenForCountChange(query, listener, data -> data.filter(quest -> quest.getCompletedAtDate() != null
                        && quest.getCompletedAtDate().getTime() >= toStartOfDayUTC(fromDate).getTime()
                        && quest.getCompletedAtDate().getTime() <= toStartOfDayUTC(toDate).getTime()
                )
        );
    }

    @Override
    public void countCompletedForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuest/id").equalTo(repeatingQuestId);
        listenForCountChange(query, listener);
    }

    @Override
    public void listenForAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {

        List<Quest> endDateQuests = new ArrayList<>();
        List<Quest> completedQuests = new ArrayList<>();
        Date startDateUTC = toStartOfDayUTC(currentDate);

        DatabaseReference collectionReference = getCollectionReference();

        Query endAt = collectionReference.orderByChild("end").equalTo(startDateUTC.getTime());

        listenForQuery(endAt, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                endDateQuests.clear();
                List<Quest> quests = getListFromMapSnapshot(dataSnapshot);
                for (Quest quest : quests) {
                    if (!Quest.isCompleted(quest)) {
                        endDateQuests.add(quest);
                    }
                }
                List<Quest> result = new ArrayList<>(endDateQuests);
                result.addAll(completedQuests);
                listener.onDataChanged(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Date startDate = toStartOfDay(currentDate);
        Date endDate = toStartOfDay(currentDate.plusDays(1));

        Query completedAt = collectionReference.orderByChild("completedAt").startAt(startDate.getTime()).endAt(endDate.getTime());

        listenForQuery(completedAt, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                completedQuests.clear();
                completedQuests.addAll(getListFromMapSnapshot(dataSnapshot));
                List<Quest> result = new ArrayList<>(completedQuests);
                result.addAll(endDateQuests);
                listener.onDataChanged(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void listenForAllNonAllDayCompletedForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        Date startDate = toStartOfDay(currentDate);
        Date endDate = toStartOfDay(currentDate.plusDays(1));
        DatabaseReference collectionReference = getCollectionReference();
        Query completedAt = collectionReference.orderByChild("completedAt").startAt(startDate.getTime()).endAt(endDate.getTime());
        listenForQuery(completedAt, createListListener(listener));
    }

    @Override
    public void listenForAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("end").equalTo(toStartOfDayUTC(currentDate).getTime());
        listenForListChange(query, listener, data -> data.filter(q -> q.getCompletedAtDate() == null), (q1, q2) -> {
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
        Query query = getCollectionReference().orderByChild("repeatingQuest/id").equalTo(repeatingQuestId);
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getCompletedAt() == null));
    }

    @Override
    public void countAllForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuest/id").equalTo(repeatingQuest.getId());
        listenForSingleCountChange(query, listener, data -> data
                .filter(q -> isBetweenDatesFilter(q.getOriginalStartDate(), startDate, endDate)));
    }

    @Override
    public void findByExternalSourceMappingId(String source, String sourceId, OnDataChangedListener<Quest> listener) {
        Query query = getCollectionReference().orderByChild("sourceMapping/" + source).equalTo(sourceId);
        listenForSingleModelChange(query, listener);
    }

    @Override
    public void findAllUpcomingForRepeatingQuest(LocalDate startDate, String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuest/id").equalTo(repeatingQuestId);
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getEndDate() == null || !q.getEndDate().before(toStartOfDayUTC(startDate))));
    }

    @Override
    public void countAllCompletedWithPriorityForDate(int priority, LocalDate date, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("end").equalTo(toStartOfDayUTC(date).getTime());
        listenForSingleCountChange(query, listener, data -> data.filter(q -> q.getCompletedAtDate() != null && q.getPriority() == priority));
    }

    @Override
    public void findAllForChallenge(String challengeId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("challengeId").equalTo(challengeId);
        listenForSingleListChange(query, listener);
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
    public void findNextUncompletedQuestEndDate(RepeatingQuest repeatingQuest, OnDataChangedListener<Date> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuest/id")
                .equalTo(repeatingQuest.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Quest> quests = getListFromMapSnapshot(dataSnapshot);
                Date startDate = toStartOfDayUTC(LocalDate.now());
                Date nextDate = null;
                for (Quest q : quests) {
                    if (q.getEndDate() == null) {
                        continue;
                    }
                    if (q.getEndDate().before(startDate)) {
                        continue;
                    }
                    if (nextDate == null) {
                        nextDate = q.getEndDate();
                    }
                    if (q.getEndDate().before(nextDate)) {
                        nextDate = q.getEndDate();
                    }
                }
                listener.onDataChanged(nextDate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void findNextUncompletedQuestEndDate(String challengeId, OnDataChangedListener<Date> listener) {
        Query query = getCollectionReference().orderByChild("challengeId")
                .equalTo(challengeId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Quest> quests = getListFromMapSnapshot(dataSnapshot);
                Date startDate = toStartOfDayUTC(LocalDate.now());
                Date nextDate = null;
                for (Quest q : quests) {
                    if (q.getEndDate() == null) {
                        continue;
                    }
                    if (q.getEndDate().before(startDate)) {
                        continue;
                    }
                    if (nextDate == null) {
                        nextDate = q.getEndDate();
                    }
                    if (q.getEndDate().before(nextDate)) {
                        nextDate = q.getEndDate();
                    }
                }
                listener.onDataChanged(nextDate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void listenForIncompleteNotRepeatingForChallenge(String challengeId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("challengeId").equalTo(challengeId);
        listenForListChange(query, listener, data -> data.filter(q -> q.getCompletedAtDate() == null && q.getRepeatingQuest() == null));
    }

    @Override
    public void findIncompleteNotRepeatingNotForChallenge(String searchText, String challengeId, OnDataChangedListener<List<Quest>> listener) {
        listenForListChange(getCollectionReference(), listener, data -> data
                .filter(q -> !challengeId.equals(q.getChallengeId()))
                .filter(q -> q.getCompletedAtDate() == null)
                .filter(q -> q.getRepeatingQuest() == null)
                .filter(rq -> rq.getName().toLowerCase().contains(searchText.toLowerCase())));
    }

    @Override
    public void findAllCompleted(String challengeId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("challengeId").equalTo(challengeId);
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getCompletedAtDate() != null));
    }

    @Override
    public void countCompletedByWeek(String challengeId, int weeks, OnDataChangedListener<List<Long>> listener) {
        Query query = getCollectionReference().orderByChild("challengeId")
                .equalTo(challengeId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Quest> quests = getListFromMapSnapshot(dataSnapshot);
                List<Long> counts = new ArrayList<>();
                List<Pair<LocalDate, LocalDate>> weekPairs = DateUtils.getBoundsForWeeksInThePast(LocalDate.now(), weeks);
                for (int i = 0; i < weeks; i++) {
                    Pair<LocalDate, LocalDate> weekPair = weekPairs.get(i);
                    Integer count = Observable.from(quests).filter(
                            q -> isBetweenDatesFilter(q.getCompletedAtDate(), weekPair.first, weekPair.second))
                            .count().toBlocking().single();
                    counts.add(Long.valueOf(count));
                }
                listener.onDataChanged(counts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isBetweenDatesFilter(Date date, LocalDate start, LocalDate end) {
        return date != null && !date.before(toStartOfDayUTC(start)) && !date.after(toStartOfDayUTC(end));
    }

    @Override
    public void countCompletedForChallenge(String challengeId, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("challengeId").equalTo(challengeId);
        listenForSingleCountChange(query, listener, data -> data.filter(q -> q.getCompletedAtDate() != null));
    }

    @Override
    public void countNotRepeating(String challengeId, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("challengeId").equalTo(challengeId);
        listenForSingleCountChange(query, listener, data -> data.filter(q -> q.getRepeatingQuest() == null));
    }

    @Override
    public void countNotDeleted(String challengeId, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("challengeId").equalTo(challengeId);
        listenForSingleCountChange(query, listener);
    }

    @Override
    public void save(Quest quest, OnOperationCompletedListener listener) {
        boolean shouldCreate = StringUtils.isEmpty(quest.getId());
        List<Long> oldStartTimes = new ArrayList<>();
        if (quest.getReminders() != null) {
            for (Reminder r : quest.getReminders()) {
                if (r.getStartTime() != null) {
                    oldStartTimes.add(r.getStart());
                }
                r.calculateStartTime(quest);
            }
        }
        super.save(quest, listener);
        DatabaseReference remindersRef = getPlayerReference().child("reminders");

        if (shouldCreate) {
            if (quest.getEndDate() == null || quest.getCompletedAt() != null || quest.getStartMinute() < 0) {
                return;
            }
            Map<String, Object> data = new HashMap<>();
            addNewReminders(data, quest);
            remindersRef.updateChildren(data);
        } else {
            Map<String, Object> data = new HashMap<>();
            addRemindersToDelete(quest, oldStartTimes, data);
            addNewRemindersIfNeeded(data, quest);
            remindersRef.updateChildren(data);
        }
    }

    @Override
    public void save(List<Quest> quests) {
        save(quests, null);
    }

    @Override
    public void save(List<Quest> quests, OnOperationCompletedListener listener) {
        List<Boolean> shouldCreate = new ArrayList<>();
        List<List<Long>> allOldStartTimes = new ArrayList<>();
        for (Quest quest : quests) {
            shouldCreate.add(StringUtils.isEmpty(quest.getId()));
            List<Long> oldStartTimes = new ArrayList<>();
            if (quest.getReminders() != null) {
                for (Reminder r : quest.getReminders()) {
                    if (r.getStartTime() != null) {
                        oldStartTimes.add(r.getStart());
                    }
                    r.calculateStartTime(quest);
                }
            }
            allOldStartTimes.add(oldStartTimes);
        }
        super.save(quests, listener);
        DatabaseReference remindersRef = getPlayerReference().child("reminders");
        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);

            if (shouldCreate.get(i)) {
                if (quest.getEndDate() == null || quest.getCompletedAt() != null || quest.getStartMinute() < 0) {
                    continue;
                }
                addNewReminders(data, quest);
            } else {
                addRemindersToDelete(quest, allOldStartTimes.get(i), data);
                addNewRemindersIfNeeded(data, quest);
            }
        }
        remindersRef.updateChildren(data);
    }

    public void saveWithNewReminders(Quest quest, List<Reminder> newReminders, OnOperationCompletedListener listener) {
        List<Long> oldStartTimes = new ArrayList<>();
        if (quest.getReminders() != null) {
            for (Reminder r : quest.getReminders()) {
                oldStartTimes.add(r.getStart());
            }
        }
        quest.setReminders(newReminders);
        if (quest.getReminders() != null) {
            for (Reminder r : quest.getReminders()) {
                r.calculateStartTime(quest);
            }
        }
        super.save(quest, listener);

        Map<String, Object> data = new HashMap<>();
        addRemindersToDelete(quest, oldStartTimes, data);
        addNewRemindersIfNeeded(data, quest);
        DatabaseReference remindersRef = getPlayerReference().child("reminders");
        remindersRef.updateChildren(data);
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
        childListeners.put(query, childListener);
        query.addChildEventListener(childListener);
    }

    @Override
    public void deleteRemindersAtTime(long startTime, OnOperationCompletedListener listener) {
        getPlayerReference().child("reminders").child(String.valueOf(startTime)).setValue(null, (databaseError, databaseReference) -> {
            if (listener != null) {
                listener.onComplete();
            }
        });
    }

    @Override
    public void delete(Quest quest, OnOperationCompletedListener listener) {
        if (quest.getReminders() != null && !quest.getReminders().isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            List<Long> startTimes = new ArrayList<>();
            for (Reminder r : quest.getReminders()) {
                startTimes.add(r.getStart());
            }
            addRemindersToDelete(quest, startTimes, data);
            DatabaseReference remindersRef = getPlayerReference().child("reminders");
            remindersRef.updateChildren(data);
        }
        super.delete(quest, listener);
    }

    @Override
    public void delete(List<Quest> quests, OnOperationCompletedListener listener) {
        Map<String, Object> data = new HashMap<>();
        for (Quest quest : quests) {
            if (quest.getReminders() != null && !quest.getReminders().isEmpty()) {
                List<Long> startTimes = new ArrayList<>();
                for (Reminder r : quest.getReminders()) {
                    if (r.getStartTime() != null) {
                        startTimes.add(r.getStart());
                    }
                }
                addRemindersToDelete(quest, startTimes, data);
            }
        }

        if (!data.isEmpty()) {
            DatabaseReference remindersRef = getPlayerReference().child("reminders");
            remindersRef.updateChildren(data);
        }
        super.delete(quests, listener);
    }

    private void addNewRemindersIfNeeded(Map<String, Object> data, Quest quest) {
        if (quest.getEndDate() != null && quest.getCompletedAt() == null && quest.getStartMinute() >= 0) {
            addNewReminders(data, quest);
        }
    }

    private void addNewReminders(Map<String, Object> data, Quest quest) {
        if (quest.getReminders() == null || quest.getReminders().isEmpty()) {
            return;
        }
        for (Reminder reminder : quest.getReminders()) {
            if (reminder.getStart() == null) {
                continue;
            }
            Map<String, Boolean> d = new HashMap<>();
            d.put(quest.getId(), true);
            data.put(String.valueOf(reminder.getStart()), d);
        }
    }

    private void addRemindersToDelete(Quest quest, List<Long> oldStartTimes, Map<String, Object> data) {
        for (Long startTime : oldStartTimes) {
            if (startTime == null) {
                continue;
            }
            Map<String, Boolean> d = new HashMap<>();
            d.put(quest.getId(), null);
            data.put(String.valueOf(startTime), d);
        }
    }
}
