package io.ipoli.android.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/13/17.
 */
class RxFacebookAuth private constructor() : RxSocialAuth {

    override fun login(controller: Controller): Single<AuthResult> {
        return loginWithReadPermissions(controller.activity!!, permissions)
                .flatMap { loginInfo ->
                    val parameters = Bundle()
                    parameters.putString("fields", "email,id,first_name,last_name,picture")
                    RxFacebookAuth.create()
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
                    )
            )
        }
    }

    override fun logout(controller: Controller): Completable = logout()

    private var accessToken: AccessToken? = null

    private var params: Bundle? = null

    private val permissions: Collection<String> = ArrayList()

    /**
     * @param params of the request
     * *
     * @return builder instance
     */
    fun params(params: Bundle): RxFacebookAuth {
        this.params = params
        return this
    }

    /**
     * @param accessToken to use in the request auth
     * *
     * @return builder instance
     */
    fun accessToken(accessToken: AccessToken): RxFacebookAuth {
        this.accessToken = accessToken
        return this
    }

    /**
     * Request my user
     * **Note: If the request has errors, it wont be sent to the error stream. Error stream is used
     * for internal errors (of the stream) and NOT for request ones. You should validate that the graphResponse
     * has no error (graphResponse.getError == null)**
     * @return observable of a graph response
     */
    private fun requestMe(): Single<GraphResponse> {
        val request = GraphRequest.newMeRequest(accessToken, null)
        request.httpMethod = HttpMethod.GET
        return request(request)
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with read permissions.
     * Note that one should call in the onActivityResult of the {@param context}
     * [.postLoginActivityResult], since we dont have control over the activity
     * If theres an error on the login phase it will be sent to the error stream
     */
    private fun loginWithReadPermissions(context: Activity, permissions: Collection<String>): Single<LoginResult> {
        return login { LoginManager.getInstance().logInWithReadPermissions(context, permissions) }
    }

    /**
     * Perform a simple login agnostic to the type
     * @param action to execute when subscribed
     * *
     * @return observable of a login result
     */
    private fun login(action: () -> Unit): Single<LoginResult> {
        loginImpl?.shutdown()

        val subject = PublishSubject.create<LoginResult>()
        loginImpl = LoginImpl(subject)
        return subject
                .doOnSubscribe { action() }
                .doOnComplete { loginImpl?.shutdown() }
                .singleOrError()
    }

    private fun logout(): Completable {
        loginImpl?.shutdown()
        loginImpl = null

        return Completable.create { emitter ->
            LoginManager.getInstance().logOut()
            emitter.onComplete()
        }
    }

    /**
     * Perform a request over a given [GraphRequest]
     * @param request to execute
     * *
     * @return observable of a graph response
     */
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

    /**
     * Internal class to use as bridge for the facebook login
     */
    private class LoginImpl internal constructor(private val subject: Subject<LoginResult>) : FacebookCallback<LoginResult> {

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

        /**
         * Trigger.
         * @param requestCode code
         * *
         * @param resultCode code
         * *
         * @param data data
         * *
         * @return if was processed or not the info.
         */
        internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
            if (facebookCallback != null) {
                val processed = facebookCallback!!.onActivityResult(requestCode, resultCode, data)
                facebookCallback = null
                return processed
            }
            return false
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

        private var loginImpl: LoginImpl? = null

        /**
         * Create an empty request builder
         * @return
         */
        fun create(): RxFacebookAuth {
            return RxFacebookAuth()
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
        fun postLoginActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
            if (loginImpl != null) {
                val result = loginImpl!!.onActivityResult(requestCode, resultCode, data)
                loginImpl = null
                return result
            }
            return false
        }

    }

}