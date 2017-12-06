package io.ipoli.android.player.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.view.BasePopup
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.common.view.visible
import kotlinx.android.synthetic.main.popup_level_up.view.*

class LevelUpPopup(private val level: Int) : BasePopup() {

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_level_up, null)

    override fun onViewShown(contentView: View) {
        startTypingAnimation(contentView)
    }

    private fun startTypingAnimation(contentView: View) {
        val title = contentView.title

        val typewriterTitleAnim = TypewriterTextAnimator.of(title, "You reached new level")
        val animSet = AnimatorSet()
        animSet.playSequentially(typewriterTitleAnim, levelBadgeAnimation(contentView),
            claimRewardAnimation(contentView))
        animSet.start()

        val rewardUrl = Constants.LEVEL_UP_REWARDS[(level - 2) % Constants.LEVEL_UP_REWARDS.size]
        contentView.claimReward.setOnClickListener {
            val reward = contentView.bounty as ImageView
            Glide.with(contentView.context)
                .load(rewardUrl)
                .into(reward)
            contentView.viewSwitcher.showNext()
        }
    }

    private fun levelBadgeAnimation(contentView: View): Animator {
        val badge = contentView.badge
        val levelView = contentView.playerLevel
        levelView.text = level.toString()

        val fadeIn = ObjectAnimator.ofFloat(badge, "alpha", 0f, 1f)
        val scaleLevelX = ObjectAnimator.ofFloat(levelView, "scaleX", 1.5f, 1f)
        val scaleLevelY = ObjectAnimator.ofFloat(levelView, "scaleY", 1.5f, 1f)

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
                levelView.visible = true
            }
        })

        return anim
    }

    private fun claimRewardAnimation(contentView: View): Animator {
        val anim = ObjectAnimator.ofFloat(contentView.claimReward, "alpha", 0f, 1f)
        anim.duration = 300
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                contentView.claimReward.visible = true
            }
        })
        return anim
    }
}