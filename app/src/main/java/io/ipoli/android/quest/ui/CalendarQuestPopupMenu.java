package io.ipoli.android.quest.ui;

import android.content.Context;
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
import io.ipoli.android.quest.events.StartQuestRequestEvent;
import io.ipoli.android.quest.events.StopQuestRequestEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/13/16.
 */
public class CalendarQuestPopupMenu {
    private static class DuplicateDate {
        int title;
        Date date;

        public DuplicateDate(int title, Date date) {
            this.title = title;
            this.date = date;
        }
    }

    public static void show(View view, Quest quest, Bus eventBus, EventSource source) {
        Context context = view.getContext();
        PopupMenu popupMenu = new PopupMenu(context, view);
        boolean isCompleted = Quest.isCompleted(quest);
        int menuRes = isCompleted ? R.menu.calendar_completed_quest_menu : R.menu.calendar_quest_menu;
        popupMenu.inflate(menuRes);

        if (!isCompleted) {
            MenuItem start = popupMenu.getMenu().findItem(R.id.quest_start);
            start.setTitle(quest.isStarted() ? R.string.stop : R.string.start);
        }

        List<DuplicateDate> duplicateDates = new ArrayList<>();
        duplicateDates.add(new DuplicateDate(R.string.today, new Date()));
        duplicateDates.add(new DuplicateDate(R.string.tomorrow, DateUtils.getTomorrow()));
        duplicateDates.add(new DuplicateDate(R.string.pick_a_date, null));

        MenuItem duplicateItem = popupMenu.getMenu().findItem(R.id.quest_duplicate);
        Map<Integer, DuplicateDate> itemIdToDate = new HashMap<>();
        for(int i = 0; i < duplicateDates.size(); i++) {
            int id = new Random().nextInt();
            itemIdToDate.put(id, duplicateDates.get(i));
            duplicateItem.getSubMenu().add(Menu.NONE, id, Menu.NONE, duplicateDates.get(i).title);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            if(itemIdToDate.containsKey(item.getItemId())) {
                eventBus.post(new DuplicateQuestRequestEvent(quest, itemIdToDate.get(item.getItemId()).date));
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
                case R.id.quest_snooze:
                    PopupMenu snoozePopupMenu = new PopupMenu(context, view);
                    return true;
                case R.id.quest_snooze_for_tomorrow:
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

        popupMenu.show();
    }
}
