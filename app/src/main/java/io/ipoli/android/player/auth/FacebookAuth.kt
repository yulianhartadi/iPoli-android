package io.ipoli.android.player.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.facebook.*
import com.facebook.internal.CallbackManagerImpl
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/13/17.
 */
class FacebookAuth private constructor(private val controller: Controller) : RxSocialAuth {

    override fun login(username: String): Single<AuthResult?> {
        return loginWithReadPermissions(controller.activity!!, permissions)
            .flatMap { loginInfo ->
                val parameters = Bundle()
                parameters.putString("fields", "email,id,first_name,last_name,picture")
                FacebookAuth.create(controller)
                    .accessToken(loginInfo.accessToken)
                    .params(parameters)
                    .requestMe()
                    .subscribeOn(Schedulers.io())
            }.map { graphResponse ->
            val response = graphResponse.jsonObject
            AuthResult(AccessToken.getCurrentAccessToken().token,
                AuthProvider(
                    response.getString("id"),
                    ProviderType.FACEBOOK.name,
                    response.getString("first_name"),
                    response.getString("last_name"),
                    response.getString("first_name"),
                    if (response.has("email")) response.getString("email") else "",
                    response.getJSONObject("picture").getJSONObject("data").getString("url")
                ),
                username
            )
        }
    }

    override fun logout(): Completable {
        loginHandler?.shutdown()
        loginHandler = null

        return Completable.create { emitter ->
            LoginManager.getInstance().logOut()
            emitter.onComplete()
        }
    }

    private var accessToken: AccessToken? = null

    private var params: Bundle? = null

    private val permissions: Collection<String> = ArrayList()

    fun params(params: Bundle): FacebookAuth {
        this.params = params
        return this
    }

    fun accessToken(accessToken: AccessToken): FacebookAuth {
        this.accessToken = accessToken
        return this
    }

    private fun requestMe(): Single<GraphResponse> {
        val request = GraphRequest.newMeRequest(accessToken, null)
        request.httpMethod = HttpMethod.GET
        return request(request)
    }

    private fun loginWithReadPermissions(context: Activity, permissions: Collection<String>): Single<LoginResult> {
        return login { LoginManager.getInstance().logInWithReadPermissions(context, permissions) }
    }

    private fun login(action: () -> Unit): Single<LoginResult> {
        loginHandler?.shutdown()

        val subject = PublishSubject.create<LoginResult>()
        loginHandler = LoginHandler(subject)
        return subject
            .doOnSubscribe { action() }
            .doOnComplete { loginHandler?.shutdown() }
            .singleOrError()
    }

    private fun request(request: GraphRequest): Single<GraphResponse> {

        if (accessToken != null && request.accessToken == null) {
            request.accessToken = accessToken
        }

        if (params != null) {
            request.parameters = params
        }

        return Single.create { emitter ->
            val response = request.executeAndWait()
            emitter.onSuccess(response)
        }
    }

    private class LoginHandler internal constructor(private val subject: Subject<LoginResult>) : FacebookCallback<LoginResult> {

        private var facebookCallback: CallbackManager?

        init {
            facebookCallback = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(facebookCallback!!, this)
        }

        /**
         * Shutdown the impl
         */
        internal fun shutdown() {
            LoginManager.getInstance().unregisterCallback(facebookCallback)
            facebookCallback = null
        }

        internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit {
            if (facebookCallback != null) {
                facebookCallback!!.onActivityResult(requestCode, resultCode, data)
                facebookCallback = null
            }
        }

        override fun onSuccess(loginResult: LoginResult) {
            subject.onNext(loginResult)
            subject.onComplete()
        }

        override fun onCancel() {
//            subject.onComplete()
        }

        override fun onError(error: FacebookException) {
            subject.onError(error)
        }

    }

    companion object {

        private var loginHandler: LoginHandler? = null

        fun create(controller: Controller): FacebookAuth {
            return FacebookAuth(controller)
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            val facebookRequestCode = CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()
            if (requestCode == facebookRequestCode && loginHandler != null) {
                loginHandler!!.onActivityResult(requestCode, resultCode, data!!)
                loginHandler = null
            }
        }

    }

}