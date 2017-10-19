package io.ipoli.android.common.navigation

import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.ipoli.android.home.HomeController

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/2/17.
 */
class Navigator(private val router: Router) {

    fun showHome() {
        val handler = FadeChangeHandler()
        router.pushController(RouterTransaction.with(HomeController())
            .pushChangeHandler(handler)
            .popChangeHandler(handler)
        )
    }
}