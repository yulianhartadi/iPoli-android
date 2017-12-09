package io.ipoli.android.common.mvi

import android.support.annotation.MainThread
import com.amplitude.api.Amplitude
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/8/17.
 */

interface ViewStateRenderer<in VS> {
    @MainThread
    fun render(state: VS)
}

interface MviPresenter<in V : ViewStateRenderer<VS>, in VS : ViewState, in I : Intent> {

    fun intentChannel(): SendChannel<I>

    fun onAttachView(view: V)

    fun onDetachView() {}

    fun onDestroy() {}
}

abstract class BaseMviPresenter<in V : ViewStateRenderer<VS>, VS : ViewState, I : Intent>(
    private val initialState: VS,
    private val coroutineContext: CoroutineContext
) : MviPresenter<V, VS, I> {

    private val intentChannel = Channel<I>()

    override fun intentChannel() = intentChannel

    protected lateinit var sendChannel: SendChannel<I>

    private fun stateReduceActor(view: V) = actor<I>(coroutineContext + CommonPool, Channel.CONFLATED) {
        var state = initialState
        renderInitialState(view)
        launch(coroutineContext + UI) {
            channel.consumeEach { intent ->

                val oldState = state
                state = reduceState(intent, state)

                val data = JSONObject()
                data.put("previous_state", oldState)
                data.put("intent", intent)
                data.put("new_state", state)
                Amplitude.getInstance().logEvent("change_state", data)

                Timber.d("new state $state")
                view.render(state)
            }
        }
    }

    private fun renderInitialState(view: V) {
        val data = JSONObject()
        data.put("initial", initialState)
        Amplitude.getInstance().logEvent("change_state", data)
        Timber.d("initial state $initialState")
        view.render(initialState)
    }

    override fun onAttachView(view: V) {

        sendChannel = stateReduceActor(view)
        launch(coroutineContext + CommonPool) {
            intentChannel.consumeEach {
                sendChannel.send(it)
            }
        }
    }

    abstract fun reduceState(intent: I, state: VS): VS

    override fun onDestroy() {
        sendChannel.close()
    }
}