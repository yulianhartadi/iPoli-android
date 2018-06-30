package io.ipoli.android.onboarding.scenes

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.redux.android.BaseViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.onboarding.*
import io.ipoli.android.player.data.AndroidAvatar
import kotlinx.android.synthetic.main.controller_onboard_avatar.view.*

class AvatarViewController(args: Bundle? = null) :
    BaseViewController<OnboardAction, OnboardViewState>(
        args
    ) {

    override val stateKey = OnboardReducer.stateKey

    private val animations = mutableListOf<Animator>()

    private val usernameWatcher: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable) {
            renderUsernameLengthHint(s.length)
            dispatch(OnboardAction.ValidateUsername(s.toString()))
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = container.inflate(R.layout.controller_onboard_avatar)
        view.avatarButterflies.setAnimation("onboarding_butterflies.json")
        view.avatarSun.setAnimation("onboarding_sun.json")
        view.avatarTrees.setAnimation("onboarding_avatar_trees.json")
        view.avatarButterflies.playAnimation()
        view.avatarSun.playAnimation()
        view.avatarTrees.playAnimation()

        view.avatarNext.dispatchOnClick { OnboardAction.ValidateUsername(view.avatarUsername.text.toString()) }

        return view
    }

    override fun onCreateLoadAction(): OnboardAction? = OnboardAction.LoadAvatars

    override fun onAttach(view: View) {
        super.onAttach(view)
        view.avatarUsername.addTextChangedListener(usernameWatcher)
        view.avatarUsername.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ViewUtils.hideKeyboard(view.avatarUsername)
                enterFullScreen()
                dispatch(OnboardAction.ValidateUsername(view.avatarUsername.text.toString()))
            }
            true
        }
        view.avatarText.movementMethod = ScrollingMovementMethod()
        view.avatarText.isVerticalScrollBarEnabled = false
        val anim = TypewriterTextAnimator.of(
            view.avatarText,
            stringRes(R.string.onboard_player),
            OnboardViewController.TYPE_SPEED
        )
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                ViewCompat.setScrollIndicators(
                    view.avatarText,
                    ViewCompat.SCROLL_INDICATOR_END
                )
                view.avatarText.isVerticalScrollBarEnabled = true
                view.avatarText.isVerticalFadingEdgeEnabled = false
            }
        })
        anim.start()
        animations.add(anim)
    }

    override fun onDetach(view: View) {
        for (a in animations) {
            a.cancel()
        }
        animations.clear()
        view.avatarUsername.removeTextChangedListener(usernameWatcher)
        super.onDetach(view)
    }

    private fun renderUsernameLengthHint(length: Int) {
        view!!.usernameLengthHint.text = "$length/${Constants.USERNAME_MAX_LENGTH}"
    }

    override fun render(state: OnboardViewState, view: View) {

        when (state.type) {

            OnboardViewState.StateType.AVATARS_LOADED -> {
                view.avatarUsername.setOnEditTextImeBackListener(object :
                    EditTextImeBackListener {
                    override fun onImeBack(ctrl: EditTextBackEvent, text: String) {
                        enterFullScreen()
                    }
                })
                renderAvatar(view, state)
                renderAvatars(view, state)
            }

            OnboardViewState.StateType.AVATAR_SELECTED -> {
                renderAvatar(view, state)
            }


            OnboardViewState.StateType.USERNAME_VALIDATION_ERROR -> {
                view.usernameValidationHint.text = state.usernameErrorMessage(activity!!)
                view.avatarUsername.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, errorIcon, null
                )
            }

            OnboardViewState.StateType.USERNAME_VALID -> {
                view.avatarNext.dispatchOnClick { OnboardAction.ShowNext }
                view.usernameValidationHint.text = stringRes(R.string.valid_username)
                view.avatarUsername.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, validIcon, null
                )
            }

            else -> {
            }
        }

    }

    private fun renderAvatars(
        view: View,
        state: OnboardViewState
    ) {
        val avatarViews =
            view.topAvatarsContainer.children + view.bottomAvatarsContainer.children

        state.avatars.forEachIndexed { index, avatar ->
            val v = avatarViews[index] as ImageView
            v.setImageResource(AndroidAvatar.valueOf(avatar.name).image)
            v.dispatchOnClick { OnboardAction.SelectAvatar(index) }
        }
    }

    private fun renderAvatar(
        view: View,
        state: OnboardViewState
    ) {
        view.avatarImage.setImageResource(AndroidAvatar.valueOf(state.avatar.name).image)
    }

    private val errorIcon
        get() = IconicsDrawable(activity)
            .normalIcon(
                GoogleMaterial.Icon.gmd_error,
                R.color.md_red_500
            ).respectFontBounds(true)

    private val validIcon
        get() = IconicsDrawable(activity)
            .normalIcon(
                GoogleMaterial.Icon.gmd_verified_user,
                R.color.md_green_500
            ).respectFontBounds(true)

}