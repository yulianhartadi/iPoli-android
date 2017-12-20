package mypoli.android.common.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.amplitude.api.Amplitude
import kotlinx.android.synthetic.main.dialog_rate.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.common.view.RateViewState.Type.*
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import org.json.JSONObject
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext


/**
 * Created by vini on 12/20/17.
 */

sealed class RateIntent : Intent {
    object LoadData : RateIntent()
    data class ChangePlayer(val player: Player) : RateIntent()
    object ShowFeedback : RateIntent()
    object ShowRate : RateIntent()
}


data class RateViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null
) : ViewState {
    enum class Type {
        LOADING,
        DATA_CHANGED,
        SHOW_FEEDBACK,
        SHOW_RATE
    }
}

class RateDialogPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext) :
    BaseMviPresenter<ViewStateRenderer<RateViewState>, RateViewState, RateIntent>(
        RateViewState(LOADING),
        coroutineContext
    ) {
    override fun reduceState(intent: RateIntent, state: RateViewState) =
        when (intent) {
            is RateIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(RateIntent.ChangePlayer(it))
                    }
                }
                state
            }

            is RateIntent.ChangePlayer -> {
                state.copy(
                    type = DATA_CHANGED,
                    petAvatar = intent.player.pet.avatar
                )
            }

            is RateIntent.ShowFeedback -> {
                state.copy(
                    type = SHOW_FEEDBACK
                )
            }

            is RateIntent.ShowRate -> {
                state.copy(
                    type = SHOW_RATE
                )
            }
        }

}

class RateDialogController :
    MviDialogController<RateViewState, RateDialogController, RateDialogPresenter, RateIntent>() {

    private val presenter by required { rateDialogPresenter }

    override fun createPresenter() = presenter

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(RateIntent.LoadData)
    }

    override fun render(state: RateViewState, view: View) {
        when (state.type) {
            DATA_CHANGED -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).visibility = View.VISIBLE
                setPositiveButtonListener {
                    send(RateIntent.ShowRate)
                }
                setNegativeButtonListener {
                    send(RateIntent.ShowFeedback)
                }
                setNeutralButtonListener {
                    //never ask again
                }
            }

            SHOW_FEEDBACK -> {
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).visibility = View.INVISIBLE
                changeTitle(R.string.rate_dialog_feedback_title)
                ViewUtils.goneViews(view.rate)
                ViewUtils.showViews(view.feedbackLayout)
                changePositiveButtonText(R.string.rate_dialog_feedback_send)
                changeNegativeButtonText(R.string.rate_dialog_feedback_no)
                setPositiveButtonListener {
                    val feedback = view.feedback.text.toString()
                    if (feedback.isNotEmpty()) {
                        Amplitude.getInstance().logEvent("rate_feedback",
                            JSONObject().put("feedback", feedback))
                        Toast.makeText(activity!!, "Thank you!", Toast.LENGTH_SHORT).show()
                    }
                    dismissDialog()
                }
                setNegativeButtonListener(null)

                view.container.showNext()
            }

            SHOW_RATE -> {
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).visibility = View.INVISIBLE
                changeTitle(R.string.rate_dialog_rate_title)
                ViewUtils.goneViews(view.feedbackLayout)
                ViewUtils.showViews(view.rate)
                changePositiveButtonText(R.string.dialog_great)
                changeNegativeButtonText(R.string.dialog_later)
                setPositiveButtonListener {
                    val uri = Uri.parse("market://details?id=" + activity!!.packageName)
                    val linkToMarket = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                    startActivity(linkToMarket)
                }
                setNegativeButtonListener(null)
                view.container.showNext()
            }
        }
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_rate, null)
    }

    override fun onCreateDialog(dialogBuilder: AlertDialog.Builder, contentView: View, savedViewState: Bundle?): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.dialog_yes, null)
            .setNegativeButton(R.string.dialog_no, null)
            .setNeutralButton(R.string.rate_dialog_never_ask_again, null)
            .create()

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.rate_dialog_initial_title)
    }
}