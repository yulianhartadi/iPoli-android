package io.ipoli.android.auth

import android.content.Context
import io.ipoli.android.ActivityStarter
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Created by vini on 8/15/17.
 */
interface RxSocialAuth {

    fun login(context: Context, activityStarter: ActivityStarter): Observable<AuthResult>

    fun logout(context: Context): Completable
}

data class AuthResult(
        val token: String,
        val authProvider: AuthProvider?
)