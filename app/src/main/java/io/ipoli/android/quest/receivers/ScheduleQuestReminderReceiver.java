package io.ipoli.android.quest.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.services.ReminderIntentService;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class ScheduleQuestReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_SCHEDULE_REMINDER = "io.ipoli.android.intent.action.SCHEDULE_QUEST_REMINDER";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);

        Quest q = questPersistenceService.findQuestStartingAfter(new Date());
        if (q == null) {
            return;
        }
        Intent i = new Intent(context, ReminderIntentService.class);
        i.setAction(ReminderIntentService.ACTION_REMIND_START_QUEST);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, q.getId());
        PendingIntent pendingIntent = PendingIntent.getService(context, Constants.REMIND_QUEST_START_REQUEST_CODE,
                i, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);

        alarm.setExact(AlarmManager.RTC_WAKEUP, Quest.getStartDateTime(q).getTime(), pendingIntent);
    }
}
