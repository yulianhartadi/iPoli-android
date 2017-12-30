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

        fun executeUseCase(challenge: Challenge, startDate: LocalDate = LocalDate.now(), randomSeed: Long? = null) =
            ScheduleChallengeUseCase(TestUtil.questRepoMock()).execute(
                ScheduleChallengeUseCase.Params(
                    challenge,
                    startDate = startDate,
                    randomSeed = randomSeed
                )
            )

        it("should not accept Challenge without Quests") {
            val exec = { executeUseCase(Challenge("", Challenge.Category.HEALTH_AND_FITNESS, listOf())) }
            exec shouldThrow IllegalArgumentException::class
        }

        describe("Repeating") {

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

            it("should schedule for selected days of week") {
                val startDate = LocalDate.now().with(DayOfWeek.MONDAY)
                val weekDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                val quests = executeUseCase(challenge.copy(
                    durationDays = 7,
                    quests = listOf(
                        Challenge.Quest.Repeating(
                            "",
                            "",
                            1,
                            weekDays = weekDays
                        )
                    )
                ), startDate = startDate)
                quests.size.`should be equal to`(3)
                quests.first().scheduledDate.dayOfWeek.`should equal`(weekDays[0])
                quests[1].scheduledDate.dayOfWeek.`should equal`(weekDays[1])
                quests[2].scheduledDate.dayOfWeek.`should equal`(weekDays[2])
            }

            it("should schedule start at selected day & for selected days of week") {
                val startDate = LocalDate.now().with(DayOfWeek.MONDAY)
                val weekDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                val quests = executeUseCase(challenge.copy(
                    durationDays = 7,
                    quests = listOf(
                        Challenge.Quest.Repeating(
                            "",
                            "",
                            1,
                            startAtDay = 3,
                            weekDays = weekDays
                        )
                    )
                ), startDate = startDate)
                quests.size.`should be equal to`(2)
                quests.first().scheduledDate.dayOfWeek.`should equal`(weekDays[1])
                quests[1].scheduledDate.dayOfWeek.`should equal`(weekDays[2])
            }
        }

        describe("OneTime") {

            it("should schedule") {
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
                quests.first().scheduledDate.`should equal`(startDate.plusDays(1))
            }

            it("should schedule when preferred day of week is unavailable") {
                val startDate = LocalDate.now().with(DayOfWeek.MONDAY)
                val quests = executeUseCase(challenge.copy(
                    durationDays = 1,
                    quests = listOf(
                        Challenge.Quest.OneTime(
                            "", "", 1, preferredDayOfWeek = DayOfWeek.SATURDAY
                        )
                    )
                ), startDate = startDate)

                quests.size.`should be equal to`(1)
                quests.first().scheduledDate.`should equal`(startDate)
            }

            it("should schedule at start day and ignore preferred day of week") {
                val startDate = LocalDate.now().with(DayOfWeek.MONDAY)
                val preferredDayOfWeek = DayOfWeek.TUESDAY
                val quests = executeUseCase(challenge.copy(
                    durationDays = 14,
                    quests = listOf(
                        Challenge.Quest.OneTime(
                            "",
                            "",
                            1,
                            startAtDay = 7,
                            preferredDayOfWeek = preferredDayOfWeek
                        )
                    )),
                    startDate = startDate
                )

                quests.size.`should be equal to`(1)
                val expectedScheduleDate = startDate.plusDays(6)
                quests.first().scheduledDate.`should equal`(expectedScheduleDate)
            }

            it("should schedule at random day") {
                val startDate = LocalDate.now().with(DayOfWeek.MONDAY)
                val preferredDayOfWeek = DayOfWeek.SUNDAY
                val quests = executeUseCase(challenge.copy(
                    durationDays = 5,
                    quests = listOf(
                        Challenge.Quest.OneTime(
                            "",
                            "",
                            1,
                            startAtDay = 7,
                            preferredDayOfWeek = preferredDayOfWeek
                        )
                    )),
                    startDate = startDate,
                    randomSeed = 42
                )

                quests.size.`should be equal to`(1)
                quests.first().scheduledDate.`should equal`(startDate)
            }

            it("should schedule at next preferred day of week") {
                val startDate = LocalDate.now().with(DayOfWeek.FRIDAY)
                val preferredDayOfWeek = DayOfWeek.MONDAY
                val quests = executeUseCase(challenge.copy(
                    durationDays = 10,
                    quests = listOf(
                        Challenge.Quest.OneTime(
                            "",
                            "",
                            1,
                            preferredDayOfWeek = preferredDayOfWeek
                        )
                    )),
                    startDate = startDate
                )

                quests.size.`should be equal to`(1)
                quests.first().scheduledDate.`should equal`(startDate.plusDays(3))
            }

        }

    }
})