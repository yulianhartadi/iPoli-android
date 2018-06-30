package io.ipoli.android.common.async

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch

class ChannelRelay<E, in P>(
    private val producer: suspend (Channel<E>, P) -> Unit,
    private val consumer: suspend (E, P) -> Unit
) {

    private val channel = Channel<E>(Channel.CONFLATED)
    private var params: P? = null

    init {
        launch(CommonPool) {
            for (e in channel) {
                consumer(e, params!!)
            }
        }
    }

    fun listen(params: P) {
        this.params = params
        launch(CommonPool) {
            producer(channel, this@ChannelRelay.params!!)
        }
    }
}