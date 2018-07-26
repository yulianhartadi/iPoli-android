package io.ipoli.android.common

import com.crashlytics.android.Crashlytics
import io.ipoli.android.BuildConfig
import timber.log.Timber

object ErrorLogger {

    fun log(error: Throwable) {
        if (BuildConfig.DEBUG) {
            Timber.e(error)
        } else {
            Crashlytics.logException(error)
        }
    }
}