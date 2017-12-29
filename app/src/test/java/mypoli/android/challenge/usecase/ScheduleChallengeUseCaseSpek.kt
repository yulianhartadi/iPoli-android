package mypoli.android.challenge.usecase

import mypoli.android.TestUtil
import mypoli.android.challenge.data.Challenge
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class ScheduleChallengeUseCaseSpek : Spek({

    describe("ScheduleChallengeUseCase") {

        val challenge = Challenge(
            "", Challenge.Category.HEALTH_AND_FITNESS, listOf(), durationDays = 1
        )

        fun executeUseCase(challenge: Challenge, startDate: LocalDate = LocalDate.now()) =
            ScheduleChallengeUseCase(TestUtil.questRepoMock()).execute(ScheduleChallengeUseCase.Params(challenge, startDate = startDate))

        it("should not accept Challenge without Quests") {
            val exec = { executeUseCase(Challenge("", Challenge.Category.HEALTH_AND_FITNESS, listOf())) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should not schedule after end date") {

            val quests = executeUseCase(challenge.copy(
                durationDays = 1,
                quests = listOf(
                    Challenge.Quest.Repeating(
                        "", "", 1, weekDays = DayOfWeek.values().toList()
                    )
                )
            ))
            quests.size.`should be equal to`(1)
            val quest = quests[0]
            quest.scheduledDate.`should equal`(LocalDate.now())
        }

        it("should schedule repeating Quest for every day") {

            val quests = executeUseCase(challenge.copy(
                durationDays = 7,
                quests = listOf(
                    Challenge.Quest.Repeating(
                        "", "", 1, weekDays = DayOfWeek.values().toList()
                    )
                )
            ))

            quests.size.`should be equal to`(7)
            quests.forEachIndexed { index, quest ->
                quest.scheduledDate.`should equal`(LocalDate.now().plusDays(index.toLong()))
            }
        }

        it("should schedule one time Quest") {
            val startDate = LocalDate.now().with(DayOfWeek.FRIDAY)
            val quests = executeUseCase(challenge.copy(
                durationDays = 7,
                quests = listOf(
                    Challenge.Quest.OneTime(
                        "", "", 1, preferredDayOfWeek = DayOfWeek.SATURDAY
                    )
                )
            ), startDate = startDate)

            quests.size.`should be equal to`(1)
            val quest = quests[0]
            quest.scheduledDate.`should equal`(startDate.plusDays(1))
        }
    }
})