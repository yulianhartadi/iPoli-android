package io.ipoli.android.quest.ui.menus;

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/14/16.
 */
public class SnoozeQuestItemsHelper {
    
    public static Map<Integer, SnoozeTimeItem> createSnoozeTimeMap(Quest quest, MenuItem snoozeItem) {
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
        snoozeTimeItems.add(new SnoozeTimeItem("Tomorrow", LocalDate.now().plusDays(1)));
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
        return quest.getStartMinute() != null;
    }
}
