package mypoli.android.common.view

import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.popup_pet_message.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.R
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import space.traversal.kapsule.required
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

data class PetMessageViewState(
    val message: String? = null,
    val avatar: PetAvatar? = null
) : ViewState

sealed class PetMessageIntent : Intent {
    data class LoadData(val message: String) : PetMessageIntent()
    data class ChangePlayer(val player: Player) : PetMessageIntent()
}

class PetMessagePresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PetMessageViewState>, PetMessageViewState, PetMessageIntent>(
    PetMessageViewState(),
    coroutineContext
) {
    override fun reduceState(intent: PetMessageIntent, state: PetMessageViewState) =
        when (intent) {
            is PetMessageIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.listen(Unit).consumeEach {
                        sendChannel.send(PetMessageIntent.ChangePlayer(it))
                    }
                }
                state.copy(
                    message = intent.message
                )
            }

            is PetMessageIntent.ChangePlayer -> {
                state.copy(
                    avatar = intent.player.pet.avatar
                )
            }
        }

}


class PetMessagePopup(
    private val message: String,
    private val undoListener: () -> Unit
) : MviPopup<PetMessageViewState, PetMessagePopup, PetMessagePresenter, PetMessageIntent>(
    position = MviPopup.Position.BOTTOM,
    isAutoHide = true
) {

    private val presenter by required { petMessagePresenter }

    override fun createPresenter() = presenter

    override fun render(state: PetMessageViewState, view: View) {
        state.message?.let {
            view.petMessage.text = it
        }

        state.avatar?.let {
            val androidAvatar = AndroidPetAvatar.valueOf(it.name)
            view.petHead.setImageResource(androidAvatar.headImage)
        }

    }

    override fun createView(inflater: LayoutInflater): View {
        val v = inflater.inflate(R.layout.popup_pet_message, null)

        v.undoAction.setOnClickListener {
            undoListener()
            hide()
        }

        return v
    }


    override fun onViewShown(contentView: View) {
        send(PetMessageIntent.LoadData(message))
        autoHideAfter(TimeUnit.SECONDS.toMillis(2))
    }
}