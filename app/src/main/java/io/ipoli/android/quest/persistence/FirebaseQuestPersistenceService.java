package io.ipoli.android.quest.persistence;

import android.content.Context;
import android.support.v4.util.Pair;

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
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import rx.Observable;

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
                q -> q.getEndDate() == null && q.getActualStart() == null && q.getCompletedAt() == null
        ));
    }

    @Override
    public void listenForPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("endDate/time").startAt(toStartOfDayUTC(startDate).getTime()).endAt(toStartOfDayUTC(endDate).getTime());
        listenForListChange(query, listener, data -> data.filter(q -> q.getCompletedAt() == null));
    }

    @Override
    public void findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("completedAt/time").startAt(toStartOfDay(startDate).getTime()).endAt(toStartOfDay(endDate).getTime());
        listenForSingleListChange(query, listener);
    }

    @Override
    public void findAllPlannedAndStartedToday(OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("endDate/time").equalTo(toStartOfDayUTC(LocalDate.now()).getTime());
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getCompletedAt() == null));
    }

    @Override
    public void findAllIncompleteToDosBefore(LocalDate date, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("endDate/time").endAt(toStartOfDayUTC(date).getTime());
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getRepeatingQuest() == null && q.getCompletedAt() == null));
    }

    @Override
    public void findCompletedWithStartTimeForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuestId").equalTo(repeatingQuestId);
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getActualStart() != null && q.getCompletedAt() != null));
    }

    @Override
    public void countCompletedForRepeatingQuest(String repeatingQuestId, LocalDate fromDate, LocalDate toDate, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuestId").equalTo(repeatingQuestId);
        listenForCountChange(query, listener, data -> data.filter(quest -> quest.getCompletedAt() != null
                        && quest.getCompletedAt().getTime() >= toStartOfDayUTC(fromDate).getTime()
                        && quest.getCompletedAt().getTime() <= toStartOfDayUTC(toDate).getTime()
                )
        );
    }

    @Override
    public void countCompletedForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuestId").equalTo(repeatingQuestId);
        listenForCountChange(query, listener);
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
    public void findAllForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuestId").equalTo(repeatingQuestId);
        listenForSingleListChange(query, listener);
    }

    @Override
    public void countAllForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuestId").equalTo(repeatingQuest.getId());
        listenForSingleCountChange(query, listener, data -> data
                .filter(q -> isBetweenDatesFilter(q.getOriginalStartDate(), startDate, endDate)));
    }

    @Override
    public List<Quest> findAllNonAllDayIncompleteForDate(LocalDate currentDate) {
        return null;
    }

    @Override
    public void findByExternalSourceMappingId(String source, String sourceId, OnDataChangedListener<Quest> listener) {
        Query query = getCollectionReference().orderByChild("sourceMapping/" + source).equalTo(sourceId);
        listenForSingleModelChange(query, listener);
    }

    @Override
    public void findAllUpcomingForRepeatingQuest(LocalDate startDate, String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuestId").equalTo(repeatingQuestId);
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getEndDate() == null || q.getEndDate().equals(toStartOfDayUTC(startDate))));
    }

    @Override
    public void countAllCompletedWithPriorityForDate(int priority, LocalDate date, OnDataChangedListener<Long> listener) {
        Query query = getCollectionReference().orderByChild("endDate/time").equalTo(toStartOfDayUTC(date).getTime());
        listenForSingleCountChange(query, listener, data -> data.filter(q -> q.getCompletedAt() != null && q.getPriority() == priority));
    }

    @Override
    public void findAllForChallenge(String challengeId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("challengeId").equalTo(challengeId);
        listenForSingleListChange(query, listener);
    }

    @Override
    public Quest findByReminderId(String reminderId) {
        return null;
    }

    @Override
    public void findAllIncompleteOrMostImportantForDate(LocalDate date, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("endDate/time").equalTo(toStartOfDayUTC(date).getTime());
        listenForListChange(query, listener, data -> data
                        .filter(q -> !q.isAllDay())
                        .filter(q -> q.getCompletedAt() == null || q.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY),
                (q1, q2) -> Integer.compare(q1.getStartMinute(), q2.getStartMinute()));
    }

    @Override
    public void findNextUncompletedQuestEndDate(RepeatingQuest repeatingQuest, OnDataChangedListener<Date> listener) {
        Query query = getCollectionReference().orderByChild("repeatingQuestId")
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
    public void findIncompleteNotRepeatingForChallenge(String challengeId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("challengeId").equalTo(challengeId);
        listenForListChange(query, listener, data -> data.filter(q -> q.getCompletedAt() == null && q.getRepeatingQuest() == null));
    }

    @Override
    public void findIncompleteNotRepeatingNotForChallenge(String searchText, String challengeId, OnDataChangedListener<List<Quest>> listener) {
        listenForListChange(getCollectionReference(), listener, data -> data
                .filter(q -> !challengeId.equals(q.getChallengeId()))
                .filter(q -> q.getCompletedAt() == null)
                .filter(q -> q.getRepeatingQuest() == null)
                .filter(rq -> rq.getName().toLowerCase().contains(searchText.toLowerCase())));
    }

    @Override
    public void findAllCompleted(String challengeId, OnDataChangedListener<List<Quest>> listener) {
        Query query = getCollectionReference().orderByChild("challengeId").equalTo(challengeId);
        listenForSingleListChange(query, listener, data -> data.filter(q -> q.getCompletedAt() != null));
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
                            q -> isBetweenDatesFilter(q.getCompletedAt(), weekPair.first, weekPair.second))
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
        listenForSingleCountChange(query, listener, data -> data.filter(q -> q.getCompletedAt() != null));
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
}
