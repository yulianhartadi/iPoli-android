package io.ipoli.android.quest.ui;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.Date;

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

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.quest_start:
                    if (!quest.isStarted()) {
                        eventBus.post(new StartQuestRequestEvent(quest));
                    } else {
                        eventBus.post(new StopQuestRequestEvent(quest));
                    }
                    return true;
                case R.id.quest_snooze:
                    return true;
                case R.id.quest_snooze_for_tomorrow:
                    return true;
                case R.id.quest_duplicate:
                    showDuplicateMenu(view, quest, eventBus);
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

    private static void showDuplicateMenu(View view, Quest quest, Bus eventBus) {
        PopupMenu datesPopupMenu = new PopupMenu(view.getContext(), view);
        datesPopupMenu.inflate(R.menu.duplicate_dates_menu);
        datesPopupMenu.setOnMenuItemClickListener(dateItem -> {
            switch (dateItem.getItemId()) {
                case R.id.today:
                    eventBus.post(new DuplicateQuestRequestEvent(quest, new Date()));
                    return true;
                case R.id.tomorrow:
                    eventBus.post(new DuplicateQuestRequestEvent(quest, DateUtils.getTomorrow()));
                    return true;
                case R.id.custom:
                    eventBus.post(new DuplicateQuestRequestEvent(quest));
                    return true;
            }
            return false;
        });
        datesPopupMenu.show();
    }
}
