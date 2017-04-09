package io.ipoli.android.quest.ui.menus;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DuplicateQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.ShareQuestEvent;
import io.ipoli.android.quest.events.SnoozeQuestRequestEvent;
import io.ipoli.android.quest.events.StartQuestRequestEvent;
import io.ipoli.android.quest.events.StopQuestRequestEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/13/16.
 */
public class CalendarQuestPopupMenu {
    private static Map<Integer, SnoozeTimeItem> itemIdToSnoozeTimeItem;
    private static Map<Integer, DuplicateDateItem> itemIdToDuplicateDateItem;

    public static void show(View view, Quest quest, Bus eventBus, EventSource source) {
        eventBus.post(new ItemActionsShownEvent(source));
        Context context = view.getContext();
        PopupMenu pm = new PopupMenu(context, view);
        boolean isCompleted = quest.isCompleted();
        int menuRes = isCompleted ? R.menu.calendar_completed_quest_menu : R.menu.calendar_quest_menu;
        pm.inflate(menuRes);

        MenuItem duplicateItem = pm.getMenu().findItem(R.id.quest_duplicate);
        itemIdToDuplicateDateItem = DuplicateQuestItemsHelper.createDuplicateDateMap(context, duplicateItem);
        itemIdToSnoozeTimeItem = new HashMap<>();

        if (!isCompleted) {
            MenuItem startItem = pm.getMenu().findItem(R.id.quest_start);
            startItem.setTitle(quest.isStarted() ? R.string.stop : R.string.start);

            if (quest.isStarted()) {
                hideItemsIfQuestStarted(pm);
            } else {
                itemIdToSnoozeTimeItem = SnoozeQuestItemsHelper.createSnoozeTimeMap(quest, pm.getMenu().findItem(R.id.quest_snooze));
            }
        }


        pm.setOnMenuItemClickListener(item -> {
            if (itemIdToDuplicateDateItem.containsKey(item.getItemId())) {
                eventBus.post(new DuplicateQuestRequestEvent(quest, itemIdToDuplicateDateItem.get(item.getItemId()).date, source));
                return true;
            }

            if (itemIdToSnoozeTimeItem.containsKey(item.getItemId())) {
                SnoozeTimeItem snoozeTimeItem = itemIdToSnoozeTimeItem.get(item.getItemId());
                eventBus.post(new SnoozeQuestRequestEvent(quest, snoozeTimeItem.minutes, snoozeTimeItem.date, snoozeTimeItem.pickTime, snoozeTimeItem.pickDate, source));
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
                    eventBus.post(new SnoozeQuestRequestEvent(quest, LocalDate.now().plusDays(1), source));
                    return true;
                case R.id.quest_edit:
                    eventBus.post(new EditQuestRequestEvent(quest.getId(), source));
                    return true;
                case R.id.quest_delete:
                    eventBus.post(new DeleteQuestRequestEvent(quest, source));
                    return true;
                case R.id.quest_share:
                    eventBus.post(new ShareQuestEvent(quest, source));
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

}
