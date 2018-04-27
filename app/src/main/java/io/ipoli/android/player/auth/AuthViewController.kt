package io.ipoli.android.player.auth

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.bluelinelabs.conductor.RouterTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import io.ipoli.android.R
import io.ipoli.android.common.EmailUtils
import io.ipoli.android.common.LoaderDialogController
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.home.HomeViewController
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.player.auth.error.SignInError
import io.ipoli.android.player.data.AndroidAvatar
import io.ipoli.android.player.data.Avatar
import kotlinx.android.synthetic.main.controller_auth.view.*
import kotlinx.android.synthetic.main.item_auth_avatar.view.*


/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/5/18.
 */
class AuthViewController(args: Bundle? = null) :
    ReduxViewController<AuthAction, AuthViewState, AuthReducer>(args) {

    private val RC_SIGN_IN = 123

    override val reducer = AuthReducer

    private var loader: LoaderDialogController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_auth, container, false)

        registerForActivityResult(RC_SIGN_IN)

        Glide.with(view.context).load(AndroidAvatar.AVATAR_03.image)
            .apply(RequestOptions.circleCropTransform())
            .into(view.playerAvatar)

        view.avatarList.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false)
        view.avatarList.adapter = AvatarAdapter()

        (view.avatarList.adapter as AvatarAdapter).updateAll(
            listOf(
                AvatarViewModel(
                    stringRes(AndroidAvatar.AVATAR_03.avatarName),
                    AndroidAvatar.AVATAR_03.image,
                    AndroidAvatar.AVATAR_03.backgroundColor,
                    Avatar.AVATAR_03
                ),
                AvatarViewModel(
                    stringRes(AndroidAvatar.AVATAR_04.avatarName),
                    AndroidAvatar.AVATAR_04.image,
                    AndroidAvatar.AVATAR_04.backgroundColor,
                    Avatar.AVATAR_04
                ),
                AvatarViewModel(
                    stringRes(AndroidAvatar.AVATAR_05.avatarName),
                    AndroidAvatar.AVATAR_05.image,
                    AndroidAvatar.AVATAR_05.backgroundColor,
                    Avatar.AVATAR_05
                ),
                AvatarViewModel(
                    stringRes(AndroidAvatar.AVATAR_06.avatarName),
                    AndroidAvatar.AVATAR_06.image,
                    AndroidAvatar.AVATAR_06.backgroundColor,
                    Avatar.AVATAR_06
                ),
                AvatarViewModel(
                    stringRes(AndroidAvatar.AVATAR_07.avatarName),
                    AndroidAvatar.AVATAR_07.image,
                    AndroidAvatar.AVATAR_07.backgroundColor,
                    Avatar.AVATAR_07
                ),
                AvatarViewModel(
                    stringRes(AndroidAvatar.AVATAR_08.avatarName),
                    AndroidAvatar.AVATAR_08.image,
                    AndroidAvatar.AVATAR_08.backgroundColor,
                    Avatar.AVATAR_08
                )
            )
        )

