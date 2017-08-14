package io.ipoli.android.auth

import android.content.Intent
import android.support.v4.app.FragmentActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
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


    fun login(activity: FragmentActivity): Observable<GoogleSignInResult> {
        val subject = PublishSubject.create<GoogleSignInResult>()
        val loginHandler = LoginImpl(subject)
        val googleApiClient = createApiClient(activity, loginHandler)
        subject.doOnComplete {
            if (googleApiClient.isConnected) {
                googleApiClient.disconnect()
            }
        }
        return subject.doOnSubscribe {
            startGoogleLogin(activity, googleApiClient)
        }
    }

    fun logout(activity: FragmentActivity): Completable {
        return Completable.create {
            emitter ->
            val googleApiClient = createApiClient(activity, emitter)
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback {
                emitter.onComplete()
                if (googleApiClient.isConnected) {
                    googleApiClient.disconnect()
                }
            }
        }
    }

    private fun createApiClient(activity: FragmentActivity, listener: GoogleApiClient.OnConnectionFailedListener): GoogleApiClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ApiConstants.WEB_SERVER_GOOGLE_PLUS_CLIENT_ID)
                .requestEmail()
                .build()
        return GoogleApiClient.Builder(activity)
                .addOnConnectionFailedListener(listener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    private fun createApiClient(activity: FragmentActivity, emitter: CompletableEmitter): GoogleApiClient {
        return createApiClient(activity, GoogleApiClient.OnConnectionFailedListener { connectionResult ->
            emitter.onError(ConnectException(connectionResult.errorMessage))
        })
    }

    private fun startGoogleLogin(activity: FragmentActivity, apiClient: GoogleApiClient) {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient)
        activity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    /**
     * Internal class to use as bridge for the facebook login
     */
    private class LoginImpl internal constructor(private val subject: Subject<GoogleSignInResult>) : GoogleApiClient.OnConnectionFailedListener {

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

        private var loginImpl: RxGoogleAuth.LoginImpl? = null

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
            if (loginImpl != null && requestCode == RC_GOOGLE_SIGN_IN) {
                loginImpl!!.onActivityResult(data)
                loginImpl = null
            }
        }
    }
}