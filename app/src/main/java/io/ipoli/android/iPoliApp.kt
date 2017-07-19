package io.ipoli.android

import android.app.Application
import com.orhanobut.logger.Logger
import io.realm.Realm
import timber.log.Timber
import com.orhanobut.logger.AndroidLogAdapter

/**
 * Created by vini on 7/7/17.
 */
class iPoliApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this)
        Logger.addLogAdapter(AndroidLogAdapter())
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                Logger.log(priority, tag, message, t)
            }
        })
    }
}