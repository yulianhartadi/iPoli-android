package io.ipoli.android.quest.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.view.BasePopup
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.common.view.visible
import kotlinx.android.synthetic.main.popup_quest_complete.view.*


class QuestCompletePopup(
    private val earnedXP: Int,
    private val earnedCoins: Int
    ) : BasePopup(isAutoHide = true) {

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_quest_complete, null)

    override fun onViewShown(contentView: View) {
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
                autoHideAfter(700)
            }
        })

        anim.start()
    }
}