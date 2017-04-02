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
import io.ipoli.android.app.activities.QuickAddActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.events.AgendaWidgetDisabledEvent;
import io.ipoli.android.quest.events.AgendaWidgetEnabledEvent;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class AgendaWidgetProvider extends AppWidgetProvider {

    private static final String WIDGET_SHOW_QUEST_ACTION = "io.ipoli.android.intent.actions.WIDGET_SHOW_QUEST_ACTION";
    private static final String WIDGET_ADD_QUEST_ACTION = "io.ipoli.android.intent.actions.WIDGET_ADD_QUEST_ACTION";

    public static final String QUEST_ACTION_EXTRA_KEY = "quest_action";

    public static final int QUEST_ACTION_VIEW = 1;
    public static final int QUEST_ACTION_COMPLETE = 2;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        eventBus.post(new AgendaWidgetEnabledEvent());
    }

    @Override
    public void onDisabled(Context context) {
        eventBus.post(new AgendaWidgetDisabledEvent());
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        if (WIDGET_SHOW_QUEST_ACTION.equals(intent.getAction())) {
            String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);

            int questAction = intent.getIntExtra(QUEST_ACTION_EXTRA_KEY, 0);
            if (questAction == QUEST_ACTION_VIEW) {
                onViewQuest(context, questId);
            } else if (questAction == QUEST_ACTION_COMPLETE) {
                onQuestComplete(questId);
            }
        } else if (WIDGET_ADD_QUEST_ACTION.equals(intent.getAction())) {
            if (!App.hasPlayer()) {
                Toast.makeText(context, R.string.widget_quick_add_validation, Toast.LENGTH_LONG).show();
                return;
            }
            Intent i = new Intent(context, QuickAddActivity.class);
            i.putExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT, " " + context.getString(R.string.today).toLowerCase());
            i.setAction(Long.toString(System.currentTimeMillis()));
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        super.onReceive(context, intent);
    }

    private void onViewQuest(Context context, String questId) {
        Intent i = new Intent(context, QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    private void onQuestComplete(String questId) {
        questPersistenceService.findById(questId, quest -> {
            if (quest == null) {
                return;
            }
            eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.WIDGET));
        });
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_agenda);
            setupHeader(context, rv);
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
        templateIntent.setAction(AgendaWidgetProvider.WIDGET_SHOW_QUEST_ACTION);
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
        setupHeadClickListener(context, rv);
        setupAddClickListener(context, rv);
    }

    private void setupHeadClickListener(Context context, RemoteViews rv) {
        Intent startAppIntent = new Intent(context, MainActivity.class);
        PendingIntent startAppPendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_agenda_header, startAppPendingIntent);
    }

    private void setupAddClickListener(Context context, RemoteViews rv) {
        rv.setOnClickPendingIntent(R.id.widget_agenda_add, getQuickAddPendingIntent(context));
    }

    private PendingIntent getQuickAddPendingIntent(Context context) {
        Intent intent = new Intent(context, AgendaWidgetProvider.class);
        intent.setAction(AgendaWidgetProvider.WIDGET_ADD_QUEST_ACTION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setupEmptyView(Context context, RemoteViews rv) {
        rv.setOnClickPendingIntent(R.id.widget_agenda_empty, getQuickAddPendingIntent(context));
        rv.setEmptyView(R.id.widget_agenda_list, R.id.widget_agenda_empty);
    }
}