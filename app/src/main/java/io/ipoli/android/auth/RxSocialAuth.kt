package io.ipoli.android.auth

import com.bluelinelabs.conductor.Controller
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/15/17.
 */
interface RxSocialAuth {

    fun login(controller: Controller): Single<AuthResult>

    fun logout(controller: Controller): Completable
}

data class AuthResult(
        val token: String,
        val authProvider: AuthProvider
)