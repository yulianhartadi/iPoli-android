package io.ipoli.android.auth

import com.bluelinelabs.conductor.Controller
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/17.
 */
class RxAnonymousAuth : RxSocialAuth {

    override fun login(controller: Controller): Single<AuthResult> =
            Single.just(
                    AuthResult("",
                            AuthProvider(provider = ProviderType.ANONYMOUS.name)
                    ))

    override fun logout(controller: Controller): Completable = Completable.complete()
}