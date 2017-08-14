package io.ipoli.android.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.json.JSONObject

/**
 * Created by vini on 8/13/17.
 */
class RxFacebook private constructor() {

    private var accessToken: AccessToken? = null

    private var params: Bundle? = null

    private var tag: Any? = null
    private var version: String? = null
    private var httpMethod: HttpMethod? = null
    private var skipClientToken: Boolean = false
    private var graphPath: String? = null
    private var graphObject: JSONObject? = null

    /**
     * Set the request tag
     * @param tag for the request
     * *
     * @return builder instance
     */
    fun tag(tag: Any): RxFacebook {
        this.tag = tag
        return this
    }

    /**
     * Set the version to use of the graph
     * @param version to use
     * *
     * @return builder instance
     */
    fun version(version: String): RxFacebook {
        this.version = version
        return this
    }

    /**
     * If you wont use a default mode or post/delete/get, this provides a particular HttpMethod
     * @param httpMethod to use
     * *
     * @return builder instance
     */
    fun httpMethod(httpMethod: HttpMethod): RxFacebook {
        this.httpMethod = httpMethod
        return this
    }

    /**
     * @param skipClientToken if it should or not skip the client accessToken
     * *
     * @return builder instance
     * * By default its false.
     */
    fun skipClientToken(skipClientToken: Boolean): RxFacebook {
        this.skipClientToken = skipClientToken
        return this
    }

    /**
     * Graph path to use.
     * If using a specific domain method (eg requesting my user (ME)) this param is ignored
     * @param graphPath to use as route of the endpoint
     * *
     * @return builder instance
     */
    fun graphPath(graphPath: String): RxFacebook {
        this.graphPath = graphPath
        return this
    }

    /**
     * @param graphObject to use as body of the endpoint in a POST
     * *
     * @return builder instance
     */
    fun graphObject(graphObject: JSONObject): RxFacebook {
        this.graphObject = graphObject
        return this
    }

    /**
     * @param params of the request
     * *
     * @return builder instance
     */
    fun params(params: Bundle): RxFacebook {
        this.params = params
        return this
    }

    /**
     * @param accessToken to use in the request auth
     * *
     * @return builder instance
     */
    fun accessToken(accessToken: AccessToken): RxFacebook {
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
    fun requestMe(): Observable<GraphResponse> {
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
    fun loginWithReadPermissions(context: Activity, permissions: Collection<String>): Observable<LoginResult> {
        return login { LoginManager.getInstance().logInWithReadPermissions(context, permissions) }
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with read permissions.
     * Note that one should call in the onActivityResult of the {@param context}
     * [.postLoginActivityResult], since we dont have control over the activity
     * If theres an error on the login phase it will be sent to the error stream
     */
    fun loginWithReadPermissions(context: Fragment, permissions: Collection<String>): Observable<LoginResult> {
        return login { LoginManager.getInstance().logInWithReadPermissions(context, permissions) }
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with write permissions.
     * Note that one should call in the onActivityResult of the {@param context}
     * [.postLoginActivityResult], since we dont have control over the activity
     * If theres an error on the login phase it will be sent to the error stream
     */
    fun loginWithPublishPermissions(context: Activity, permissions: Collection<String>): Observable<LoginResult> {
        return login { LoginManager.getInstance().logInWithPublishPermissions(context, permissions) }
    }

    /**
     * Should be ran on the UI thread
     * Performs a login with write permissions.
     * Note that one should call in the onActivityResult of the {@param context}
     * [.postLoginActivityResult], since we dont have control over the activity
     * If theres an error on the login phase it will be sent to the error stream
     */
    fun loginWithPublishPermissions(context: Fragment, permissions: Collection<String>): Observable<LoginResult> {
        return login { LoginManager.getInstance().logInWithPublishPermissions(context, permissions) }
    }

    /**
     * Perform a simple login agnostic to the type
     * @param action to execute when subscribed
     * *
     * @return observable of a login result
     */
    private fun login(action: () -> Unit): Observable<LoginResult> {
        loginImpl?.shutdown()

        val subject = PublishSubject.create<LoginResult>()
        loginImpl = LoginImpl(subject)
        return subject.doOnSubscribe { action() }
    }

    fun logout(): Observable<Void> {
        loginImpl?.shutdown()
        loginImpl = null

        return Observable.just<Void>(null)
                .doOnSubscribe { LoginManager.getInstance().logOut() }
    }

    /**
     * Perform a request over a given [GraphRequest]
     * @param request to execute
     * *
     * @return observable of a graph response
     */
    @JvmOverloads fun request(request: GraphRequest = GraphRequest()): Observable<GraphResponse> {

        if (httpMethod != null && request.httpMethod == HttpMethod.GET) { // It wont be null, default is GET
            request.httpMethod = httpMethod
        }

        if (accessToken != null && request.accessToken == null) {
            request.accessToken = accessToken
        }

        if (params != null) {
            request.parameters = params
        }

        if (tag != null && request.tag == null) {
            request.tag = tag
        }

        if (version != null && request.version == null) {
            request.version = version
        }

        request.setSkipClientToken(skipClientToken)

        return Observable.create { emitter ->
            val response = request.executeAndWait()
            emitter.onNext(response)
            emitter.onComplete()
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
            facebookCallback = null
            subject.onComplete()
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
            subject.onComplete()
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
        fun create(): RxFacebook {
            return RxFacebook()
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