//        view.switchToLogin.dispatchOnClick(AuthAction.SwitchViewType)
//        view.closeLogin.dispatchOnClick(AuthAction.SwitchViewType)
//
//        view.switchToLogin.setImageDrawable(
//            IconicsDrawable(view.context)
//                .icon(GoogleMaterial.Icon.gmd_vpn_key)
//                .colorRes(R.color.md_white)
//                .sizeDp(24)
//        )
        return view
    }

    private fun createLoader() =
        LoaderDialogController(
            R.string.sign_in_loading_dialog_title,
            R.string.sign_in_loading_dialog_message
        )

    override fun render(state: AuthViewState, view: View) {
        when (state.type) {

            AuthViewState.StateType.LOADING -> {
                // show loading
            }

            AuthViewState.StateType.SHOW_SIGN_UP -> {

//                if (view.loginContainer.visible) {
//                    playShowSignUpAnimation(view, {
//                        renderSighUpViews(view, state)
//                    })
//                } else {
                    renderSighUpViews(view, state)
//                }

                view.anonymousSignUp.setOnClickListener {
                    playShowLoginAnimation(view, {
                        view.loginContainer.visibility = View.INVISIBLE
                        view.loginPet.visible = false
                        view.setupPet.visible = true
//                        view.signUpHeadline.setText(R.string.welcome_back_hero)
                    })
                }

//                view.googleSignIn.setOnClickListener {
//                    dispatch(
//                        AuthAction.SignUp(
//                            view.username.text.toString(),
//                            AuthViewState.Provider.GOOGLE
//                        )
//                    )
//                }
//                view.facebookSignIn.setOnClickListener {
//                    dispatch(
//                        AuthAction.SignUp(
//                            view.username.text.toString(),
//                            AuthViewState.Provider.FACEBOOK
//                        )
//                    )
//                }
//
//                view.anonymousSignUp.setOnClickListener {
//                    dispatch(
//                        AuthAction.SignUp(
//                            view.username.text.toString(),
//                            AuthViewState.Provider.GUEST
//                        )
//                    )
//                }
            }

            AuthViewState.StateType.SHOW_LOGIN -> {
                playShowLoginAnimation(view, {
                    view.loginContainer.visibility = View.INVISIBLE
                    view.loginPet.visible = false
                    view.setupPet.visible = true
//                    view.signUpHeadline.setText(R.string.welcome_back_hero)
                })

//                view.googleLogin.setOnClickListener {
//                    dispatch(
//                        AuthAction.Login(
//                            AuthViewState.Provider.GOOGLE
//                        )
//                    )
//                }
//                view.facebookLogin.setOnClickListener {
//                    dispatch(
//                        AuthAction.Login(
//                            AuthViewState.Provider.FACEBOOK
//                        )
//                    )
//                }
            }

            AuthViewState.StateType.GOOGLE_AUTH_STARTED -> {
                showLoader()
                startActivityForResult(
                    startSignUpForProvider(AuthUI.IdpConfig.GoogleBuilder().build()),
                    RC_SIGN_IN
                )
            }

            AuthViewState.StateType.FACEBOOK_AUTH_STARTED -> {
                showLoader()
                startActivityForResult(
                    startSignUpForProvider(AuthUI.IdpConfig.FacebookBuilder().build()),
                    RC_SIGN_IN
                )
            }

            AuthViewState.StateType.GUEST_AUTH_STARTED -> {
                showLoader()
                FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener {
                    if (it.isSuccessful) {
//                        dispatch(
//                            AuthAction.CompleteUserAuth(
//                                FirebaseAuth.getInstance().currentUser!!,
//                                view.username.text.toString()
//                            )
//                        )
                    } else {
                        hideLoader()
                        showShortToast(R.string.something_went_wrong)
                        Crashlytics.logException(it.exception)
                    }
                }
            }

            AuthViewState.StateType.USERNAME_VALIDATION_ERROR -> {
                hideLoader()
//                view.username.error = state.usernameErrorMessage(activity!!)
            }

            AuthViewState.StateType.PLAYER_CREATED -> {
                hideLoader()
                startHomeViewController()
            }

            AuthViewState.StateType.PLAYER_LOGGED_IN -> {
                hideLoader()
                showShortToast(R.string.welcome_hero)
                startHomeViewController()
            }

            AuthViewState.StateType.GUEST_PLAYER_LOGGED_IN -> {
                hideLoader()
                showShortToast(R.string.welcome_hero)
                rootRouter.popCurrentController()
            }

            AuthViewState.StateType.ACCOUNTS_LINKED -> {
                hideLoader()
                rootRouter.popCurrentController()
            }

            AuthViewState.StateType.DELETE_ACCOUNT -> {
                AuthUI.getInstance()
                    .delete(view.context.applicationContext)
                    .addOnCompleteListener { task ->
                        hideLoader()
                        if (task.isSuccessful) {
                            showShortToast(R.string.login_no_existing_user_error)
                            dispatch(AuthAction.SwitchViewType)
                        } else {
                            Crashlytics.logException(task.exception)
                            showHelpSnackbar(view, FirebaseAuth.getInstance().currentUser?.uid)
                        }
                    }
            }

            AuthViewState.StateType.SIGN_OUT_ACCOUNT -> {
                AuthUI.getInstance()
                    .signOut(view.context.applicationContext)
                    .addOnCompleteListener { task ->
                        hideLoader()
                        if (task.isSuccessful) {
                            showShortToast(R.string.login_anonymous_no_existing_user_error)
                            dispatch(AuthAction.SwitchViewType)
                        } else {
                            Crashlytics.logException(task.exception)
                            showHelpSnackbar(view, FirebaseAuth.getInstance().currentUser?.uid)
                        }
                    }
            }
        }
    }

    private fun showHelpSnackbar(view: View, playerId: String?) {
        exitFullScreen()
        val snackbar = Snackbar.make(
            view.authContainer,
            R.string.login_error_contact_us_message,
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction(R.string.contact_us, { _ ->
            EmailUtils.send(
                view.context,
                stringRes(R.string.help_me_login),
                playerId ?: "",
                stringRes(R.string.contact_us_email_chooser_title)
            )
        })
        snackbar.show()
    }

    private fun hideLoader() {
        loader?.dismiss()
        loader = null
    }

    private fun showLoader() {
        loader = createLoader()
        loader!!.show(router, "loader")
    }

    private fun renderSighUpViews(view: View, state: AuthViewState) {
        view.setupContainer.visibility = View.INVISIBLE
//        if (state.isGuest)
//            view.guestGroup.goneViews()
//        else
            view.guestGroup.showViews()
        view.loginPet.visible = true
        view.setupPet.visible = false
        view.signUpHeadline.setText(R.string.welcome_hero)

    }

    private fun playShowSignUpAnimation(
        view: View,
        endAnimation: () -> Unit
    ) {
        val set = AnimatorSet()
        set.playTogether(
            createContainerRevealAnimation(view.setupContainer, reverse = true),
            createContainerRevealAnimation(view.loginContainer)
        )
        set.interpolator = AccelerateDecelerateInterpolator()
        set.duration = longAnimTime

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.loginContainer.visibility = View.VISIBLE
                (view.signUpContent.views() + view.guestGroup.views()).forEach {
                    it.alpha = 0f
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
//                view.switchToLogin.visible = true
                (view.signUpContent.views() + view.guestGroup.views()).forEach {
                    it.animate().alpha(1f).start()
                }

                endAnimation()

                val translateX = view.loginContainer.width / 2
                val translateY =
                    view.loginContainer.height / 2 - ViewUtils.dpToPx(
                        24f,
                        view.context
                    )

//                view.switchToLogin
//                    .animate()
//                    .translationXBy(translateX.toFloat())
//                    .translationYBy(-translateY)
//                    .setDuration(mediumAnimTime)
//                    .start()

            }
        })

        set.start()
    }

    private fun playShowLoginAnimation(view: View, animationEnd: () -> Unit) {
        val loginContainer = view.setupContainer
        val signUpContainer = view.loginContainer

        val translateX = view.loginContainer.width / 2
        val translateY =
            view.loginContainer.height / 2 - ViewUtils.dpToPx(24f, view.context)

//        view.switchToLogin
//            .animate()
//            .translationXBy((-translateX).toFloat())
//            .translationYBy(translateY)
//            .setDuration(mediumAnimTime)
//            .withEndAction {
//
                val set = AnimatorSet()
                set.playTogether(
                    createContainerRevealAnimation(loginContainer),
                    createContainerRevealAnimation(signUpContainer, true)
                )
                set.interpolator = AccelerateDecelerateInterpolator()
                set.duration = longAnimTime

                set.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
//                        view.switchToLogin.visible = false
                        loginContainer.visibility = View.VISIBLE
                        view.setupContent.views().forEach {
                            it.alpha = 0f
                        }
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        view.setupContent.views().forEach {
                            it.animate().alpha(1f).start()
                        }
                        animationEnd()
                    }
                })

                set.start()
