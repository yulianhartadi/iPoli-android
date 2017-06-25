package io.ipoli.android.quest.ui.menus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.ipoli.android.R;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/14/16.
 */
public class SnoozeQuestItemsHelper {

    public static Map<Integer, SnoozeTimeItem> createSnoozeTimeMap(Context context, Quest quest, MenuItem snoozeItem) {
        List<SnoozeTimeItem> snoozeTimeItems = getSnoozeTimeItems(context, quest);
        Map<Integer, SnoozeTimeItem> itemIdToSnoozeTimeItem = new HashMap<>();
        for (SnoozeTimeItem item : snoozeTimeItems) {
            int id = new Random().nextInt();
            itemIdToSnoozeTimeItem.put(id, item);
            snoozeItem.getSubMenu().add(Menu.NONE, id, Menu.NONE, item.title);
        }
        return itemIdToSnoozeTimeItem;
    }

    @NonNull
    private static List<SnoozeTimeItem> getSnoozeTimeItems(Context context, Quest quest) {
        List<SnoozeTimeItem> snoozeTimeItems = new ArrayList<>();
        if (isQuestScheduledForTime(quest)) {
            snoozeTimeItems.add(new SnoozeTimeItem(String.format(context.getString(R.string.snooze_time_min), 10), 10));
            snoozeTimeItems.add(new SnoozeTimeItem(String.format(context.getString(R.string.snooze_time_min), 15), 15));
            snoozeTimeItems.add(new SnoozeTimeItem(String.format(context.getString(R.string.snooze_time_min), 30), 30));
            snoozeTimeItems.add(new SnoozeTimeItem(String.format(context.getString(R.string.snooze_time_hour), 1), 60));
        }
        snoozeTimeItems.add(new SnoozeTimeItem(context.getString(R.string.tomorrow), LocalDate.now().plusDays(1)));
        snoozeTimeItems.add(new SnoozeTimeItem(context.getString(R.string.move_to_inbox), null));
        SnoozeTimeItem pickTime = new SnoozeTimeItem(context.getString(R.string.pick_time));
        pickTime.pickTime = true;
        snoozeTimeItems.add(pickTime);
        SnoozeTimeItem pickDate = new SnoozeTimeItem(context.getString(R.string.pick_date));
        pickDate.pickDate = true;
        snoozeTimeItems.add(pickDate);
        return snoozeTimeItems;
    }

    private static boolean isQuestScheduledForTime(Quest quest) {
        return quest.getStartMinute() != null;
    }
}
