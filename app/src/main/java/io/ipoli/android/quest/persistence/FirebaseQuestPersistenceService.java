package io.ipoli.android.quest.persistence;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.reminders.data.Reminder;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDay;
import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseQuestPersistenceService extends BaseFirebasePersistenceService<Quest> implements QuestPersistenceService {

    public FirebaseQuestPersistenceService(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    @Override
    protected GenericTypeIndicator<Map<String, Quest>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, Quest>>() {

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
    public void listenForUnplanned(OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void findPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener) {
        DatabaseReference collectionReference = getCollectionReference();
        Query query = collectionReference.orderByChild("completedAt/time").startAt(toStartOfDay(startDate).getTime()).endAt(toStartOfDay(endDate).getTime());
        listenForSingleChange(query, createListListener(listener));
    }

    @Override
    public List<Quest> findAllPlannedAndStartedToday() {
        return null;
    }

    @Override
    public List<Quest> findAllIncompleteToDosBefore(LocalDate localDate) {
        return null;
    }

    @Override
    public List<Quest> findAllCompletedWithStartTime(RepeatingQuest repeatingQuest) {
        return null;
    }

    @Override
    public long countCompleted(RepeatingQuest repeatingQuest, LocalDate fromDate, LocalDate toDate) {
        return 0;
    }

    @Override
    public long countCompleted(RepeatingQuest repeatingQuest) {
        return 0;
    }

    @Override
    public void findAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {

        List<Quest> endDateQuests = new ArrayList<>();
        List<Quest> completedQuests = new ArrayList<>();
        Date startDateUTC = toStartOfDayUTC(currentDate);

        DatabaseReference collectionReference = getCollectionReference();

        Query endAt = collectionReference.orderByChild("endDate/time").equalTo(startDateUTC.getTime());

        listenForQuery(endAt, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                endDateQuests.clear();
                endDateQuests.addAll(getListFromMapSnapshot(dataSnapshot));
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

        Query completedAt = collectionReference.orderByChild("completedAt/time").startAt(startDate.getTime()).endAt(endDate.getTime());

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

    private void listenForQuery(Query query, ValueEventListener valueListener) {
        valueListeners.put(query.getRef(), valueListener);
        query.addValueEventListener(valueListener);
    }

    private void listenForSingleChange(Query query, ValueEventListener valueListener) {
        query.addListenerForSingleValueEvent(valueListener);
    }

    @Override
    public void findAllNonAllDayCompletedForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        Date startDate = toStartOfDay(currentDate);
        Date endDate = toStartOfDay(currentDate.plusDays(1));
        DatabaseReference collectionReference = getCollectionReference();
        Query completedAt = collectionReference.orderByChild("completedAt/time").startAt(startDate.getTime()).endAt(endDate.getTime());
        listenForQuery(completedAt, createListListener(listener));
    }

    @Override
    public void findAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        Date currentDateUtc = toStartOfDayUTC(currentDate);
        DatabaseReference collectionReference = getCollectionReference();
        Query endAt = collectionReference.orderByChild("endDate/time").equalTo(currentDateUtc.getTime());
        listenForQuery(endAt, createListListener(listener));
    }

    @Override
    public List<Quest> findAllForRepeatingQuest(RepeatingQuest repeatingQuest) {
        return null;
    }

    @Override
    public long countAllForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate) {
        return 0;
    }

    @Override
    public List<Quest> findAllNonAllDayIncompleteForDateSync(LocalDate currentDate) {
        return null;
    }

    @Override
    public Quest findByExternalSourceMappingId(String source, String sourceId) {
        return null;
    }

    @Override
    public List<Quest> findAllUpcomingForRepeatingQuest(LocalDate startDate, RepeatingQuest repeatingQuest) {
        return null;
    }

    @Override
    public long countAllCompletedWithPriorityForDate(int priority, LocalDate date) {
        return 0;
    }

    @Override
    public List<Quest> findAllForChallenge(Challenge challenge) {
        return null;
    }

    @Override
    public Quest findByReminderId(String reminderId) {
        return null;
    }

    @Override
    public void findAllIncompleteOrMostImportantForDate(LocalDate now, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void saveReminders(Quest quest, List<Reminder> reminders) {

    }

    @Override
    public void saveSubQuests(Quest quest, List<SubQuest> subQuests) {

    }

    @Override
    public Date findNextUncompletedQuestEndDate(RepeatingQuest repeatingQuest) {
        return null;
    }

    @Override
    public void findNextUncompletedQuestEndDate(Challenge challenge, OnDataChangedListener<Date> listener) {
        Query query = getCollectionReference().orderByChild("challengeId")
                .equalTo(challenge.getId());

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
    public void findIncompleteNotRepeatingForChallenge(Challenge challenge, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public List<Quest> findIncompleteNotRepeatingNotForChallenge(String query, Challenge challenge) {
        return null;
    }

    @Override
    public void findAllCompleted(Challenge challenge, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Quest> quests = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot.hasChild("completedAt")) {
                        continue;
                    }
                    Quest quest = snapshot.getValue(getModelClass());
                    quest.setId(snapshot.getKey());
                    quests.add(quest);
                }
                listener.onDataChanged(quests);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        query.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public long countCompleted(Challenge challenge, LocalDate start, LocalDate end) {
        return 0;
    }

    @Override
    public void countCompleted(Challenge challenge, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot.hasChild("completedAt")) {
                        continue;
                    }
                    count++;
                }
                listener.onDataChanged(count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        query.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void countNotRepeating(Challenge challenge, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.hasChild("repeatingQuest")) {
                        continue;
                    }
                    count++;
                }
                listener.onDataChanged(count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        query.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void countNotDeleted(Challenge challenge, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDataChanged(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        query.addListenerForSingleValueEvent(valueEventListener);
    }
}
