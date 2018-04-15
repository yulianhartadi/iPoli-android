package io.ipoli.android.common.async

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

class ChannelRelay<E, in P>(
    private val producer: suspend (Channel<E>, P) -> Unit,
    private val consumer: suspend (E, P) -> Unit
) {

    private val channel = Channel<E>()
    private var params: P? = null

    init {
        launch(CommonPool) {
            channel.consumeEach {
                consumer(it, params!!)
            }
        }
    }

    fun listen(params: P) {
        this.params = params
        launch(UI) {
            producer(channel, this@ChannelRelay.params!!)
        }
    }
}