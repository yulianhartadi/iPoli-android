package io.ipoli.android.onboarding.scenes

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.view.ViewCompat
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.redux.android.BaseViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.onboarding.OnboardAction
import io.ipoli.android.onboarding.OnboardReducer
import io.ipoli.android.onboarding.OnboardViewController
import io.ipoli.android.onboarding.OnboardViewState
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.pet.PetState
import kotlinx.android.synthetic.main.controller_onboard_pet.view.*

class PetViewController(args: Bundle? = null) :
    BaseViewController<OnboardAction, OnboardViewState>(
        args
    ) {

    override val stateKey = OnboardReducer.stateKey

    private val animations = mutableListOf<Animator>()

    private val petNameWatcher: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable) {
            dispatch(OnboardAction.ValidatePetName(s.toString()))
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
        val view = container.inflate(R.layout.controller_onboard_pet)
        view.petSea.setAnimation("onboarding_pet_sea.json")
        view.petSun.setAnimation("onboarding_sun.json")
        view.petSea.playAnimation()
        view.petSun.playAnimation()
        return view
    }

    override fun onCreateLoadAction() = OnboardAction.LoadPets

    override fun onAttach(view: View) {
        super.onAttach(view)
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        view.petName.addTextChangedListener(petNameWatcher)

        view.petName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ViewUtils.hideKeyboard(view.petName)
                enterFullScreen()
                dispatch(OnboardAction.ValidatePetName(view.petName.text.toString()))
            }
            true
        }

        view.petText.movementMethod = ScrollingMovementMethod()
        view.petText.isVerticalScrollBarEnabled = false
        view.petNext.dispatchOnClick { OnboardAction.ValidatePetName(view.petName.text.toString()) }

        val anim = TypewriterTextAnimator.of(
            view.petText,
            stringRes(R.string.onboard_pet),
            OnboardViewController.TYPE_SPEED
        )
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                ViewCompat.setScrollIndicators(
                    view.petText,
                    ViewCompat.SCROLL_INDICATOR_END
                )
                view.petText.isVerticalScrollBarEnabled = true
                view.petText.isVerticalFadingEdgeEnabled = false
            }
        })
        anim.start()
        animations.add(anim)
    }

    override fun onDetach(view: View) {
        for(a in animations) {
            a.cancel()
        }
        animations.clear()
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        view.petName.removeTextChangedListener(petNameWatcher)
        super.onDetach(view)
    }

    override fun render(state: OnboardViewState, view: View) {
        when (state.type) {
            OnboardViewState.StateType.PETS_LOADED -> {
                renderPets(view, state)
                view.petName.setOnEditTextImeBackListener(object :
                    EditTextImeBackListener {
                    override fun onImeBack(ctrl: EditTextBackEvent, text: String) {
                        enterFullScreen()
                    }
                })
            }

            OnboardViewState.StateType.PET_SELECTED -> {
                TransitionManager.beginDelayedTransition(view.houseContainer as ViewGroup)
                renderPets(view, state)
            }

            OnboardViewState.StateType.PET_NAME_EMPTY -> {
                view.petNameValidationHint.setText(R.string.pet_name_error)
                view.petName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, errorIcon, null
                )
            }

            OnboardViewState.StateType.PET_NAME_VALID -> {
                view.petNext.dispatchOnClick { OnboardAction.ShowNext }
                view.petNameValidationHint.setText(R.string.pet_name_format_hint)
                view.petName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, validIcon, null
                )
            }

            else -> {
            }
        }

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

    private fun renderPets(
        view: View,
        state: OnboardViewState
    ) {
        view.pet1.setImageResource(state.pet1.androidPetAvatar.image)
        view.pet1State.setImageResource(state.pet1.androidPetAvatar.stateImage[PetState.GOOD]!!)
        view.pet2.setImageResource(state.pet2.androidPetAvatar.image)
        view.pet2State.setImageResource(state.pet2.androidPetAvatar.stateImage[PetState.GOOD]!!)
        view.selectedPet.setImageResource(state.pet.androidPetAvatar.image)
        view.selectedPetState.setImageResource(state.pet.androidPetAvatar.stateImage[PetState.HAPPY]!!)

        view.pet1.dispatchOnClick { OnboardAction.SelectPet1 }
        view.pet2.dispatchOnClick { OnboardAction.SelectPet2 }

        playSelectedPetAnimation(view)
    }

    private fun playSelectedPetAnimation(view: View) {
        val anims = listOf<ImageView>(
            view.selectedPet,
            view.selectedPetState
        )
            .map {
                ObjectAnimator.ofFloat(
                    it,
                    "y",
                    it.y,
                    it.y - ViewUtils.dpToPx(30f, view.context),
                    it.y,
                    it.y - ViewUtils.dpToPx(24f, view.context),
                    it.y
                )
            }

        val set = AnimatorSet()
        set.duration = intRes(android.R.integer.config_longAnimTime).toLong() + 100
        set.playTogether(anims)
        set.interpolator = AccelerateDecelerateInterpolator()
        set.startDelay = 200
        set.start()
    }

    private val PetAvatar.androidPetAvatar: AndroidPetAvatar
        get() = AndroidPetAvatar.valueOf(name)

}