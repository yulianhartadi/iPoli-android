package io.ipoli.android.player

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.hannesdorfmann.mosby3.RestoreViewOnCreateMviController
import io.ipoli.android.ApiConstants
import io.ipoli.android.R
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.SyncCredentials
import io.realm.SyncUser
import kotlinx.android.synthetic.main.controller_sign_in.view.*
import timber.log.Timber
import io.realm.SyncConfiguration




/**
 * Created by vini on 8/8/17.
 */
class SignInController : RestoreViewOnCreateMviController<SignInController, SignInPresenter>(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private val RC_GOOGLE_SIGN_IN = 9001

    override fun onConnected(p0: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    private lateinit var googleApiClient: GoogleApiClient

    override fun setRestoringViewState(restoringViewState: Boolean) {
    }

    override fun createPresenter(): SignInPresenter = SignInPresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_sign_in, container, false) as ViewGroup

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ApiConstants.WEB_SERVER_GOOGLE_PLUS_CLIENT_ID)
                .requestEmail()
                .build()

        googleApiClient = GoogleApiClient.Builder(applicationContext!!)
                .enableAutoManage(activity as FragmentActivity, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        view.googleSignIn.setOnClickListener({
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        })

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleGoogleSignInResult(result)
        }
    }

    private fun handleGoogleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            val account = result.signInAccount
            val idToken = account!!.idToken
            if (idToken == null) {
                Timber.d("Token is null")
                return
            } else {
                Timber.d(idToken)
                Flowable.create<SyncUser>({ subscriber ->

                    val credentials = SyncCredentials.google(idToken)
                    val authURL = "http://10.0.2.2:9080/auth"
                    val user = SyncUser.login(credentials, authURL)

                    subscriber.onNext(user)
                    subscriber.onComplete()
                }, BackpressureStrategy.LATEST)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { user ->
                            Timber.d(user.identity)
                            val serverURL = "realm://10.0.2.2:9080/~/default"
                            val configuration = SyncConfiguration.Builder(user, serverURL).build()
                            Realm.setDefaultConfiguration(configuration)
                            val realm = Realm.getInstance(Realm.getDefaultConfiguration())

                        }

            }
        }
    }

}