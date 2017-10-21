package io.ipoli.android.common.mvi

import android.support.annotation.MainThread
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/8/17.
 */

interface ViewStateRenderer<in VS> {
    @MainThread
    fun render(state: VS)
}

interface MviPresenter<in V : ViewStateRenderer<VS>, VS : ViewState, in I : Intent> {

    fun intentChannel(): SendChannel<I>

    var initialState: VS

    fun onAttachView(view: V)

    fun onDetachView() {}

    fun onDestroy() {}
}

abstract class BaseMviPresenter<in V : ViewStateRenderer<VS>, VS : ViewState, I : Intent>(private val coroutineContext: CoroutineContext) : MviPresenter<V, VS, I> {

    private val intentChannel = Channel<I>()

    override lateinit var initialState: VS

    override fun intentChannel() = intentChannel

    protected lateinit var actor: ActorJob<I>

    private fun stateReduceActor(view: V) = actor<I> {
        var state = initialState
        view.render(state)
        channel.consumeEach { intent ->
            state = reduceState(intent, state)
            launch(coroutineContext + UI) {
                view.render(state)
            }
        }
    }

    override fun onAttachView(view: V) {
        actor = stateReduceActor(view)
        launch(coroutineContext + CommonPool) {
            intentChannel.consumeEach {
                actor.send(it)
            }
        }
        launch(coroutineContext + CommonPool) {
            loadStreamingData(actor, initialState)
        }
    }

    open suspend fun loadStreamingData(actor: ActorJob<I>, initialState: VS) {}

    abstract fun reduceState(intent: I, state: VS): VS
}