package mypoli.android.common.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import mypoli.android.common.rate.RatePopupJob
import mypoli.android.pet.LowerPetStatsJob
import mypoli.android.player.LevelUpJob
import mypoli.android.quest.job.QuestCompleteJob
import mypoli.android.quest.job.ReminderNotificationJob
import mypoli.android.quest.timer.job.TimerCompleteNotificationJob
import mypoli.android.repeatingquest.SaveQuestsForRepeatingQuestJob
import mypoli.android.store.membership.job.CheckMembershipStatusJob
import mypoli.android.store.powerup.job.RemoveExpiredPowerUpsJob

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/28/17.
 */
class myPoliJobCreator : JobCreator {
    override fun create(tag: String): Job? =
        when (tag) {
            ReminderNotificationJob.TAG -> ReminderNotificationJob()
            QuestCompleteJob.TAG -> QuestCompleteJob()
            LevelUpJob.TAG -> LevelUpJob()
            RatePopupJob.TAG -> RatePopupJob()
            LowerPetStatsJob.TAG -> LowerPetStatsJob()
            TimerCompleteNotificationJob.TAG -> TimerCompleteNotificationJob()
            SaveQuestsForRepeatingQuestJob.TAG -> SaveQuestsForRepeatingQuestJob()
            RemoveExpiredPowerUpsJob.TAG -> RemoveExpiredPowerUpsJob()
            CheckMembershipStatusJob.TAG -> CheckMembershipStatusJob()
            else -> null
        }
}