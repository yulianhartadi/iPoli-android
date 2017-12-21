package mypoli.android.common.view

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.R
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.common.view.CurrencyConverterIntent.ChangePlayer
import mypoli.android.common.view.CurrencyConverterIntent.LoadData
import mypoli.android.common.view.CurrencyConverterViewState.Type.DATA_CHANGED
import mypoli.android.common.view.CurrencyConverterViewState.Type.LOADING
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/21/17.
 */

sealed class CurrencyConverterIntent : Intent {
    object LoadData : CurrencyConverterIntent()
    data class ChangePlayer(val player: Player) : CurrencyConverterIntent()
}

data class CurrencyConverterViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null
) : ViewState {
    enum class Type {
        LOADING,
        DATA_CHANGED
    }
}

class CurrencyConverterPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext) :
    BaseMviPresenter<ViewStateRenderer<CurrencyConverterViewState>, CurrencyConverterViewState, CurrencyConverterIntent>(
        CurrencyConverterViewState(LOADING),
        coroutineContext
    ) {
    override fun reduceState(intent: CurrencyConverterIntent, state: CurrencyConverterViewState) =
        when (intent) {
            is CurrencyConverterIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(ChangePlayer(it))
                    }
                }
                state
            }

            is CurrencyConverterIntent.ChangePlayer -> {
                val player = intent.player

                state.copy(
                    type = DATA_CHANGED,
                    petAvatar = player.pet.avatar
                )
            }
        }
}

class CurrencyConverterController :
    MviDialogController<CurrencyConverterViewState, CurrencyConverterController, CurrencyConverterPresenter, CurrencyConverterIntent>() {

    private val presenter by required { currencyConverterPresenter }

    override fun createPresenter() = presenter

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadData)
    }

    override fun render(state: CurrencyConverterViewState, view: View) {
        when (state.type) {
            DATA_CHANGED -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
            }
        }
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_currency_converter, null)
    }

    override fun onCreateDialog(dialogBuilder: AlertDialog.Builder, contentView: View, savedViewState: Bundle?): AlertDialog =
        dialogBuilder
            .setNegativeButton("Cancel", null)
            .create()

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.currency_converter_title)
    }
}