package io.ipoli.android.quest.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DuplicateQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.SnoozeQuestRequestEvent;
import io.ipoli.android.quest.events.StartQuestRequestEvent;
import io.ipoli.android.quest.events.StopQuestRequestEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/13/16.
 */
public class CalendarQuestPopupMenu {
    private static Context context;
    private static Map<Integer, SnoozeTimeItem> itemIdToSnoozeTimeItem;
    private static Map<Integer, DuplicateDateItem> itemIdToDateItem;

    public static void show(View view, Quest quest, Bus eventBus, EventSource source) {
        context = view.getContext();
        PopupMenu pm = new PopupMenu(context, view);
        boolean isCompleted = Quest.isCompleted(quest);
        int menuRes = isCompleted ? R.menu.calendar_completed_quest_menu : R.menu.calendar_quest_menu;
        pm.inflate(menuRes);

        MenuItem duplicateItem = pm.getMenu().findItem(R.id.quest_duplicate);
        itemIdToDateItem = initDuplicateDatesMap(duplicateItem);
        itemIdToSnoozeTimeItem = new HashMap<>();

        if (!isCompleted) {
            MenuItem start = pm.getMenu().findItem(R.id.quest_start);
            start.setTitle(quest.isStarted() ? R.string.stop : R.string.start);

            if (quest.isStarted()) {
                hideItemsIfQuestStarted(pm);
            } else {
                itemIdToSnoozeTimeItem = initSnoozeTimeMap(quest, pm.getMenu().findItem(R.id.quest_snooze));
            }
        }


        pm.setOnMenuItemClickListener(item -> {
            if (itemIdToDateItem.containsKey(item.getItemId())) {
                eventBus.post(new DuplicateQuestRequestEvent(quest, itemIdToDateItem.get(item.getItemId()).date));
                return true;
            }

            if (itemIdToSnoozeTimeItem.containsKey(item.getItemId())) {
                SnoozeTimeItem snoozeTimeItem = itemIdToSnoozeTimeItem.get(item.getItemId());
                eventBus.post(new SnoozeQuestRequestEvent(quest, snoozeTimeItem.minutes, snoozeTimeItem.date, snoozeTimeItem.pickTime, snoozeTimeItem.pickDate));
                return true;
            }

            switch (item.getItemId()) {
                case R.id.quest_start:
                    if (!quest.isStarted()) {
                        eventBus.post(new StartQuestRequestEvent(quest));
                    } else {
                        eventBus.post(new StopQuestRequestEvent(quest));
                    }
                    return true;
                case R.id.quest_snooze_for_tomorrow:
                    eventBus.post(new SnoozeQuestRequestEvent(quest, DateUtils.getTomorrow()));
                    return true;
                case R.id.quest_edit:
                    eventBus.post(new EditQuestRequestEvent(quest, source));
                    return true;
                case R.id.quest_delete:
                    eventBus.post(new DeleteQuestRequestEvent(quest, source));
                    Toast.makeText(context, R.string.quest_deleted, Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });

        pm.show();
    }

    private static void hideItemsIfQuestStarted(PopupMenu popupMenu) {
        popupMenu.getMenu().findItem(R.id.quest_snooze).setVisible(false);
        popupMenu.getMenu().findItem(R.id.quest_snooze_for_tomorrow).setVisible(false);
        popupMenu.getMenu().findItem(R.id.quest_edit).setVisible(false);
    }

    private static Map<Integer, SnoozeTimeItem> initSnoozeTimeMap(Quest quest, MenuItem snoozeItem) {
        List<SnoozeTimeItem> snoozeTimeItems = getSnoozeTimeItems(quest);
        Map<Integer, SnoozeTimeItem> itemIdToSnoozeTimeItem = new HashMap<>();
        for (SnoozeTimeItem item : snoozeTimeItems) {
            int id = new Random().nextInt();
            itemIdToSnoozeTimeItem.put(id, item);
            snoozeItem.getSubMenu().add(Menu.NONE, id, Menu.NONE, item.title);
        }
        return itemIdToSnoozeTimeItem;
    }

    @NonNull
    private static List<SnoozeTimeItem> getSnoozeTimeItems(Quest quest) {
        List<SnoozeTimeItem> snoozeTimeItems = new ArrayList<>();
        if (isQuestScheduledForTime(quest)) {
            snoozeTimeItems.add(new SnoozeTimeItem("10 min", 10));
            snoozeTimeItems.add(new SnoozeTimeItem("15 min", 15));
            snoozeTimeItems.add(new SnoozeTimeItem("30 min", 30));
            snoozeTimeItems.add(new SnoozeTimeItem("1 hour", 60));
        }
        snoozeTimeItems.add(new SnoozeTimeItem("Tomorrow", DateUtils.getTomorrow()));
        snoozeTimeItems.add(new SnoozeTimeItem("Move to inbox", null));
        SnoozeTimeItem pickTime = new SnoozeTimeItem("Pick time");
        pickTime.pickTime = true;
        snoozeTimeItems.add(pickTime);
        SnoozeTimeItem pickDate = new SnoozeTimeItem("Pick date");
        pickDate.pickDate = true;
        snoozeTimeItems.add(pickDate);
        return snoozeTimeItems;
    }

    private static boolean isQuestScheduledForTime(Quest quest) {
        return quest.getStartMinute() >= 0;
    }

    @NonNull
    private static Map<Integer, DuplicateDateItem> initDuplicateDatesMap(MenuItem duplicateItem) {
        List<DuplicateDateItem> duplicateDateItems = getDuplicateDates();

        Map<Integer, DuplicateDateItem> itemIdToDate = new HashMap<>();
        for (DuplicateDateItem item : duplicateDateItems) {
            int id = new Random().nextInt();
            itemIdToDate.put(id, item);
            duplicateItem.getSubMenu().add(Menu.NONE, id, Menu.NONE, item.title);
        }
        return itemIdToDate;
    }

    @NonNull
    private static List<DuplicateDateItem> getDuplicateDates() {
        List<DuplicateDateItem> duplicateDateItems = new ArrayList<>();
        duplicateDateItems.add(new DuplicateDateItem(context.getString(R.string.today), new Date()));
        duplicateDateItems.add(new DuplicateDateItem(context.getString(R.string.tomorrow), DateUtils.getTomorrow()));
        duplicateDateItems.add(new DuplicateDateItem(context.getString(R.string.pick_date), null));
        return duplicateDateItems;
    }

    private static class DuplicateDateItem {
        String title;
        Date date;

        public DuplicateDateItem(String title, Date date) {
            this.title = title;
            this.date = date;
        }
    }

    private static class SnoozeTimeItem {
        String title;
        int minutes;
        Date date;
        boolean pickTime;
        boolean pickDate;

        public SnoozeTimeItem(String title, int minutes) {
            this.title = title;
            this.minutes = minutes;
            this.date = null;
        }

        public SnoozeTimeItem(String title, Date date) {
            this.title = title;
            this.date = date;
            this.minutes = -1;
        }

        public SnoozeTimeItem(String title) {
            this.title = title;
        }
    }
}
