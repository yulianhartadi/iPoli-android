package io.ipoli.android.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import io.ipoli.android.ActivityStarter
import io.ipoli.android.ApiConstants
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.net.ConnectException

/**
 * Created by vini on 8/14/17.
 */
class RxGoogleAuth {

    fun login(context: Context, activityStarter: ActivityStarter): Observable<GoogleSignInResult> {
        val subject = PublishSubject.create<GoogleSignInResult>()
        loginHandler = LoginHandler(subject)
        val googleApiClient = createApiClient(context, loginHandler!!)
        subject.doOnComplete {
            if (googleApiClient.isConnected) {
                googleApiClient.disconnect()
            }
        }
        return subject.doOnSubscribe {
            startGoogleLogin(googleApiClient, activityStarter)
        }
    }

    fun logout(context: Context): Completable {
        return Completable.create {
            emitter ->
            val googleApiClient = createApiClient(context, emitter)
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

    private fun startGoogleLogin(apiClient: GoogleApiClient, activityStarter: ActivityStarter) {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient)
        activityStarter.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    /**
     * Internal class to use as bridge for the facebook login
     */
    private class LoginHandler internal constructor(private val subject: Subject<GoogleSignInResult>) : GoogleApiClient.OnConnectionFailedListener {

        override fun onConnectionFailed(connectionResult: ConnectionResult) {
            subject.onError(ConnectException(connectionResult.errorMessage))
        }

        internal fun onActivityResult(data: Intent): Unit {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            subject.onNext(result)
            subject.onComplete()
        }

    }

    companion object {

        private val RC_GOOGLE_SIGN_IN = 9001

        private var loginHandler: RxGoogleAuth.LoginHandler? = null

        /**
         * Create an empty request builder
         * @return
         */
        fun create(): RxGoogleAuth {
            return RxGoogleAuth()
        }

        /**
         * Post the login results to be processed. The initial stream used will output its results/error
         * @param requestCode code used for request
         * *
         * @param resultCode result code
         * *
         * @param data of the result
         * *
         * @return if the on activity result could be parsed or not (maybe it wasnt a facebook intent the one started?)
         */
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit {
            if (loginHandler != null && requestCode == RC_GOOGLE_SIGN_IN) {
                loginHandler!!.onActivityResult(data)
                loginHandler = null
            }
        }
    }
}