package io.ipoli.android.player

import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.myPoliApp
import io.ipoli.android.player.view.LevelUpPopup
import kotlinx.coroutines.experimental.android.UI
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

        val c = myPoliApp.instance.asThemedWrapper()
        launch(UI) {
            LevelUpPopup(newLevel).show(c)
        }
    }
}