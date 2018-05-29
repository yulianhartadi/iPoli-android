package io.ipoli.android.common.analytics

import android.app.Activity
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import io.ipoli.android.BuildConfig

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/26/2018.
 */
interface EventLogger {
    fun logEvent(name: String, params: Bundle = Bundle())
    fun logCurrentScreen(activity: Activity, screenName: String)
    fun setPlayerId(playerId: String)
}

class FirebaseEventLogger(private val firebaseAnalytics: FirebaseAnalytics) : EventLogger {

    override fun logEvent(name: String, params: Bundle) {
        firebaseAnalytics.logEvent(name, params)
        if (!BuildConfig.DEBUG) {
            Crashlytics.log(name)
        }
    }

    override fun logCurrentScreen(activity: Activity, screenName: String) {
        firebaseAnalytics.setCurrentScreen(activity, screenName, null)
        if (!BuildConfig.DEBUG) {
            Crashlytics.log("screen_$screenName")
        }
    }

    override fun setPlayerId(playerId: String) =
        firebaseAnalytics.setUserId(playerId)
}