package io.ipoli.android.quest.view

import android.animation.*
import android.support.transition.AutoTransition
import android.support.transition.Transition
import android.support.transition.TransitionListenerAdapter
import android.support.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import io.ipoli.android.R
import io.ipoli.android.common.view.BasePopup
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.common.view.visible
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.Food
import kotlinx.android.synthetic.main.popup_quest_complete.view.*

class QuestCompletePopup(
    private val earnedXP: Int,
    private val earnedCoins: Int,
    private val bounty: Food? = null,
    private val avatar: AndroidPetAvatar
) : BasePopup(isAutoHide = true) {

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_quest_complete, null)

    override fun onViewShown(contentView: View) {
        contentView.pet.setImageResource(avatar.headImage)
        bounty?.let {
            contentView.bounty.setImageResource(it.image)
        }
        startTypingAnimation(contentView)
    }

    private fun startTypingAnimation(contentView: View) {
        val title = contentView.title

        val typewriterAnim = TypewriterTextAnimator.of(title, "Quest Complete")
        typewriterAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startEarnedRewardAnimation(contentView)
            }
        })
        typewriterAnim.start()
    }

    private fun startEarnedRewardAnimation(contentView: View) {
        val earnedXP = contentView.earnedXP
        val earnedCoins = contentView.earnedCoins

        earnedXP.visible = true
        earnedCoins.visible = true

        val xpAnim = ValueAnimator.ofInt(0, this.earnedXP)
        xpAnim.addUpdateListener {
            earnedXP.text = "+ ${it.animatedValue} XP"
        }

        val coinsAnim = ValueAnimator.ofInt(0, this.earnedCoins)
        coinsAnim.addUpdateListener {
            earnedCoins.text = "+ ${it.animatedValue} life coins"
        }

        val anim = AnimatorSet()
        anim.duration = 300
        anim.playTogether(xpAnim, coinsAnim)

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (bounty != null) playRewardAnimation(contentView)
                else autoHideAfter(700)
            }
        })

        anim.start()
    }

    private fun playRewardAnimation(contentView: View) {
        val transition = AutoTransition()
        transition.addListener(object : TransitionListenerAdapter() {

            override fun onTransitionEnd(transition: Transition) {
                val xAnim = ObjectAnimator.ofFloat(contentView.bounty, "scaleX", 0f, 1f)
                val yAnim = ObjectAnimator.ofFloat(contentView.bounty, "scaleY", 0f, 1f)
                val set = AnimatorSet()
                set.interpolator = OvershootInterpolator()
                set.playTogether(xAnim, yAnim)
                set.addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationStart(animation: Animator?) {
                        contentView.bounty.visible = true
                        contentView.bountyQuantity.visible = true
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        val fadeAnim = ObjectAnimator.ofFloat(contentView.bountyQuantity, "alpha", 0f, 1f)
                        fadeAnim.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                autoHideAfter(700)
                            }
                        })
                        fadeAnim.start()
                    }
                })
                set.start()
            }
        })


        TransitionManager.beginDelayedTransition(contentView as ViewGroup, transition)
        contentView.bounty.visible = false
        contentView.bountyQuantity.visible = false
    }
}