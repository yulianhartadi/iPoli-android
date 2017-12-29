package mypoli.android.challenge.usecase

import mypoli.android.challenge.data.Challenge
import mypoli.android.common.UseCase
import mypoli.android.quest.Category
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class ScheduleChallengeUseCase(private val questRepository: QuestRepository) : UseCase<ScheduleChallengeUseCase.Params, List<Quest>> {

    override fun execute(parameters: Params): List<Quest> {
        val challenge = parameters.challenge

        require(challenge.quests.isNotEmpty(), { "Challenge must contain quests" })

        val startDate = parameters.startDate
        val endDate = startDate.plusDays((challenge.durationDays - 1).toLong())

        val quests = mutableListOf<Quest>()

        challenge.quests.forEach {
            when (it) {
                is Challenge.Quest.Repeating -> {
                    val days = ChronoUnit.DAYS.between(startDate, endDate)
                    (0..days).mapTo(quests) { i ->
                        createFromRepeating(it, challenge, startDate.plusDays(i))
                    }
                }

                is Challenge.Quest.OneTime -> {

                    val scheduledDate = if (it.preferredDayOfWeek != null) {
                        startDate.with(it.preferredDayOfWeek)
                    } else {
                        endDate
                    }

                    val q = createFromOneTime(it, challenge, scheduledDate)
                    quests.add(q)
                }
            }
        }

        return quests.map { questRepository.save(it) }
    }

    private fun createFromOneTime(it: Challenge.Quest.OneTime, challenge: Challenge, scheduledDate: LocalDate) =
        Quest(
            name = it.name,
            color = it.color,
            icon = it.icon,
            startTime = it.startTime,
            category = Category(challenge.category.name, it.color),
            duration = it.duration,
            scheduledDate = scheduledDate
        )

    private fun createFromRepeating(it: Challenge.Quest.Repeating, challenge: Challenge, scheduledDate: LocalDate) =
        Quest(
            name = it.name,
            color = it.color,
            icon = it.icon,
            startTime = it.startTime,
            category = Category(challenge.category.name, it.color),
            duration = it.duration,
            scheduledDate = scheduledDate
        )


    data class Params(val challenge: Challenge, val startDate: LocalDate = LocalDate.now())
}