package io.ipoli.android.player.auth

import android.content.Context
import android.content.Intent
import com.bluelinelabs.conductor.Controller
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import io.ipoli.android.ApiConstants
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.net.ConnectException

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/14/17.
 */
class GoogleAuth private constructor(private val controller: Controller) : RxSocialAuth {

    override fun login(username: String): Single<AuthResult?> {
        val subject = PublishSubject.create<GoogleSignInResult>()
        loginHandler = LoginHandler(subject)
        val googleApiClient = createApiClient(controller.applicationContext!!, loginHandler!!)
        return subject.doOnSubscribe {
            startGoogleLogin(googleApiClient)
        }.doOnComplete {
            if (googleApiClient.isConnected) {
                googleApiClient.disconnect()
            }
        }.flatMap { signInResult ->
            Observable.fromCallable {
                if (signInResult.isSuccess) {
                    val account = signInResult.signInAccount!!
                    AuthResult(account.idToken!!,
                        AuthProvider(account.id!!,
                            ProviderType.GOOGLE.name,
                            account.givenName.toString(),
                            account.familyName.toString(),
                            account.displayName.toString(),
                            account.email.toString(),
                            account.photoUrl.toString()),
                        username)
                } else if (signInResult.status.statusCode == 12501) {
                    null
                } else {
                    throw GoogleSignInError()
                }
            }.subscribeOn(Schedulers.io())
        }.singleOrError()
    }

    override fun logout(): Completable {
        return Completable.create { emitter ->
            val googleApiClient = createApiClient(controller.applicationContext!!, emitter)
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback {
                emitter.onComplete()
                if (googleApiClient.isConnected) {
                    googleApiClient.disconnect()
                }
            }
        }
    }

    private fun createApiClient(context: Context, listener: GoogleApiClient.OnConnectionFailedListener): GoogleApiClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ApiConstants.WEB_SERVER_GOOGLE_PLUS_CLIENT_ID)
            .requestEmail()
            .build()
        return GoogleApiClient.Builder(context)
            .addOnConnectionFailedListener(listener)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    private fun createApiClient(context: Context, emitter: CompletableEmitter): GoogleApiClient {
        return createApiClient(context, GoogleApiClient.OnConnectionFailedListener { connectionResult ->
            emitter.onError(ConnectException(connectionResult.errorMessage))
        })
    }

    private fun startGoogleLogin(apiClient: GoogleApiClient) {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient)
        controller.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private class LoginHandler internal constructor(private val subject: Subject<GoogleSignInResult>) : GoogleApiClient.OnConnectionFailedListener {

        override fun onConnectionFailed(connectionResult: ConnectionResult) {
            subject.onError(ConnectException(connectionResult.errorMessage))
        }

        internal fun onActivityResult(data: Intent) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            subject.onNext(result)
            subject.onComplete()
        }

    }

    companion object {

        private val RC_GOOGLE_SIGN_IN = 9001

        private var loginHandler: GoogleAuth.LoginHandler? = null

        fun create(controller: Controller): GoogleAuth {
            return GoogleAuth(controller)
        }

        fun onActivityResult(requestCode: Int, data: Intent?) {
            if (loginHandler != null && requestCode == RC_GOOGLE_SIGN_IN) {
                loginHandler?.onActivityResult(data!!)
                loginHandler = null
            }
        }
    }
}