package io.ipoli.android

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/17/17.
 */
class SignInUseCaseTest {

//    @Rule
//    @JvmField
//    val rxRule = RxSchedulersTestRule()
//
//    @Test
//    fun signInGuestPlayer() {
//
//        val playerRepoMock = mock<PlayerRepository> {
//            on { save(any()) } doReturn Single.just(Player())
//        }
//
//        val signInRequest = SignInRequest("",
//            false,
//            ProviderType.ANONYMOUS,
//            AnonymousAuth.create())
//
//        val observer = TestObserver<SignInStatePartialChange>()
//
//        val useCase = SignInUseCase(playerRepoMock)
//        useCase.execute(signInRequest)
//            .subscribe(observer)
//
//        observer.assertComplete()
//        observer.assertNoErrors()
//        observer.assertValueCount(2)
//        observer.assertValueAt(0, { it is SignInLoadingPartialChange })
//        observer.assertValueAt(1, { it is PlayerSignedInPartialChange })
//    }
}