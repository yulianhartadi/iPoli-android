package mypoli.android.common.navigation

import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import mypoli.android.home.HomeViewController

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/2/17.
 */
class Navigator(private val router: Router?) {

    fun showHome() {
        val handler = FadeChangeHandler()
        router?.pushController(
            RouterTransaction.with(HomeViewController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }
}