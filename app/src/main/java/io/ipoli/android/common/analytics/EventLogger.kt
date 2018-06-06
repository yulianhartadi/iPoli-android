package io.ipoli.android.common.analytics

import android.app.Activity
import android.os.Bundle
import com.amplitude.api.AmplitudeClient
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import io.ipoli.android.BuildConfig
import org.json.JSONObject

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/26/2018.
 */
interface EventLogger {
    fun logEvent(name: String, params: Bundle = Bundle())
    fun logCurrentScreen(activity: Activity, screenName: String)
    fun setPlayerId(playerId: String)
}

class SimpleEventLogger(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val amplitude: AmplitudeClient
) : EventLogger {

    override fun logEvent(name: String, params: Bundle) {
        firebaseAnalytics.logEvent(name, params)
        val o = JSONObject()
        for (k in params.keySet()) {
            o.put(k, params[k])
        }
        amplitude.logEvent(name, o)
        if (!BuildConfig.DEBUG) {
            Crashlytics.log(name)
        }
    }

    override fun logCurrentScreen(activity: Activity, screenName: String) {
        firebaseAnalytics.setCurrentScreen(activity, screenName, null)
        amplitude.logEvent("screen_shown", JSONObject(mapOf("name" to screenName)))
        if (!BuildConfig.DEBUG) {
            Crashlytics.log("screen_$screenName")
        }
    }

    override fun setPlayerId(playerId: String) {
        amplitude.userId = playerId
        firebaseAnalytics.setUserId(playerId)
    }
}