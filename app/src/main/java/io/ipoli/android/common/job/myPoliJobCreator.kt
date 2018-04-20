package io.ipoli.android.common.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import io.ipoli.android.common.rate.RatePopupJob
import io.ipoli.android.pet.LowerPetStatsJob
import io.ipoli.android.player.LevelUpJob
import io.ipoli.android.quest.job.QuestCompleteJob
import io.ipoli.android.quest.job.ReminderNotificationJob
import io.ipoli.android.quest.show.job.TimerCompleteNotificationJob
import io.ipoli.android.repeatingquest.SaveQuestsForRepeatingQuestJob
import io.ipoli.android.store.membership.job.CheckMembershipStatusJob
import io.ipoli.android.store.powerup.job.RemoveExpiredPowerUpsJob

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