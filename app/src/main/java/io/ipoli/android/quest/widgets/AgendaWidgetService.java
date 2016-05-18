package io.ipoli.android.quest.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/18/16.
 */
public class AgendaWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuestRemoteViewsFactory(getApplicationContext());
    }
}