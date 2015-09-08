package com.curiousily.ipoli.input.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.input.NewInputActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/8/15.
 */
public class InputWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, NewInputActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_add_input);
            views.setOnClickPendingIntent(R.id.widget_add_input_layout, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
