package io.ipoli.android.common.analytics

import android.app.Activity
import com.amplitude.api.AmplitudeClient
import com.crashlytics.android.Crashlytics
import io.ipoli.android.BuildConfig
import org.json.JSONObject

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/26/2018.
 */
interface EventLogger {
    fun logEvent(name: String, data: Map<String, Any?> = emptyMap())
    fun logCurrentScreen(activity: Activity, screenName: String)
    fun setPlayerId(playerId: String)
}

class SimpleEventLogger(
    private val amplitude: AmplitudeClient
) : EventLogger {

    override fun logEvent(name: String, data: Map<String, Any?>) {
        amplitude.logEvent(name, JSONObject(data))
        if (!BuildConfig.DEBUG) {
            Crashlytics.log(name)
        }
    }

    override fun logCurrentScreen(activity: Activity, screenName: String) {
        amplitude.logEvent("screen_shown", JSONObject(mapOf("name" to screenName)))
        if (!BuildConfig.DEBUG) {
            Crashlytics.log("screen_$screenName")
        }
    }

    override fun setPlayerId(playerId: String) {
        amplitude.userId = playerId
    }
}