package mypoli.android.challenge.complete

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.popup_challenge_complete.view.*
import mypoli.android.R
import mypoli.android.challenge.entity.Challenge
import mypoli.android.common.view.ReduxPopup
import mypoli.android.common.view.visible

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/15/2018.
 */
class CompleteChallengePopup(private val challenge: Challenge) :
    ReduxPopup<CompleteChallengeAction, CompleteChallengeViewState, CompleteChallengeReducer>() {

    override val reducer get() = CompleteChallengeReducer

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_challenge_complete, null)

    override fun onViewShown(contentView: View) {
        contentView.trophyAnimation.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                TransitionManager.beginDelayedTransition(contentView.container)
                contentView.secondCard.visible()
                contentView.challengeName.visible()

                contentView.challengeName.text = challenge.name
                contentView.challengeXP.text = "+${challenge.experience!!} experience"
                contentView.challengeCoins.text = "+${challenge.coins!!} life coins"
            }
        })
        contentView.trophyAnimation.playAnimation()
        contentView.sweet.setOnClickListener { hide() }
    }

    override fun render(state: CompleteChallengeViewState, view: View) {

    }
}