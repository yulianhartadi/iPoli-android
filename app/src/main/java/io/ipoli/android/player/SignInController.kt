package io.ipoli.android.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.facebook.internal.CallbackManagerImpl
import com.hannesdorfmann.mosby3.RestoreViewOnCreateMviController
import com.jakewharton.rxbinding2.view.RxView
import io.ipoli.android.R
import io.ipoli.android.auth.RxAnonymousAuth
import io.ipoli.android.auth.RxFacebookAuth
import io.ipoli.android.auth.RxGoogleAuth
import io.ipoli.android.daggerComponent
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_sign_in.view.*


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/8/17.
 */
class SignInController : RestoreViewOnCreateMviController<SignInController, SignInPresenter>() {

    private val RC_GOOGLE_SIGN_IN = 9001

    init {
        registerForActivityResult(CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode())
    }

    val signInComponent: SignInComponent by lazy {
        val component = DaggerSignInComponent
                .builder()
                .controllerComponent(daggerComponent)
                .build()
        component.inject(this@SignInController)
        component
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        signInComponent // will ensure that dagger component will be initialized lazily.
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
    }

    override fun createPresenter(): SignInPresenter = signInComponent.createSignInPresenter()
//    override fun createPresenter(): SignInPresenter = SignInPresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_sign_in, container, false) as ViewGroup

//        view.googleSignIn.setOnClickListener({
//            RxGoogleAuth.create()
//                    .login(this)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({ (token, authProvider) ->
//                        Timber.d(token)
//                        Timber.d(authProvider.toString())
//
//                        Flowable.create<SyncUser>({ subscriber ->
//
//                            val credentials = SyncCredentials.google(token)
//                            val authURL = "http://10.0.2.2:9080/auth"
//                            val user = SyncUser.login(credentials, authURL)
//
//                            subscriber.onNext(user)
//                            subscriber.onComplete()
//                        }, BackpressureStrategy.LATEST)
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe { user ->
//                                    Timber.d(user.identity)
//                                    val serverURL = "realm://10.0.2.2:9080/~/default"
//                                    val configuration = SyncConfiguration.Builder(user, serverURL).build()
//                                    Realm.setDefaultConfiguration(configuration)
//                                    val realm = Realm.getInstance(Realm.getDefaultConfiguration())
//                                }
//                    })
//        })


//                .subscribe{ result -> Timber.d(result.)}

//                .subscribe({ syncUser ->
//                    //                        Timber.d(token)
////                        Timber.d(authProvider.toString())
////
////                        Flowable.create<SyncUser>({ subscriber ->
////
////                            val credentials = SyncCredentials.facebook(token)
////                            val authURL = "http://10.0.2.2:9080/auth"
////                            val user = SyncUser.login(credentials, authURL)
////
////                            subscriber.onNext(user)
////                            subscriber.onComplete()
////                        }, BackpressureStrategy.LATEST)
////                                .subscribeOn(Schedulers.io())
////                                .observeOn(AndroidSchedulers.mainThread())
////                                .subscribe { user ->
////                                    Timber.d(user.identity)
////                                    val serverURL = "realm://10.0.2.2:9080/~/default"
////                                    val configuration = SyncConfiguration.Builder(user, serverURL).build()
////                                    Realm.setDefaultConfiguration(configuration)
////                                    val realm = Realm.getInstance(Realm.getDefaultConfiguration())
////                                }
//                }, { error -> Timber.e(error) })

//        view.facebookSignIn.setOnClickListener({
//            RxFacebookAuth.create(SignInController@ this)
//                    .login(this)
//                    .flatMapCompletable { (token, authProvider) ->
//                        val credentials = SyncCredentials.facebook(token)
//                        val authURL = "http://10.0.2.2:9080/auth"
//                        val user = SyncUser.login(credentials, authURL)
//                        val serverURL = "realm://10.0.2.2:9080/~/default"
//                        val configuration = SyncConfiguration.Builder(user, serverURL).build()
//                        Realm.setDefaultConfiguration(configuration)
//                        PlayerRepository().save(Player(user.identity, authProvider = authProvider))
//                    }
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe {
//                        Timber.d("Welcome")
//                    }
//
//        })

        return view
    }

    fun signInWithGoogleIntent(): Observable<SignInRequest> {
        val containerView = view!!
        return RxView.clicks(containerView.googleSignIn)
                .takeUntil(RxView.detaches(containerView.googleSignIn))
                .map {
                    SignInRequest(
                            containerView.username.text.toString(),
                            containerView.existingPlayer.isChecked,
                            RxGoogleAuth.create(SignInController@ this)
                    )
                }
    }

    fun signInWithFacebookIntent(): Observable<SignInRequest> {
        val containerView = view!!
        return RxView.clicks(containerView.facebookSignIn)
                .takeUntil(RxView.detaches(containerView.facebookSignIn))
                .map {
                    SignInRequest(
                            containerView.username.text.toString(),
                            containerView.existingPlayer.isChecked,
                            RxFacebookAuth.create(SignInController@ this)
                    )
                }
    }

    fun signInAsGuestIntent(): Observable<SignInRequest> {
        val containerView = view!!
        return RxView.clicks(containerView.guestSignIn).takeUntil(RxView.detaches(containerView.guestSignIn)).map {
            SignInRequest(
                    containerView.username.text.toString(),
                    containerView.existingPlayer.isChecked,
                    RxAnonymousAuth.create()
            )
        }
    }

    fun render(state: SignInViewState) {
        when (state) {
            is SignInInitialState -> {
                Toast.makeText(activity, "Start", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        RxGoogleAuth.onActivityResult(requestCode, data)
        RxFacebookAuth.onActivityResult(requestCode, resultCode, data)
    }
}