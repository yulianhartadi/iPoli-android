package io.ipoli.android.player

import io.ipoli.android.BaseRxUseCase
import io.reactivex.Observable
import io.reactivex.Scheduler

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/17.
 */
class SignInUseCase(subscribeOnScheduler: Scheduler?, observeOnScheduler: Scheduler?) : BaseRxUseCase<SignInRequest, SignInStatePartialChange>(subscribeOnScheduler, observeOnScheduler) {

    override fun createObservable(params: SignInRequest): Observable<SignInStatePartialChange> {
        return params.socialAuth.login(params.username).map {
            PlayerSignedInPartialChange() as SignInStatePartialChange
        }
                .toObservable()
                .startWith(SignInLoadingPartialChange())
    }
}