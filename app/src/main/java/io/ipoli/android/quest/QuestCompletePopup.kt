package io.ipoli.android.quest

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.common.view.visible
import io.ipoli.android.reminder.view.BasePopup
import kotlinx.android.synthetic.main.popup_quest_complete.view.*


class QuestCompletePopup(private val earnedXP: Int) : BasePopup() {

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_quest_complete, null)

    override fun playEnterAnimation(contentView: View) {
        val container = contentView.contentContainer
        val transAnim = ObjectAnimator.ofFloat(container, "y", getScreenHeight(container.context).toFloat(), container.y)
        val fadeAnim = ObjectAnimator.ofFloat(container, "alpha", 0f, 1f)
        transAnim.duration = contentView.context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        fadeAnim.duration = contentView.context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        val animSet = AnimatorSet()
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                container.visible = true
            }

            override fun onAnimationEnd(animation: Animator) {
                startTypingAnimation(contentView)
            }
        })
        animSet.playTogether(transAnim, fadeAnim)
        animSet.start()
    }

    private fun startTypingAnimation(contentView: View) {
        val title = contentView.title

        val typewriterAnim = TypewriterTextAnimator.of(title, "Quest Complete")
        typewriterAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                startEarnedRewardAnimation(contentView)
            }
        })
        typewriterAnim.start()
    }

    private fun startEarnedRewardAnimation(contentView: View) {
        val earnedXP = contentView.earnedXP

        val scaleX = ObjectAnimator.ofFloat(earnedXP, "scaleX", 1f, 1.6f, 1f)
        val scaleY = ObjectAnimator.ofFloat(earnedXP, "scaleY", 1f, 1.6f, 1f)
        val scaleSet = AnimatorSet()
        scaleSet.interpolator = LinearOutSlowInInterpolator()
        scaleSet.playTogether(scaleX, scaleY)

        earnedXP.text = "+ ${this.earnedXP}XP"
        earnedXP.visible = true
        scaleSet.start()
    }
}