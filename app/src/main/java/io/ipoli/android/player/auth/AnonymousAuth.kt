package io.ipoli.android.player.auth

import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/17.
 */
class AnonymousAuth : RxSocialAuth {

    override fun login(username: String): Single<AuthResult?> =
            Single.just(
                    AuthResult("",
                            AuthProvider(provider = ProviderType.ANONYMOUS.name)
                            , username))

    override fun logout(): Completable = Completable.complete()

    companion object {
        fun create(): AnonymousAuth = AnonymousAuth()
    }
}