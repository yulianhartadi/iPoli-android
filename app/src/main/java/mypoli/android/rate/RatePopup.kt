package mypoli.android.rate

import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.amplitude.api.Amplitude
import kotlinx.android.synthetic.main.popup_rate.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.common.view.MviPopup
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import mypoli.android.rate.RateViewState.Type.*
import org.json.JSONObject
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext

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

class RatePresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext
) :
    BaseMviPresenter<ViewStateRenderer<RateViewState>, RateViewState, RateIntent>(
        RateViewState(LOADING),
        coroutineContext
    ) {
    override fun reduceState(intent: RateIntent, state: RateViewState) =
        when (intent) {
            is RateIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.listen(Unit).consumeEach {
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

class RatePopup :
    MviPopup<RateViewState, RatePopup, RatePresenter, RateIntent>(isAutoHide = true) {

    override fun createView(inflater: LayoutInflater): View {
        val view = inflater.inflate(R.layout.popup_rate, null)
        changeTitle(view, R.string.rate_dialog_initial_title)
        view.positive.setText(R.string.dialog_yes)
        view.negative.setText(R.string.dialog_no)
        view.neutral.setText(R.string.rate_dialog_never_ask_again)
        return view
    }

    private val presenter by required { ratePresenter }

    override fun createPresenter() = presenter

    override fun onViewShown(contentView: View) {
        send(RateIntent.LoadData)
    }

    override fun render(state: RateViewState, view: View) {
        val neutral = view.neutral
        val negative = view.negative
        val positive = view.positive

        when (state.type) {
            DATA_CHANGED -> {
                view.rateDialogHeaderIcon.setImageResource(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                neutral.visibility = View.VISIBLE

                positive.setOnClickListener {
                    logEvent("rate_initial", "answer", "yes")
                    send(RateIntent.ShowRate)
                }
                negative.setOnClickListener {
                    logEvent("rate_initial", "answer", "no")
                    send(RateIntent.ShowFeedback)
                }
                neutral.setOnClickListener {
                    logEvent("rate_initial", "answer", "never")
                    saveDoNotShowAgainPref(view.context)
                    hide()
                }
            }

            SHOW_FEEDBACK -> {
                neutral.visibility = View.INVISIBLE
                changeTitle(view, R.string.rate_dialog_feedback_title)
                ViewUtils.goneViews(view.rate)
                ViewUtils.showViews(view.feedbackLayout)
                positive.setText(R.string.rate_dialog_feedback_send)
                negative.setText(R.string.rate_dialog_feedback_no)

                positive.setOnClickListener {
                    val feedback = view.feedback.text.toString()
                    if (feedback.isNotEmpty()) {
                        logEvent("rate_negative", "feedback", feedback)
                        Toast.makeText(view.context, R.string.thank_you, Toast.LENGTH_SHORT).show()
                    }
                    hide()
                }
                negative.setOnClickListener {
                    logEvent("rate_negative", "answer", "no")
                    hide()
                }

                view.viewSwitcher.showNext()
            }

            SHOW_RATE -> {
                neutral.visibility = View.INVISIBLE
                changeTitle(view, R.string.rate_dialog_rate_title)
                ViewUtils.goneViews(view.feedbackLayout)
                ViewUtils.showViews(view.rate)
                positive.setText(R.string.dialog_lets_go)
                negative.setText(R.string.dialog_later)
                positive.setOnClickListener {
                    logEvent("rate_positive", "answer", "yes")
                    saveDoNotShowAgainPref(view.context)
                    val uri = Uri.parse("market://details?id=" + view.context.packageName)
                    val linkToMarket =
                        android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                    view.context.startActivity(linkToMarket)
                    hide()
                }
                negative.setOnClickListener {
                    logEvent("rate_positive", "answer", "no")
                    hide()
                }
                view.viewSwitcher.showNext()
            }
        }
    }

    private fun logEvent(name: String, paramName: String, paramValue: String) {
        Amplitude.getInstance().logEvent(
            name,
            JSONObject().put(paramName, paramValue)
        )
    }

    private fun changeTitle(view: View, @StringRes title: Int) {
        view.rateDialogHeaderTitle.setText(title)
    }

    private fun saveDoNotShowAgainPref(context: Context) {
        val pm = PreferenceManager.getDefaultSharedPreferences(context)
        pm.edit().putBoolean(Constants.KEY_SHOULD_SHOW_RATE_DIALOG, false).apply()
    }
}