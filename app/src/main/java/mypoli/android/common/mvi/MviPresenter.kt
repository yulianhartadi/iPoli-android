package mypoli.android.common.mvi

import android.support.annotation.MainThread
import android.util.Log
import com.amplitude.api.Amplitude
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
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

    private val receiveChannels = mutableListOf<ReceiveChannel<*>>()

    private fun stateReduceActor(view: V) =
        actor<I>(coroutineContext + CommonPool, Channel.CONFLATED) {
            var state = initialState

            launch(coroutineContext + UI) {

                renderInitialState(view)

                channel.consumeEach { intent ->
                    try {
                        val oldState = state
                        state = reduceState(intent, state)

                        longIntentChange(oldState, intent, state)
                        view.render(state)
                    } catch (e: Throwable) {
                        Timber.e(e, "From presenter ${this@BaseMviPresenter}")
                        Crashlytics.logException(
                            RuntimeException(
                                "From presenter ${this@BaseMviPresenter} for view $view",
                                e
                            )
                        )

                        val data = JSONObject()
                        data.put("message", e.message)
                        data.put("stack_trace", Log.getStackTraceString(e))

                        Amplitude.getInstance().logEvent("app_error", data)
                    }
                }
            }
        }

    private fun longIntentChange(oldState: VS, intent: I, state: VS) {
        val data = JSONObject()
        data.put("previous_state", oldState)
        data.put("intent", intent)
        data.put("new_state", state)
        val eventType = "intent_${intent::class.java.name}"
        Amplitude.getInstance().logEvent(eventType, data)

        Timber.d("Intent $intent new state $state")
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

    protected fun <D> addReceiveChannel(channel: ReceiveChannel<D>): ReceiveChannel<D> {
        receiveChannels += channel
        return channel
    }

    private fun stopReceiveChannels() {
        for (c in receiveChannels) {
            if (!c.isClosedForReceive) {
                c.cancel()
            }
        }
    }

    override fun onDetachView() {
        stopReceiveChannels()
        if (!sendChannel.isClosedForSend) {
            sendChannel.close()
        }
    }

    abstract fun reduceState(intent: I, state: VS): VS

    override fun onDestroy() {
        stopReceiveChannels()
        if (!sendChannel.isClosedForSend) {
            sendChannel.close()
        }
    }
}