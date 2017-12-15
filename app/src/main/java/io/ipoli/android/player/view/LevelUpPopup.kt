package io.ipoli.android.player.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.amplitude.api.Amplitude
import com.bumptech.glide.Glide
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.MviPopup
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.common.view.visible
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.Player
import io.ipoli.android.player.usecase.ListenForPlayerChangesUseCase
import kotlinx.android.synthetic.main.popup_level_up.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext


data class LevelUpViewState(
    val level: Int? = null,
    val avatar: PetAvatar? = null
) : ViewState

sealed class LevelUpIntent : Intent {
    object LoadData : LevelUpIntent()
    data class ChangePlayer(val player: Player) : LevelUpIntent()
}

class LevelUpPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext) : BaseMviPresenter<ViewStateRenderer<LevelUpViewState>, LevelUpViewState, LevelUpIntent>(
    LevelUpViewState(),
    coroutineContext
) {
    override fun reduceState(intent: LevelUpIntent, state: LevelUpViewState) =
        when (intent) {
            is LevelUpIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(LevelUpIntent.ChangePlayer(it))
                    }
                }
                state
            }

            is LevelUpIntent.ChangePlayer -> {
                val player = intent.player
                state.copy(
                    level = player.level,
                    avatar = player.pet.avatar
                )
            }
        }

}

class LevelUpPopup : MviPopup<LevelUpViewState, LevelUpPopup, LevelUpPresenter, LevelUpIntent>() {

    private val presenter by required { levelUpPresenter }

    override fun createPresenter() = presenter

    override fun render(state: LevelUpViewState, view: View) {
        state.avatar?.let {
            val androidAvatar = AndroidPetAvatar.valueOf(it.name)
            view.pet.setImageResource(androidAvatar.headImage)
            val data = JSONObject()
            data.put("level", state.level)
            view.positive.setOnClickListener {
                data.put("sentiment", "positive")
                Amplitude.getInstance().logEvent("reward_response", data)
                hide()
            }
            view.negative.setOnClickListener {
                data.put("sentiment", "negative")
                Amplitude.getInstance().logEvent("reward_response", data)
                hide()
            }
            startTypingAnimation(view, state.level!!)
        }
    }

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_level_up, null)

    override fun onViewShown(contentView: View) {
        send(LevelUpIntent.LoadData)
    }

    private fun startTypingAnimation(contentView: View, level: Int) {
        val title = contentView.title

        val typewriterTitleAnim = TypewriterTextAnimator.of(title, "You reached new level")
        val animSet = AnimatorSet()
        animSet.playSequentially(typewriterTitleAnim, levelBadgeAnimation(contentView, level),
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

    private fun levelBadgeAnimation(contentView: View, level: Int): Animator {
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