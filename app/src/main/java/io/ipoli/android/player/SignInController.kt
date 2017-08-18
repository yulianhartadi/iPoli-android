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
import io.ipoli.android.auth.ProviderType
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.controller_sign_in, container, false)
    }

    fun signInWithGoogleIntent(): Observable<SignInRequest> {
        val containerView = view!!
        return RxView.clicks(containerView.googleSignIn)
                .takeUntil(RxView.detaches(containerView.googleSignIn))
                .map {
                    SignInRequest(
                            containerView.username.text.toString(),
                            containerView.existingPlayer.isChecked,
                            ProviderType.GOOGLE,
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
                            ProviderType.FACEBOOK,
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
                    ProviderType.ANONYMOUS,
                    RxAnonymousAuth.create()
            )
        }
    }

    fun render(state: SignInViewState) {
        when (state) {
            is SignInInitialState -> {
                Toast.makeText(activity, "Start", Toast.LENGTH_SHORT).show()
            }

            is SignInLoadingState -> {
                Toast.makeText(activity, "Loading", Toast.LENGTH_SHORT).show()
            }

            is PlayerSignedInState -> {
                Toast.makeText(activity, "Signed in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        RxGoogleAuth.onActivityResult(requestCode, data)
        RxFacebookAuth.onActivityResult(requestCode, resultCode, data)
    }
}