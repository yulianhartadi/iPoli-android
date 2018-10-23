package io.ipoli.android.challenge.complete

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.support.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.entity.SharingPreference
import io.ipoli.android.common.IntentUtil
import io.ipoli.android.common.view.Popup
import io.ipoli.android.common.view.visible
import kotlinx.android.synthetic.main.popup_challenge_complete.view.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/15/2018.
 */
class CompleteChallengePopup(private val challenge: Challenge) : Popup(position = Position.TOP) {

    @SuppressLint("InflateParams")
    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_challenge_complete, null)

    override fun onViewShown(contentView: View) {
        contentView.trophyAnimation.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                TransitionManager.beginDelayedTransition(contentView.container)
                contentView.secondCard.visible()
                contentView.challengeName.visible()

                contentView.challengeName.text = challenge.name
                contentView.challengeXP.text = "+${challenge.reward!!.experience} experience"
                contentView.challengeCoins.text = "+${challenge.reward.coins} life coins"
            }
        })
        contentView.trophyAnimation.playAnimation()
        contentView.sweet.setOnClickListener {
            if (challenge.sharingPreference == SharingPreference.FRIENDS) {
                val ctx = contentView.context
                ctx.startActivity(
                    IntentUtil.showAddChallengePost(
                        challengeId = challenge.id,
                        context = ctx
                    )
                )
            }
            hide()
        }
    }
}