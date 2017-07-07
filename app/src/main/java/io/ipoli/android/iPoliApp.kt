package io.ipoli.android

import android.app.Application
import io.realm.Realm

/**
 * Created by vini on 7/7/17.
 */
class iPoliApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this)
    }
}