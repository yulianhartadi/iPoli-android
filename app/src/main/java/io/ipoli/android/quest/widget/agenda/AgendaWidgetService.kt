package io.ipoli.android.quest.widget.agenda

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/10/2018.
 */

import android.content.Intent
import android.widget.RemoteViewsService

class AgendaWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?) =
        AgendaWidgetViewsFactory(applicationContext)
}