package io.ipoli.android.player

import io.ipoli.android.BaseRxUseCase
import io.reactivex.Observable
import io.reactivex.Scheduler

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/17.
 */
class SignInUseCase(subscribeOnScheduler: Scheduler?, observeOnScheduler: Scheduler?) : BaseRxUseCase<SignInUseCaseParams, SignInStatePartialChange>(subscribeOnScheduler, observeOnScheduler) {

    override fun createObservable(params: SignInUseCaseParams): Observable<SignInStatePartialChange> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class SignInUseCaseParams