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
import io.ipoli.android.common.view.BasePopup
import io.ipoli.android.common.view.RevealAnimator
import kotlinx.android.synthetic.main.popup_level_up.view.*


class LevelUpPopup(private val earnedXP: Int) : BasePopup() {

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_level_up, null)

    override fun onEnterAnimationEnd(contentView: View) {
        startTypingAnimation(contentView)
    }

    private fun startTypingAnimation(contentView: View) {
        val title = contentView.title

        val typewriterTitleAnim = TypewriterTextAnimator.of(title, "You reached new level")
        val animSet = AnimatorSet()
        animSet.playSequentially(typewriterTitleAnim, levelBadgeAnimation(contentView),
            claimRewardAnimation(contentView))

        animSet.start()
    }

    private fun levelBadgeAnimation(contentView: View): Animator {
        val badge = contentView.badge
        val level = contentView.level


        val fadeIn = ObjectAnimator.ofFloat(badge, "alpha", 0f, 1f)
        val scaleLevelX = ObjectAnimator.ofFloat(level, "scaleX", 1.5f, 1f)
        val scaleLevelY = ObjectAnimator.ofFloat(level, "scaleY", 1.5f, 1f)

        val scaleAnim = AnimatorSet()
        scaleAnim.playTogether(scaleLevelX, scaleLevelY)

        val anim = AnimatorSet()
        anim.interpolator = LinearOutSlowInInterpolator()
        anim.playSequentially(fadeIn, scaleAnim)
        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                badge.visible = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                level.visible = true
            }
        })

        return anim
    }

    private fun claimRewardAnimation(contentView: View): Animator {
        val anim = RevealAnimator().create(contentView.button2)
        anim.duration = 1000
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                contentView.button2.visible = true
            }
        })
        return anim
    }
}