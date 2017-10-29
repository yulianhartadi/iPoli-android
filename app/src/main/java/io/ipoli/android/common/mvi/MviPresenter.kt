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

    protected lateinit var actor: ActorJob<I>

    private fun stateReduceActor(view: V) = actor<I>(coroutineContext + CommonPool, Channel.CONFLATED) {
        var state = initialState
//        launch(coroutineContext + UI) {
//            view.render(initialState)
//        }
        launch(coroutineContext + UI) {
            channel.consumeEach { intent ->

                state = reduceState(intent, state)
//            Timber.d("AAA " + Looper.getMainLooper().isCurrentThread)

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
    }

    abstract fun reduceState(intent: I, state: VS): VS
}