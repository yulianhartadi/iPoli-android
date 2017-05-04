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

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.AndroidCalendarMapping;
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
    private final View scheduledForRepeatingQuest;
    private final View completedDayQuestsView;
    private final View questFromAndroidCalendar;

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
                LocalDate key = DateUtils.fromMillis((long) keys.get(0));
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
                if (Quest.TYPE.equals(type) && !document.containsKey("completedAt")
                        && !document.containsKey("actualStart") && document.containsKey("scheduled")
                        && document.containsKey("startMinute") && document.containsKey("reminders")) {
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

        View oldView = database.getExistingView("quests/uncompletedForRepeatingQuest");
        if (oldView != null) {
            oldView.delete();
        }

        scheduledForRepeatingQuest = database.getView("quests/scheduledForRepeatingQuest");
        if (scheduledForRepeatingQuest.getMap() == null) {
            scheduledForRepeatingQuest.setMapReduce((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && document.containsKey("repeatingQuestId") && document.containsKey("originalScheduled")) {
                    List<Object> key = new ArrayList<>();
                    key.add(document.get("repeatingQuestId"));
                    key.add(document.get("originalScheduled"));
                    emitter.emit(key, document);
                }
            }, (keys, values, rereduce) -> {
                List<Quest> quests = new ArrayList<>();
                for (Object v : values) {
                    quests.add(toObject(v));
                }
                return quests;
            }, Constants.DEFAULT_VIEW_VERSION);
        }

        questFromAndroidCalendar = database.getView("quests/fromAndroidCalendar");
        if (questFromAndroidCalendar.getMap() == null) {
            questFromAndroidCalendar.setMapReduce((document, emitter) -> {
                String type = (String) document.get("type");
                if (Quest.TYPE.equals(type) && document.containsKey("source") &&
                        document.get("source").equals(Constants.SOURCE_ANDROID_CALENDAR)) {
                    Map<String, Object> sourceMapping = (Map<String, Object>) document.get("sourceMapping");
                    Map<String, Object> androidCalendarMapping = (Map<String, Object>) sourceMapping.get("androidCalendarMapping");
                    if(androidCalendarMapping != null) {
                        List<Object> key = new ArrayList<>();
                        key.add(String.valueOf(androidCalendarMapping.get("calendarId")));
                        key.add(String.valueOf(androidCalendarMapping.get("eventId")));
                        emitter.emit(key, document);
                    }
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

        final QuerySort<Quest> inboxQuestsQuerySort = (q1, q2) -> {
            Long q1End = q1.getEnd();
            Long q2End = q2.getEnd();
            if (q1End == null && q2End == null) {
                return -1;
            }
            if (q1End == null) {
                return -1;
            }
            if (q2End == null) {
                return 1;
            }
            return Long.compare(q1End, q2End);
        };

        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                postResult(listener, getResult(event, inboxQuestsQuerySort));
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
    public void findAllIncompleteNotFromRepeatingBefore(LocalDate date, OnDataChangedListener<List<Quest>> listener) {
        Query query = dayQuestsView.createQuery();
        query.setMapOnly(true);
        long key = toStartOfDayUTC(date.minusDays(1)).getTime();
        query.setEndKey(key);
        runQuery(query, listener, q -> !q.isCompleted() && !q.isFromRepeatingQuest());
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
    public void findAllUpcomingForRepeatingQuest(LocalDate scheduledPeriodStart, String repeatingQuestId, OnDataChangedListener<List<Quest>> listener) {
        listener.onDataChanged(findAllUpcomingForRepeatingQuest(scheduledPeriodStart, repeatingQuestId));
    }

    @Override
    public List<Quest> findAllUpcomingForRepeatingQuest(LocalDate scheduledPeriodStart, String repeatingQuestId) {
        Query query = scheduledForRepeatingQuest.createQuery();
        query.setGroupLevel(2);
        query.setStartKey(Arrays.asList(repeatingQuestId, String.valueOf(DateUtils.toMillis(scheduledPeriodStart))));
        query.setEndKey(Arrays.asList(repeatingQuestId, String.valueOf(DateUtils.toMillis(scheduledPeriodStart.plusYears(2)))));

        List<Quest> result = new ArrayList<>();
        try {
            QueryEnumerator enumerator = query.run();
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                List<Quest> quests = (List<Quest>) row.getValue();
                for (Quest q : quests) {
                    result.add(q);
                }
            }
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
        return result;
    }

    @Override
    public List<Quest> findAllForRepeatingQuest(String repeatingQuestId) {
        Query query = scheduledForRepeatingQuest.createQuery();
        query.setGroupLevel(1);
        query.setStartKey(Arrays.asList(repeatingQuestId, null));
        query.setEndKey(Arrays.asList(repeatingQuestId, new HashMap<String, Object>()));

        List<Quest> result = new ArrayList<>();
        try {
            QueryEnumerator enumerator = query.run();
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                List<Quest> quests = (List<Quest>) row.getValue();
                for (Quest q : quests) {
                    result.add(q);
                }
            }
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
        return result;
    }

    @Override
    public void countAllCompletedWithPriorityForDate(int priority, LocalDate date, OnDataChangedListener<Long> listener) {
        Query query = dayQuestsView.createQuery();
        long key = toStartOfDayUTC(date).getTime();
        query.setStartKey(key);
        query.setEndKey(key);
        query.setMapOnly(true);
        try {
            QueryEnumerator enumerator = null;
            enumerator = query.run();
            long count = 0;
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                Map<String, Object> questDoc = (Map<String, Object>) row.getValue();
                if (questDoc.containsKey("priority") &&
                        Integer.valueOf(questDoc.get("priority").toString()) == priority && questDoc.containsKey("completedAt")) {
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

    @Override
    public List<Quest> findNotCompletedFromAndroidCalendar(Long calendarId) {
        return doFindFromAndroid(calendarId, false);
    }

    @Override
    public List<Quest> findFromAndroidCalendar(Long calendarId) {
        return doFindFromAndroid(calendarId, true);
    }

    @Override
    public Quest findFromAndroidCalendar(AndroidCalendarMapping androidCalendarMapping) {
        Query query = questFromAndroidCalendar.createQuery();
        query.setGroupLevel(2);
        List<Object> key = Arrays.asList(
                String.valueOf(androidCalendarMapping.getCalendarId()), String.valueOf(androidCalendarMapping.getEventId()));
        query.setStartKey(key);
        query.setEndKey(key);

        try {
            QueryEnumerator enumerator = query.run();
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                List<Quest> quests = (List<Quest>) row.getValue();
                Quest quest = quests.isEmpty() ? null : quests.get(0);
                return quest;
            }
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
        return null;
    }

    private List<Quest> doFindFromAndroid(Long calendarId, boolean includeCompleted) {
        Query query = questFromAndroidCalendar.createQuery();
        query.setGroupLevel(1);
        query.setStartKey(Arrays.asList(String.valueOf(calendarId), null));
        query.setEndKey(Arrays.asList(String.valueOf(calendarId), new HashMap<String, Object>()));

        List<Quest> result = new ArrayList<>();
        try {
            QueryEnumerator enumerator = query.run();
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                List<Quest> quests = (List<Quest>) row.getValue();
                for (Quest q : quests) {
                    if (!includeCompleted && q.isCompleted()) {
                        continue;
                    }
                    result.add(q);
                }
            }
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
        return result;
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
