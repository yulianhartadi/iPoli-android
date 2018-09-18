package io.ipoli.android.player.job

import io.ipoli.android.MyPoliApp
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.player.view.SecretSocietyPopup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

interface SecretSocietyInviteScheduler {
    fun schedule()
}

class AndroidSecretSocietyInviteScheduler : SecretSocietyInviteScheduler {

    override fun schedule() {
        val c = MyPoliApp.instance.asThemedWrapper()
        launch(UI) {
            SecretSocietyPopup().show(c)
        }
    }
}