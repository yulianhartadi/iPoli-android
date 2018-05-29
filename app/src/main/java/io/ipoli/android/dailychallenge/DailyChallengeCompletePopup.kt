package io.ipoli.android.dailychallenge

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.support.transition.ChangeBounds
import android.support.transition.Transition
import android.support.transition.TransitionListenerAdapter
import android.support.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.view.Popup
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.common.view.gone
import io.ipoli.android.common.view.invisible
import io.ipoli.android.common.view.visible
import kotlinx.android.synthetic.main.popup_daily_challenge_complete.view.*

class DailyChallengeCompletePopup(private val earnedXP: Int, private val earnedCoins: Int) :
    Popup() {

    override fun createView(inflater: LayoutInflater): View {
        val v = inflater.inflate(R.layout.popup_daily_challenge_complete, null)
        v.starsAnimation.setAnimation("daily_challenge_stars.json")

        v.giftAnimation.setAnimation("daily_challenge_gift.json")

        return v
    }

    override fun onViewShown(contentView: View) {
        val anim = TypewriterTextAnimator.of(contentView.title, "Daily Challenge Complete")

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                contentView.starsAnimation.visible()
                contentView.starsAnimation.playAnimation()
            }
        })

        contentView.starsAnimation.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                contentView.giftAnimation.visible()
                contentView.giftAnimation.setMaxProgress(0.45f)
                contentView.giftAnimation.playAnimation()
            }
        })

        contentView.giftAnimation.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                contentView.giftAnimation.setOnClickListener {
                    contentView.giftAnimation.removeAllAnimatorListeners()
                    contentView.giftAnimation.addAnimatorListener(object :
                        AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            transitionToRewards(contentView)
                        }
                    })
                    contentView.giftAnimation.setMinAndMaxProgress(0.45f, 1f)
                    contentView.giftAnimation.resumeAnimation()
                    contentView.giftHint.invisible()
                }
                contentView.giftHint.visible()
            }
        })

        anim.start()
    }

    private fun transitionToRewards(contentView: View) {
        val transition = ChangeBounds()
        transition.addListener(object : TransitionListenerAdapter() {
            override fun onTransitionStart(transition: Transition) {
                animateRewards(contentView)
            }
        })

        TransitionManager.beginDelayedTransition(contentView as ViewGroup, transition)
        contentView.giftAnimation.gone()
        contentView.giftHint.gone()
    }

    private fun animateRewards(contentView: View) {
        val xpAnim = ValueAnimator.ofInt(0, earnedXP)
        xpAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                contentView.xpReward.visible()
                contentView.xpIcon.visible()
            }
        })
        xpAnim.addUpdateListener {
            contentView.xpReward.text = "${it.animatedValue}"
        }

        val coinsAnim = ValueAnimator.ofInt(0, earnedCoins)

        coinsAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                contentView.coinReward.visible()
                contentView.coinIcon.visible()
            }

            override fun onAnimationEnd(animation: Animator?) {
                contentView.doneButton.visible()
                contentView.doneButton.setOnClickListener {
                    hide()
                }
            }
        })

        coinsAnim.addUpdateListener {
            contentView.coinReward.text = "${it.animatedValue}"
        }

        val anim = AnimatorSet()
        anim.duration =
            contentView.resources
                .getInteger(android.R.integer.config_mediumAnimTime)
                .toLong()
        anim.playSequentially(xpAnim, coinsAnim)
        anim.start()
    }

}