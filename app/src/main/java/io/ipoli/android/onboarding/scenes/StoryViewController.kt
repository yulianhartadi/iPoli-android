package io.ipoli.android.onboarding.scenes

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.transition.AutoTransition
import android.support.transition.Transition
import android.support.transition.TransitionListenerAdapter
import android.support.transition.TransitionManager
import android.support.v4.view.ViewCompat
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.BaseViewController
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.common.view.inflate
import io.ipoli.android.common.view.stringRes
import io.ipoli.android.onboarding.OnboardAction
import io.ipoli.android.onboarding.OnboardReducer
import io.ipoli.android.onboarding.OnboardViewController
import io.ipoli.android.onboarding.OnboardViewState
import kotlinx.android.synthetic.main.controller_onboard_story_start.view.*

class StoryViewController(args: Bundle? = null) :
    BaseViewController<OnboardAction, OnboardViewState>(
        args
    ) {

    override val stateKey = OnboardReducer.stateKey

    private val animations = mutableListOf<Animator>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = container.inflate(R.layout.controller_onboard_story_start)

        view.storySnail.setAnimation("onboarding_snail.json")
        view.storySnail.playAnimation()

        view.storyNext.dispatchOnClick { OnboardAction.ShowNext }
        view.storySkip.dispatchOnClick { OnboardAction.Skip }
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        view.storyText.movementMethod = ScrollingMovementMethod()
        view.storyText.isVerticalScrollBarEnabled = false
        val anim = TypewriterTextAnimator.of(
            view.storyText,
            stringRes(R.string.onboard_story_p1),
            OnboardViewController.TYPE_SPEED
        )
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                showEvilSnail(view)
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
        super.onDetach(view)
    }

    private fun showEvilSnail(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(view.context, R.layout.controller_onboard_story)

        val transition = AutoTransition()
        transition.interpolator = AnticipateOvershootInterpolator(1.0f)
        transition.duration = 1300
        transition.addListener(object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                showPart2Animation(view)
            }
        })
        TransitionManager.beginDelayedTransition(
            view as ViewGroup,
            transition
        )
        constraintSet.applyTo(view as ConstraintLayout)
    }

    private fun showPart2Animation(view: View) {
        val anim = TypewriterTextAnimator.of(
            view.storyText,
            stringRes(R.string.onboard_story_p2),
            OnboardViewController.TYPE_SPEED
        )
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                showStars(view)
            }
        })
        anim.start()
        animations.add(anim)
    }

    private fun showStars(view: View) {
        view.storyStars.alpha = 0f
        ObjectAnimator
            .ofFloat(view.storyStars, "alpha", 0f, 1f)
            .setDuration(1500)
            .start()
        view.storyStars.setAnimation("onboarding_stars.json")
        view.storyStars.playAnimation()

        val anim = TypewriterTextAnimator.of(
            view.storyText,
            stringRes(R.string.onboard_story_p3),
            OnboardViewController.TYPE_SPEED
        )
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                ViewCompat.setScrollIndicators(
                    view.storyText,
                    ViewCompat.SCROLL_INDICATOR_END
                )
                view.storyText.isVerticalScrollBarEnabled = true
                view.storyText.isVerticalFadingEdgeEnabled = false
            }
        })
        anim.start()
        animations.add(anim)
    }

    override fun render(state: OnboardViewState, view: View) {

    }

}