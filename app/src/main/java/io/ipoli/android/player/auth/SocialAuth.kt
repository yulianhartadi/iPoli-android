package io.ipoli.android.player.auth

import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/15/17.
 */
interface RxSocialAuth {

    fun login(username: String): Single<AuthResult?>

    fun logout(): Completable
}

data class AuthResult(
    val token: String,
    val authProvider: AuthProvider,
    val username: String
)