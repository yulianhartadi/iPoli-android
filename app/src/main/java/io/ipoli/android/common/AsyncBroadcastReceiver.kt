package io.ipoli.android.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.ipoli.android.MyPoliApp
import io.ipoli.android.common.di.BackgroundModule
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/03/2018.
 */
abstract class AsyncBroadcastReceiver : BroadcastReceiver(), Injects<BackgroundModule> {

    override fun onReceive(context: Context, intent: Intent) {
        inject(MyPoliApp.backgroundModule(context))
        val res = goAsync()
        GlobalScope.launch(Dispatchers.IO) {
            onReceiveAsync(context, intent)
            res.finish()
        }
    }

    abstract suspend fun onReceiveAsync(context: Context, intent: Intent)
}