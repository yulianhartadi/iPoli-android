package io.ipoli.android.quest.persistence;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.QuestReminder;
import io.ipoli.android.reminder.data.Reminder;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/5/17.
 */

public class CouchbaseQuestPersistenceService extends BaseCouchbasePersistenceService<Quest> implements QuestPersistenceService {

    private final View dayQuestsView;
    private final View inboxQuestsView;
    private final View questRemindersView;
    private final View startedQuestsView;
    private final View uncompletedQuestsForRepeatingQuestView;
    private final View completedDayQuestsView;

    public CouchbaseQuestPersistenceService(Database database, ObjectMapper objectMapper, Bus eventBus) {
        super(database, objectMapper, eventBus);

        startedQuestsView = database.getView("quests/started");
        if (startedQuestsView.getMap() == null) {
            startedQuestsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && document.containsKey("actualStart") && !document.containsKey("completedAt")) {
                    emitter.emit(document.get("_id"), document);
                }
            }, Constants.DEFAULT_VIEW_VERSION);
        }

        dayQuestsView = database.getView("quests/byDay");
        if (dayQuestsView.getMap() == null) {
            dayQuestsView.setMapReduce((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && document.containsKey("scheduled")) {
                    emitter.emit(Long.valueOf(document.get("scheduled").toString()), document);
                }
            }, (keys, values, rereduce) -> {
                List<Quest> quests = new ArrayList<>();
                for (Object v : values) {
                    quests.add(toObject(v));
                }
                LocalDate key = new LocalDate((long) keys.get(0));
                return new Pair<>(key, quests);
            }, Constants.DEFAULT_VIEW_VERSION);
        }

        completedDayQuestsView = database.getView("quests/byCompletedDay");
        if (completedDayQuestsView.getMap() == null) {
            completedDayQuestsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && document.containsKey("completedAt")) {
                    emitter.emit(Long.valueOf(document.get("completedAt").toString()), document);
                }
            }, Constants.DEFAULT_VIEW_VERSION);
        }

        inboxQuestsView = database.getView("quests/inbox");
        if (inboxQuestsView.getMap() == null) {
            inboxQuestsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && !document.containsKey("scheduled")) {
                    emitter.emit(document.get("_id"), document);
                }
            }, Constants.DEFAULT_VIEW_VERSION);
        }

        questRemindersView = database.getView("quests/reminders");
        if (questRemindersView.getMap() == null) {
            questRemindersView.setMapReduce((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && document.containsKey("scheduled") && document.containsKey("startMinute") && document.containsKey("reminders")) {
                    List<Map<String, Object>> reminders = (List<Map<String, Object>>) document.get("reminders");
                    if (reminders.size() == 0) {
                        return;
                    }
                    for (Map<String, Object> r : reminders) {
                        QuestReminder qReminder = new QuestReminder((String) document.get("name"),
                                (String) document.get("_id"),
                                Long.valueOf(r.get("minutesFromStart").toString()),
                                Long.valueOf(r.get("start").toString()),
                                Integer.valueOf(r.get("notificationId").toString()),
                                (String) r.get("message"));
                        emitter.emit(Long.valueOf(r.get("start").toString()), qReminder);
                    }
                }
            }, (keys, values, rereduce) -> {
                List<QuestReminder> questReminders = new ArrayList<>();
                for (Object v : values) {
                    questReminders.add(toObject(v, QuestReminder.class));
                }
                Long key = Long.valueOf(keys.get(0).toString());
                return new Pair<>(key, questReminders);
            }, Constants.DEFAULT_VIEW_VERSION);
        }

        uncompletedQuestsForRepeatingQuestView = database.getView("quests/uncompletedForRepeatingQuest");
        if (uncompletedQuestsForRepeatingQuestView.getMap() == null) {
            uncompletedQuestsForRepeatingQuestView.setMapReduce((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && document.containsKey("repeatingQuestId") && !document.containsKey("completedAt")) {
                    emitter.emit(document.get("repeatingQuestId"), document);
                }
            }, (keys, values, rereduce) -> {
                List<Quest> quests = new ArrayList<>();
                for (Object v : values) {
                    quests.add(toObject(v));
                }
                return quests;
            }, Constants.DEFAULT_VIEW_VERSION);
        }
    }

    @Override
    public void save(Quest obj) {
        for (Reminder reminder : obj.getReminders()) {
            reminder.calculateStartTime(obj);
        }
        super.save(obj);
    }

    @Override
    protected Class<Quest> getModelClass() {
        return Quest.class;
    }

    @Override
    public void listenForInboxQuests(OnDataChangedListener<List<Quest>> listener) {
        LiveQuery query = inboxQuestsView.createQuery().toLiveQuery();

        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                postResult(listener, getResult(event));
            }
        };

        startLiveQuery(query, changeListener);
    }

    @Override
    public void listenForPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<SortedMap<LocalDate, List<Quest>>> listener) {
        LiveQuery query = dayQuestsView.createQuery().toLiveQuery();
        query.setGroupLevel(1);
        query.setStartKey(toStartOfDayUTC(startDate).getTime());
        query.setEndKey(toStartOfDayUTC(endDate).getTime());

        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                SortedMap<LocalDate, List<Quest>> result = new TreeMap<>();
                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    QueryRow queryRow = enumerator.next();
                    Pair<LocalDate, List<Quest>> value = (Pair<LocalDate, List<Quest>>) queryRow.getValue();
                    List<Quest> questsForDate = value.second;
                    Collections.sort(questsForDate, createDefaultQuestSortQuery()::sort);
                    result.put(value.first, questsForDate);
                }
                postResult(listener, result);
            }
        };

        startLiveQuery(query, changeListener);
    }

    @Override
    public void findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener) {
        Query query = completedDayQuestsView.createQuery();
        query.setStartKey(toStartOfDayUTC(startDate).getTime());
        query.setEndKey(toStartOfDayUTC(endDate).getTime());
        runQuery(query, listener);
    }

    @Override
    public void findAllPlannedAndStarted(OnDataChangedListener<List<Quest>> listener) {
        runQuery(startedQuestsView, listener);
    }

    @Override
    public void findAllIncompleteFor(LocalDate date, OnDataChangedListener<List<Quest>> listener) {
        Query query = dayQuestsView.createQuery();
        query.setMapOnly(true);
        long key = toStartOfDayUTC(date).getTime();
        query.setStartKey(key);
        query.setEndKey(key);
        runQuery(query, listener, q -> !q.isCompleted());
    }

    @Override
    public void listenForAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        LiveQuery query = dayQuestsView.createQuery().toLiveQuery();
        query.setMapOnly(true);
        long date = toStartOfDayUTC(currentDate).getTime();
        query.setStartKey(date);
        query.setEndKey(date);

        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                postResult(listener, getResult(event, createDefaultQuestSortQuery()));
            }
        };

        startLiveQuery(query, changeListener);
    }

    @Override
    public void findAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        Query query = dayQuestsView.createQuery();
        query.setMapOnly(true);
        long date = toStartOfDayUTC(currentDate).getTime();
        query.setStartKey(date);
        query.setEndKey(date);
        runQuery(query, listener, createDefaultQuestSortQuery());
    }

    @Override
    public void listenForAllNonAllDayCompletedForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        LiveQuery query = dayQuestsView.createQuery().toLiveQuery();
        query.setMapOnly(true);
        long date = toStartOfDayUTC(currentDate).getTime();
        query.setStartKey(date);
        query.setEndKey(date);

        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                postResult(listener, getResult(event, Quest::isCompleted));
            }
        };

        startLiveQuery(query, changeListener);
    }

    @Override
    public void listenForAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {
        LiveQuery query = dayQuestsView.createQuery().toLiveQuery();
        query.setMapOnly(true);
        long date = toStartOfDayUTC(currentDate).getTime();
        query.setStartKey(date);
        query.setEndKey(date);

        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                postResult(listener, getResult(event, q -> !q.isCompleted()));
            }
        };

        startLiveQuery(query, changeListener);
    }

    @Override
    public void findAllUpcomingForRepeatingQuest(LocalDate startDate, String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {
        Query query = uncompletedQuestsForRepeatingQuestView.createQuery();
        query.setGroupLevel(1);
        query.setStartKey(repeatingQuestId);
        query.setEndKey(repeatingQuestId);
        try {
            QueryEnumerator enumerator = query.run();
            List<Quest> result = new ArrayList<>();
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                List<Quest> quests = (List<Quest>) row.getValue();
                for (Quest q : quests) {
                    if (!q.getScheduledDate().before(toStartOfDayUTC(startDate)) || q.getScheduled() == null) {
                        result.add(q);
                    }
                }
            }
            listener.onDataChanged(result);
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
    }

    @Override
    public void countAllCompletedWithPriorityForDate(int priority, LocalDate date, OnDataChangedListener<Long> listener) {
        Query query = dayQuestsView.createQuery();
        long key = toStartOfDayUTC(date).getTime();
        query.setStartKey(key);
        query.setEndKey(key);
        try {
            QueryEnumerator enumerator = null;
            enumerator = query.run();
            long count = 0;
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                Map<String, Object> docProperties = row.getDocumentProperties();
                if (docProperties.containsKey("priority") && Integer.valueOf(docProperties.get("priority").toString()) == priority) {
                    count++;
                }
            }
            listener.onDataChanged(count);
        } catch (CouchbaseLiteException e) {
            postError(e);
            listener.onDataChanged(-1L);
        }
    }

    @Override
    public void findQuestRemindersAtStartTime(long startTime, OnDataChangedListener<List<QuestReminder>> listener) {
        Query query = questRemindersView.createQuery();
        query.setStartKey(startTime);
        query.setEndKey(startTime);
        query.setGroupLevel(1);
        try {
            QueryEnumerator enumerator = query.run();
            if (enumerator.getCount() == 0) {
                listener.onDataChanged(new ArrayList<>());
                return;
            }
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                Pair<Long, List<QuestReminder>> pair = (Pair<Long, List<QuestReminder>>) row.getValue();
                listener.onDataChanged(pair.second);
            }
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
    }

    @Override
    public void findNextReminderTime(OnDataChangedListener<Long> listener) {
        Query query = questRemindersView.createQuery();
        query.setMapOnly(true);
        query.setStartKey(System.currentTimeMillis());
        query.setLimit(1);
        try {
            QueryEnumerator enumerator = query.run();
            if (enumerator.getCount() == 0) {
                listener.onDataChanged(null);
                return;
            }
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                listener.onDataChanged(Long.valueOf(row.getKey().toString()));
            }
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
    }

    @Override
    public void listenForAllIncompleteOrMostImportantForDate(LocalDate date, OnDataChangedListener<List<Quest>> listener) {
        LiveQuery query = dayQuestsView.createQuery().toLiveQuery();
        query.setMapOnly(true);
        long key = toStartOfDayUTC(date).getTime();
        query.setStartKey(key);
        query.setEndKey(key);
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                postResult(listener, getResult(event, q -> !q.isCompleted() || q.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY));
            }
        };

        startLiveQuery(query, changeListener);
    }

    @NonNull
    private QuerySort<Quest> createDefaultQuestSortQuery() {
        return (q1, q2) -> {
            if (q1.shouldBeDoneMultipleTimesPerDay() || q2.shouldBeDoneMultipleTimesPerDay()) {
                return Integer.compare(q1.getTimesADay(), q2.getTimesADay());
            }
            Integer q1Start = q1.getStartMinute();
            if (q1Start == null) {
                return -1;
            }
            Integer q2Start = q2.getStartMinute();
            if (q2Start == null) {
                return 1;
            }
            return Integer.compare(q1Start, q2Start);
        };
    }
}
