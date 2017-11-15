package io.ipoli.android.common.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import io.ipoli.android.player.LevelUpJob
import io.ipoli.android.quest.QuestCompleteJob
import io.ipoli.android.reminder.ReminderNotificationJob

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/28/17.
 */
class iPoliJobCreator : JobCreator {
    override fun create(tag: String): Job? =
        when (tag) {
            ReminderNotificationJob.TAG -> ReminderNotificationJob()
            QuestCompleteJob.TAG -> QuestCompleteJob()
            LevelUpJob.TAG -> LevelUpJob()
            else -> null
        }
}