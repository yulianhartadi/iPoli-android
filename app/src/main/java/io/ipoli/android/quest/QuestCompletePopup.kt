package io.ipoli.android.quest

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.view.BasePopup
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.common.view.visible
import kotlinx.android.synthetic.main.popup_quest_complete.view.*


class QuestCompletePopup(private val earnedXP: Int) : BasePopup(isAutoHide = true) {

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

        earnedXP.visible = true

        val xpAnim = ValueAnimator.ofInt(0, this.earnedXP)
        xpAnim.duration = 300
        xpAnim.addUpdateListener {
            earnedXP.text = "+ ${it.animatedValue}XP"
        }

        xpAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                autoHideAfter(700)
            }
        })

        xpAnim.start()
    }
}