package io.ipoli.android.player.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.facebook.internal.CallbackManagerImpl
import com.jakewharton.rxbinding2.view.RxView
import io.ipoli.android.R
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.view.BaseController
import io.ipoli.android.iPoliApp
import io.ipoli.android.player.SignInPresenter
import io.ipoli.android.player.auth.AnonymousAuth
import io.ipoli.android.player.auth.FacebookAuth
import io.ipoli.android.player.auth.GoogleAuth
import io.ipoli.android.player.auth.ProviderType
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_sign_in.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/8/17.
 */
class SignInController : BaseController<SignInController, SignInPresenter>(), Injects<Module> {

    private val presenter by required { signInPresenter }

    init {
        registerForActivityResult(CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode())
    }

//    override fun buildComponent(): SignInComponent =
//        DaggerSignInComponent.builder()
//            .controllerComponent(daggerComponent)
//            .build()

    override fun createPresenter() = presenter
//    : SignInPresenter {
//        return SignInPresenter(SignInUseCase(RealmPlayerRepository()), Navigator(router))
//    }

    override fun onContextAvailable(context: Context) {
        inject(iPoliApp.module(context, router))
    }

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
                    GoogleAuth.create(SignInController@ this)
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
                    FacebookAuth.create(SignInController@ this)
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
                AnonymousAuth.create()
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
        GoogleAuth.onActivityResult(requestCode, data)
        FacebookAuth.onActivityResult(requestCode, resultCode, data)
    }
}