//            }
//            .start()
    }

    private fun createContainerRevealAnimation(
        container: ViewGroup,
        reverse: Boolean = false
    ): Animator? {
        val halfWidth = container.width / 2
        val halfHeight = container.height / 2
        val radius = Math.max(halfWidth, halfHeight).toFloat()
        return ViewAnimationUtils
            .createCircularReveal(
                container,
                halfWidth,
                halfHeight,
                if (reverse) radius else 0f,
                if (reverse) 0f else radius
            )
    }

    private fun startHomeViewController() {
        rootRouter.popCurrentController()
        rootRouter.pushController(RouterTransaction.with(HomeViewController()))
    }

    private fun startSignUpForProvider(provider: AuthUI.IdpConfig): Intent {
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(listOf(provider))
            .setIsAccountLinkingEnabled(true, null)
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
//                dispatch(
//                    AuthAction.CompleteUserAuth(
//                        FirebaseAuth.getInstance().currentUser!!,
//                        view!!.username.text.toString()
//                    )
//                )
                return
            } else {
                hideLoader()
                val hasUserPressedBack = response == null
                if (hasUserPressedBack) {
                    return
                }

                if (response!!.errorCode == ErrorCodes.NO_NETWORK) {
                    showShortToast(R.string.sign_in_internet)
                    Crashlytics.logException(SignInError("Attempt to login without internet"))
                    return
                }

                if (response.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    showShortToast(R.string.something_went_wrong)
                    Crashlytics.logException(SignInError("Unknown login error with code ${response.errorCode}"))
                    return
                }
            }

        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
//        view.username.clearFocus()
        enterFullScreen()
        dispatch(AuthAction.Load)
    }

    override fun onDetach(view: View) {
        exitFullScreen()
        super.onDetach(view)
    }

    data class AvatarViewModel(
        val name: String,
        @DrawableRes val image: Int,
        @ColorRes val backgroundColor: Int,
        val avatar: Avatar
    )

    inner class AvatarAdapter :
        BaseRecyclerViewAdapter<AvatarViewModel>(R.layout.item_auth_avatar) {

        override fun onBindViewModel(vm: AvatarViewModel, view: View, holder: SimpleViewHolder) {
            view.avatarName.text = vm.name
            view.avatarImage.setBackgroundResource(vm.backgroundColor)
            view.avatarImage.setImageResource(vm.image)
        }

    }

}