package mypoli.android.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.di.Module
import mypoli.android.myPoliApp
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/03/2018.
 */
abstract class AsyncBroadcastReceiver : BroadcastReceiver(), Injects<Module> {

    override fun onReceive(context: Context, intent: Intent) {
        inject(myPoliApp.module(context))
        val res = goAsync()
        launch(CommonPool) {
            onReceiveAsync(intent)
            res.finish()
        }
    }

    abstract fun onReceiveAsync(intent: Intent)
}