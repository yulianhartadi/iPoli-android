package mypoli.android.quest.view

import android.animation.*
import android.support.transition.AutoTransition
import android.support.transition.Transition
import android.support.transition.TransitionListenerAdapter
import android.support.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import kotlinx.android.synthetic.main.popup_quest_complete.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.R
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.common.view.MviPopup
import mypoli.android.common.view.anim.TypewriterTextAnimator
import mypoli.android.common.view.visible
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.Food
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext

data class QuestCompleteViewState(
    val xp: Int? = null,
    val coins: Int? = null,
    val bounty: Food? = null,
    val avatar: PetAvatar? = null
) : ViewState

sealed class QuestCompleteIntent : Intent {
    data class LoadData(val xp: Int, val coins: Int, val bounty: Food?) : QuestCompleteIntent()
    data class ChangePlayer(val player: Player) : QuestCompleteIntent()
}

class QuestCompletePresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<QuestCompleteViewState>, QuestCompleteViewState, QuestCompleteIntent>(
    QuestCompleteViewState(),
    coroutineContext
) {
    override fun reduceState(intent: QuestCompleteIntent, state: QuestCompleteViewState) =
        when (intent) {
            is QuestCompleteIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(QuestCompleteIntent.ChangePlayer(it))
                    }
                }
                state.copy(
                    xp = intent.xp,
                    coins = intent.coins,
                    bounty = intent.bounty
                )
            }

            is QuestCompleteIntent.ChangePlayer -> {
                val player = intent.player
                state.copy(
                    avatar = player.pet.avatar
                )
            }
        }
}

class QuestCompletePopup(
    private val earnedXP: Int,
    private val earnedCoins: Int,
    private val bounty: Food? = null
) : MviPopup<QuestCompleteViewState, ViewStateRenderer<QuestCompleteViewState>, QuestCompletePresenter, QuestCompleteIntent>(
    isAutoHide = true
) {

    private val presenter by required { questCompletePresenter }

    override fun createPresenter() = presenter

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_quest_complete, null)

    override fun onViewShown(contentView: View) {
        send(QuestCompleteIntent.LoadData(earnedXP, earnedCoins, bounty))
    }

    override fun render(state: QuestCompleteViewState, view: View) {
        state.avatar?.let {
            val androidAvatar = AndroidPetAvatar.valueOf(it.name)
            view.pet.setImageResource(androidAvatar.headImage)
            state.bounty?.let {
                view.bounty.setImageResource(it.image)
            }
            startTypingAnimation(view, state)
        }
    }

    private fun startTypingAnimation(contentView: View, state: QuestCompleteViewState) {
        val title = contentView.title

        val typewriterAnim = TypewriterTextAnimator.of(title, "Quest Complete")
        typewriterAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startEarnedRewardAnimation(contentView, state)
            }
        })
        typewriterAnim.start()
    }

    private fun startEarnedRewardAnimation(contentView: View, state: QuestCompleteViewState) {
        val earnedXP = contentView.earnedXP
        val earnedCoins = contentView.earnedCoins

        earnedXP.visible = true
        earnedCoins.visible = true

        val xpAnim = ValueAnimator.ofInt(0, state.xp!!)
        xpAnim.addUpdateListener {
            earnedXP.text = "+ ${it.animatedValue} XP"
        }

        val coinsAnim = ValueAnimator.ofInt(0, state.coins!!)
        coinsAnim.addUpdateListener {
            earnedCoins.text = "+ ${it.animatedValue} Life Coins"
        }

        val anim = AnimatorSet()
        anim.duration = 300
        anim.playTogether(xpAnim, coinsAnim)

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (state.bounty != null) playRewardAnimation(contentView)
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
                        val fadeAnim =
                            ObjectAnimator.ofFloat(contentView.bountyQuantity, "alpha", 0f, 1f)
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