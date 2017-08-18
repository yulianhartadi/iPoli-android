package io.ipoli.android.common.navigation

import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.ipoli.android.reward.RewardListController
import io.ipoli.android.store.StoreController

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/17.
 */
class Navigator(private val router: Router) {

    fun showRewardsList() {
        val handler = FadeChangeHandler()
        router.pushController(RouterTransaction.with(RewardListController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }

    fun showStore() {
        val handler = FadeChangeHandler()
        router.pushController(RouterTransaction.with(StoreController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }
}