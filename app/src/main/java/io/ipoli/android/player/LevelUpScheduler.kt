package io.ipoli.android.player

import io.ipoli.android.MyPoliApp
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.player.view.LevelUpPopup
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/15/17.
 */
interface LevelUpScheduler {
    fun schedule(newLevel: Int)
}

class AndroidLevelUpScheduler : LevelUpScheduler {

    override fun schedule(newLevel: Int) {
        val c = MyPoliApp.instance.asThemedWrapper()
        GlobalScope.launch(Dispatchers.Main) {
            LevelUpPopup(newLevel).show(c)
        }
    }
}