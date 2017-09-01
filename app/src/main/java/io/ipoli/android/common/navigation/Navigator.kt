package io.ipoli.android.common.navigation

import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.ipoli.android.home.HomeController
import io.ipoli.android.reward.edit.EditRewardController
import io.ipoli.android.reward.list.RewardListController
import io.ipoli.android.store.StoreController
import io.ipoli.android.store.avatars.AvatarListController

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
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

    fun showHome() {
        val handler = FadeChangeHandler()
        router.pushController(RouterTransaction.with(HomeController())
            .pushChangeHandler(handler)
            .popChangeHandler(handler)
        )
    }

    fun showAvatarList() {
        val handler = FadeChangeHandler()
        router.pushController(RouterTransaction.with(AvatarListController())
            .pushChangeHandler(handler)
            .popChangeHandler(handler)
        )
    }

    fun showEditReward(rewardId: String) {
        val handler = FadeChangeHandler()
        router.pushController(RouterTransaction.with(EditRewardController(rewardId))
            .pushChangeHandler(handler)
            .popChangeHandler(handler)
        )
    }
}