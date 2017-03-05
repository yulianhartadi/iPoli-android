package io.ipoli.android.quest.persistence;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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

    public CouchbaseQuestPersistenceService(Database database, ObjectMapper objectMapper) {
        super(database, objectMapper);

        startedQuestsView = database.getView("quests/started");
        if (startedQuestsView.getMap() == null) {
            startedQuestsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && document.containsKey("actualStart") && !document.containsKey("completedAt")) {
                    emitter.emit(document.get("_id"), document);
                }
            }, "1.0");
        }

        dayQuestsView = database.getView("quests/byDay");
        if (dayQuestsView.getMap() == null) {
            dayQuestsView.setMapReduce((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && document.containsKey("scheduled")) {
                    emitter.emit(document.get("scheduled"), document);
                }
            }, (keys, values, rereduce) -> {
                List<Quest> quests = new ArrayList<>();
                for (Object v : values) {
                    quests.add(toObject(v));
                }
                LocalDate key = new LocalDate((long) keys.get(0));
                return new Pair<>(key, quests);
            }, "1.0");
        }

        inboxQuestsView = database.getView("quests/inbox");
        if (inboxQuestsView.getMap() == null) {
            inboxQuestsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && !document.containsKey("scheduled")) {
                    emitter.emit(document.get("_id"), document);
                }
            }, "1.0");
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
                                (Integer) r.get("notificationId"),
                                (String) r.get("message"));
                        emitter.emit(r.get("start"), qReminder);
                    }
                }
            }, (keys, values, rereduce) -> {
                List<QuestReminder> questReminders = new ArrayList<>();
                for (Object v : values) {
                    questReminders.add(toObject(v, QuestReminder.class));
                }
                Long key = (Long) keys.get(0);
                return new Pair<>(key, questReminders);
            }, "1.0");
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
    public void listenById(String id, OnDataChangedListener<Quest> listener) {
        Document.ChangeListener changeListener = event -> {
            DocumentChange change = event.getChange();
            if (change.isDeletion()) {
                listener.onDataChanged(null);
            } else {
                listener.onDataChanged(toObject(change.getAddedRevision().getProperties()));
            }
        };
        Document doc = database.getExistingDocument(id);
        doc.addChangeListener(changeListener);
        documentToListener.put(doc, changeListener);
        listener.onDataChanged(toObject(doc.getProperties()));
    }

    @Override
    public void removeDataChangedListener(OnDataChangedListener<?> listener) {

    }

    @Override
    public void listenForInboxQuests(OnDataChangedListener<List<Quest>> listener) {
        LiveQuery query = inboxQuestsView.createQuery().toLiveQuery();

        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                List<Quest> result = new ArrayList<>();
                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    result.add(toObject(enumerator.next().getValue()));
                }
                new Handler(Looper.getMainLooper()).post(() -> listener.onDataChanged(result));
            }
        };

        query.addChangeListener(changeListener);
        query.start();
        queryToListener.put(query, changeListener);
    }

    @Override
    public void listenForPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<SortedMap<LocalDate, List<Quest>>> listener) {
        LiveQuery query = dayQuestsView.createQuery().toLiveQuery();
        query.setGroupLevel(1);
        long start = toStartOfDayUTC(startDate).getTime();
        long end = toStartOfDayUTC(endDate).getTime();
        query.setStartKey(start);
        query.setEndKey(end);

        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                SortedMap<LocalDate, List<Quest>> result = new TreeMap<>();
                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    QueryRow queryRow = enumerator.next();
                    Pair<LocalDate, List<Quest>> value = (Pair<LocalDate, List<Quest>>) queryRow.getValue();
                    result.put(value.first, value.second);
                }
                new Handler(Looper.getMainLooper()).post(() -> listener.onDataChanged(result));

            }
        };

        query.addChangeListener(changeListener);
        query.start();
        queryToListener.put(query, changeListener);
    }

    @Override
    public void findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void findAllPlannedAndStarted(OnDataChangedListener<List<Quest>> listener) {
        Query query = startedQuestsView.createQuery();
        QueryEnumerator enumerator = null;
        List<Quest> result = new ArrayList<>();
        try {
            enumerator = query.run();
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                result.add(toObject(row.getValue()));
            }
            listener.onDataChanged(result);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void findAllIncompleteToDosBefore(LocalDate date, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void listenForAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void findAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void listenForAllNonAllDayCompletedForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void listenForAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void findAllForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void findAllNotCompletedForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void findAllUpcomingForRepeatingQuest(LocalDate startDate, String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void countAllCompletedWithPriorityForDate(int priority, LocalDate date, OnDataChangedListener<Long> listener) {

    }

    @Override
    public void findQuestRemindersAtStartTime(long startTime, OnDataChangedListener<List<QuestReminder>> listener) {
        Query query = questRemindersView.createQuery();
        query.setStartKey(startTime);
        query.setEndKey(startTime);
        query.setGroupLevel(1);
        try {
            QueryEnumerator enumerator = query.run();
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                Pair<Long, List<QuestReminder>> pair = (Pair<Long, List<QuestReminder>>) row.getValue();
                listener.onDataChanged(pair.second);
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
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
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                listener.onDataChanged((Long) row.getKey());
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listenForAllIncompleteOrMostImportantForDate(LocalDate now, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void findIncompleteNotRepeatingNotForChallenge(String query, String challengeId, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void listenForIncompleteNotRepeating(OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void listenForReminderChange(OnChangeListener onChangeListener) {

    }

    @Override
    public void deleteRemindersAtTime(long startTime) {

    }

    @Override
    public void update(Quest quest) {

    }

    @Override
    public void populateNewQuestData(Quest quest, Map<String, Object> data) {

    }

    @Override
    public void populateDeleteQuestData(Quest quest, Map<String, Object> data) {

    }

    @Override
    public void save(List<Quest> quests) {
        database.runInTransaction(() -> {
            for (Quest q : quests) {
                save(q);
            }
            return true;
        });
    }

    @Override
    public void update(List<Quest> quests) {

    }

    @Override
    public void populateDeleteQuestDataFromRepeatingQuest(Quest quest, Map<String, Object> data) {

    }
}
