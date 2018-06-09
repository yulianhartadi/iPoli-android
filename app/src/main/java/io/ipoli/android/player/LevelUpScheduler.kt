package io.ipoli.android.player

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.myPoliApp
import io.ipoli.android.player.view.LevelUpPopup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects

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