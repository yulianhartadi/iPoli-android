package io.ipoli.android.quest.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.joda.time.LocalDate;

import java.util.Date;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.receivers.AsyncBroadcastReceiver;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class ScheduleQuestReminderReceiver extends AsyncBroadcastReceiver {

    public static final String ACTION_SCHEDULE_REMINDER = "io.ipoli.android.intent.action.SCHEDULE_QUEST_REMINDER";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    protected Observable<Void> doOnReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        cancelScheduledReminder(context, alarm);

        return questPersistenceService.findPlannedQuestsStartingAfter(new LocalDate()).flatMap(quests -> {
            if (quests.isEmpty()) {
                return Observable.empty();
            }
            scheduleNextReminder(context, alarm, quests.get(0));
            return Observable.empty();
        });
    }

    private void cancelScheduledReminder(Context context, AlarmManager alarm) {
        alarm.cancel(getCancelPendingIntent(context));
    }

    private void scheduleNextReminder(Context context, AlarmManager alarm, Quest q) {
        Intent i = new Intent(RemindStartQuestReceiver.ACTION_REMIND_START_QUEST);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, q.getId());
        PendingIntent pendingIntent = IntentUtils.getBroadcastPendingIntent(context, i);
        Date startDateTime = Quest.getStartDateTime(q);
        if (startDateTime == null) {
            return;
        }
        alarm.setExact(AlarmManager.RTC_WAKEUP, startDateTime.getTime(), pendingIntent);
    }

    public PendingIntent getCancelPendingIntent(Context context) {
        Intent i = new Intent(RemindStartQuestReceiver.ACTION_REMIND_START_QUEST);
        return IntentUtils.getBroadcastPendingIntent(context, i);
    }
}
