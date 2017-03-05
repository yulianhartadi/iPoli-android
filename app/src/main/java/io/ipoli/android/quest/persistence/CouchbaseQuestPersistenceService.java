package io.ipoli.android.quest.persistence;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.QuestReminder;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/5/17.
 */

public class CouchbaseQuestPersistenceService implements QuestPersistenceService {

    private final Database database;
    private final ObjectMapper objectMapper;
    private final Map<LiveQuery, LiveQuery.ChangeListener> queryToListener;
    private final Map<Document, Document.ChangeListener> documentToListener;
    private final View dayQuestsView;
    private final View inboxQuestsView;

    public CouchbaseQuestPersistenceService(Database database, ObjectMapper objectMapper) {
        this.database = database;
        this.objectMapper = objectMapper;

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
                    quests.add(toObject(v, Quest.class));
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

        queryToListener = new HashMap<>();
        documentToListener = new HashMap<>();
    }

    @Override
    public void save(Quest obj) {
        Map<String, Object> data = new HashMap<>();
        Document document;
        if (StringUtils.isEmpty(obj.getId())) {
            document = database.createDocument();
        } else {
            document = database.getExistingDocument(obj.getId());
            data.putAll(document.getProperties());
            obj.markUpdated();
        }

        TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
        };
        data.putAll(objectMapper.convertValue(obj, mapTypeReference));

        try {
            document.putProperties(data);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void findById(String id, OnDataChangedListener<Quest> listener) {
        listener.onDataChanged(toObject(database.getExistingDocument(id).getProperties(), Quest.class));
    }

    @Override
    public void listenById(String id, OnDataChangedListener<Quest> listener) {
        Document.ChangeListener changeListener = event -> {
            DocumentChange change = event.getChange();
            if (change.isDeletion()) {
                listener.onDataChanged(null);
            } else {
                listener.onDataChanged(toObject(change.getAddedRevision().getProperties(), Quest.class));
            }
        };
        Document doc = database.getExistingDocument(id);
        doc.addChangeListener(changeListener);
        documentToListener.put(doc, changeListener);
        listener.onDataChanged(toObject(doc.getProperties(), Quest.class));
    }

    @Override
    public void delete(Quest object) {
        try {
            database.getExistingDocument(object.getId()).delete();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAllListeners() {
        for (Map.Entry<LiveQuery, LiveQuery.ChangeListener> entry : queryToListener.entrySet()) {
            LiveQuery liveQuery = entry.getKey();
            liveQuery.removeChangeListener(entry.getValue());
            liveQuery.stop();
        }
        for (Map.Entry<Document, Document.ChangeListener> entry : documentToListener.entrySet()) {
            Document doc = entry.getKey();
            doc.removeChangeListener(entry.getValue());
        }
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
                    result.add(toObject(enumerator.next().getValue(), Quest.class));
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

    private <T> T toObject(Object data, Class<T> clazz) {
        return objectMapper.convertValue(data, clazz);
    }

    @Override
    public void findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener) {

    }

    @Override
    public void findAllPlannedAndStarted(OnDataChangedListener<List<Quest>> listener) {

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

    }

    @Override
    public void findNextReminderTime(OnDataChangedListener<Long> listener) {

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

    }

    @Override
    public void update(List<Quest> quests) {

    }

    @Override
    public void listenForDayQuestChange(LocalDate date, OnChangeListener onChangeListener) {

    }

    @Override
    public void populateDeleteQuestDataFromRepeatingQuest(Quest quest, Map<String, Object> data) {

    }
}
