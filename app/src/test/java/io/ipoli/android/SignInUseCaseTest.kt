package io.ipoli.android

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.auth.ProviderType
import io.ipoli.android.auth.RxAnonymousAuth
import io.ipoli.android.player.*
import io.ipoli.android.util.RxSchedulersTestRule
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Rule
import org.junit.Test

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/17.
 */
class SignInUseCaseTest {

    @Rule
    @JvmField
    val rxRule = RxSchedulersTestRule()

    @Test
    fun signInGuestPlayer() {

        val playerRepoMock = mock<PlayerRepository> {
            on { save(any()) } doReturn Single.just(Player())
        }

        val signInRequest = SignInRequest("",false, ProviderType.ANONYMOUS, RxAnonymousAuth.create())

        val observer = TestObserver<SignInStatePartialChange>()

        val useCase = SignInUseCase(playerRepoMock)
        useCase.subscribeOnScheduler = Schedulers.io()
        useCase.execute(signInRequest)
                .subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValueCount(2)
        observer.assertValueAt(0, { it is SignInLoadingPartialChange })
        observer.assertValueAt(1, { it is PlayerSignedInPartialChange })
    }
}