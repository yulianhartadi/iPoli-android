package io.ipoli.android.quest.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import rx.Observable;

public class AgendaWidgetProvider extends AppWidgetProvider {

    private static final String WIDGET_QUEST_CLICK_ACTION = "io.ipoli.android.intent.actions.WIDGET_QUEST_CLICK_ACTION";

    public static final String QUEST_ACTION_EXTRA_KEY = "quest_action";

    public static final int QUEST_ACTION_VIEW = 1;
    public static final int QUEST_ACTION_COMPLETE = 2;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);

        if (WIDGET_QUEST_CLICK_ACTION.equals(intent.getAction())) {
            String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);

            int questAction = intent.getIntExtra(QUEST_ACTION_EXTRA_KEY, 0);
            if (questAction == QUEST_ACTION_VIEW) {
                onViewQuest(context, questId);
            } else if (questAction == QUEST_ACTION_COMPLETE) {
                onQuestComplete(context, intent, questId);
            }
        }
        super.onReceive(context, intent);
    }

    private void onViewQuest(Context context, String questId) {
        Intent i = new Intent(context, QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    private void onQuestComplete(Context context, Intent intent, String questId) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        questPersistenceService.findById(questId).flatMap(q -> {
            if (q == null) {
                return Observable.empty();
            }

            QuestNotificationScheduler.stopAll(q.getId(), context);
            q.setCompletedAt(new Date());
            q.setCompletedAtMinute(Time.now().toMinutesAfterMidnight());
            return questPersistenceService.save(q).flatMap(savedQuest -> {
                Toast.makeText(context, R.string.quest_complete, Toast.LENGTH_SHORT).show();
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_agenda_list);
                return Observable.empty();
            });
        }).subscribe();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_agenda);
            setupHeader(context, rv);
            setupHeadClickListener(context, rv);
            Intent widgetServiceIntent = createWidgetServiceIntent(context, appWidgetId);
            setupAgenda(widgetServiceIntent, context, appWidgetId, rv);
            setupEmptyView(context, rv);
            updateWidget(appWidgetManager, appWidgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @NonNull
    private Intent createWidgetServiceIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, AgendaWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return intent;
    }

    private void updateWidget(AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews rv) {
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    @NonNull
    private Intent setupAgenda(Intent widgetServiceIntent, Context context, int appWidgetId, RemoteViews rv) {
        rv.setRemoteAdapter(R.id.widget_agenda_list, widgetServiceIntent);
        Intent templateIntent = new Intent(context, AgendaWidgetProvider.class);
        templateIntent.setAction(AgendaWidgetProvider.WIDGET_QUEST_CLICK_ACTION);
        templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        widgetServiceIntent.setData(Uri.parse(widgetServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent templatePendingIntent = PendingIntent.getBroadcast(context, 0, templateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.widget_agenda_list, templatePendingIntent);
        return widgetServiceIntent;
    }

    private void setupHeader(Context context, RemoteViews rv) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.widget_agenda_title_format), Locale.getDefault());
        rv.setTextViewText(R.id.widget_agenda_title, simpleDateFormat.format(new Date()));
    }

    private void setupHeadClickListener(Context context, RemoteViews rv) {
        Intent startAppIntent = new Intent(context, MainActivity.class);
        PendingIntent startAppPendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_agenda_title, startAppPendingIntent);
    }

    private void setupEmptyView(Context context, RemoteViews rv) {
        Intent addQuestIntent = new Intent(context, MainActivity.class);
        addQuestIntent.setAction(MainActivity.ACTION_ADD_QUEST);
        PendingIntent addQuestPendingIntent = PendingIntent.getActivity(context, 0, addQuestIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_agenda_empty, addQuestPendingIntent);
        rv.setEmptyView(R.id.widget_agenda_list, R.id.widget_agenda_empty);
    }
}