package io.ipoli.android.common.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.data.Player
import kotlinx.android.synthetic.main.popup_pet_message.view.*
import space.traversal.kapsule.required
import java.util.concurrent.TimeUnit

data class PetMessageViewState(
    val message: String? = null,
    val petAvatar: PetAvatar? = null
) : BaseViewState()

sealed class PetMessageAction : Action {
    data class Load(val message: String) : PetMessageAction()
    data class PlayerLoaded(val player: Player) : PetMessageAction()
}

object PetMessageSideEffectHandler : AppSideEffectHandler() {

    private val playerRepository by required { playerRepository }

    override suspend fun doExecute(action: Action, state: AppState) {
        if (action is PetMessageAction.Load) {
            dispatch(PetMessageAction.PlayerLoaded(playerRepository.find()!!))
        }
    }

    override fun canHandle(action: Action) = action is PetMessageAction.Load
}

object PetMessageReducer : BaseViewStateReducer<PetMessageViewState>() {

    override val stateKey = key<PetMessageViewState>()

    override fun reduce(
        state: AppState,
        subState: PetMessageViewState,
        action: Action
    ) =
        when (action) {
            is PetMessageAction.Load -> subState.copy(message = action.message)
            is PetMessageAction.PlayerLoaded -> subState.copy(petAvatar = action.player.pet.avatar)
            else -> subState
        }

    override fun defaultState() = PetMessageViewState(message = null, petAvatar = null)
}


class PetMessagePopup(
    private val message: String,
    private val actionListener: () -> Unit = {},
    private val actionText: String = ""
) : ReduxPopup<PetMessageAction, PetMessageViewState, PetMessageReducer>(
    position = ReduxPopup.Position.BOTTOM,
    isAutoHide = true
) {

    override val reducer = PetMessageReducer

    override fun createView(inflater: LayoutInflater): View {
        @SuppressLint("InflateParams")
        val v = inflater.inflate(R.layout.popup_pet_message, null)

        if (actionText.isNotBlank()) {
            v.petAction.text = actionText
        }

        v.petAction.setOnClickListener {
            actionListener()
            hide()
        }

        return v
    }

    override fun onCreateLoadAction() = PetMessageAction.Load(message)

    override fun onViewShown(contentView: View) {
        autoHideAfter(TimeUnit.SECONDS.toMillis(3))
    }

    override fun render(state: PetMessageViewState, view: View) {
        state.message?.let {
            view.petMessage.text = it
        }

        state.petAvatar?.let {
            val androidAvatar = AndroidPetAvatar.valueOf(it.name)
            view.petHead.setImageResource(androidAvatar.headImage)
        }

    }
}