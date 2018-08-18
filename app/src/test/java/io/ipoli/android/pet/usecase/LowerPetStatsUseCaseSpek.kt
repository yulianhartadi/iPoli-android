package io.ipoli.android.pet.usecase

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.pet.Pet
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.any
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 30.11.17.
 */
class LowerPetStatsUseCaseSpek : Spek({
    describe("LowerPetStatsUseCase") {

        val quest = Quest(
            name = "",
            color = Color.BLUE,
            scheduledDate = LocalDate.now(),
            duration = 60
        )

        describe("execute") {

            val player = TestUtil.player().copy(
                pet = TestUtil.player().pet.copy(
                    healthPoints = 40,
                    moodPoints = Pet.AWESOME_MIN_MOOD_POINTS - 1
                )
            )

            val playerRepo = TestUtil.playerRepoMock(player)

            val now = LocalDateTime.now().with(DayOfWeek.TUESDAY)
            val yesterday = now.minusDays(1).toLocalDate()

            fun executeUseCase(questRepository: QuestRepository, date: LocalDateTime = now) =
                LowerPetStatsUseCase(questRepository, playerRepo, randomSeed = 42).execute(
                    LowerPetStatsUseCase.Params(date)
                )

            it("should lower HP & MP when no quests are done") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf<Quest>()
                }

                val newPet = executeUseCase(questRepo)
                newPet.healthPoints.`should be equal to`(12)
                newPet.moodPoints.`should be equal to`(34)
            }

            it("should lower randomly HP & MP when all time is scheduled & quest(s) is complete") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = LowerPetStatsUseCase.PLAN_DAY_HIGH_PRODUCTIVE_HOURS * Time.MINUTES_IN_AN_HOUR,
                            startTime = player.preferences.resetDayTime,
                            completedAtDate = yesterday
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                newPet.healthPoints.`should be equal to`(37)
                newPet.moodPoints.`should be equal to`(86)
            }

            it("should lower stats with low penalty when medium < duration < high productive hours") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = LowerPetStatsUseCase.PLAN_DAY_MEDIUM_PRODUCTIVE_HOURS * Time.MINUTES_IN_AN_HOUR,
                            startTime = player.preferences.resetDayTime,
                            completedAtDate = yesterday
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                newPet.healthPoints.`should be equal to`(33)
                newPet.moodPoints.`should be equal to`(82)
            }

            it("should lower stats with medium penalty when low < duration < medium productive hours") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = LowerPetStatsUseCase.PLAN_DAY_LOW_PRODUCTIVE_HOURS * Time.MINUTES_IN_AN_HOUR,
                            startTime = player.preferences.resetDayTime,
                            completedAtDate = yesterday
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                newPet.healthPoints.`should be equal to`(29)
                newPet.moodPoints.`should be equal to`(78)
            }

            it("should lower stats with high penalty when duration < low productive hours") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = (LowerPetStatsUseCase.PLAN_DAY_LOW_PRODUCTIVE_HOURS * Time.MINUTES_IN_AN_HOUR) - 1,
                            startTime = player.preferences.resetDayTime,
                            completedAtDate = yesterday
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                newPet.healthPoints.`should be equal to`(19)
                newPet.moodPoints.`should be equal to`(34)
            }
        }


        describe("filterQuestsInInterval") {

            it("should not include quest before startDate") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    LocalDate.now(),
                    null,
                    Time.atHours(10),
                    listOf(
                        quest.copy(
                            startTime = Time.atHours(9),
                            duration = 60,
                            completedAtDate = LocalDate.now()
                        )
                    )
                )
                duration.`should be equal to`(0)
            }

            it("should not include quest after end") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    LocalDate.now().minusDays(1),
                    LocalDate.now(),
                    Time.atHours(10),
                    listOf(
                        quest.copy(
                            startTime = Time.atHours(10),
                            duration = 60,
                            completedAtDate = LocalDate.now()
                        )
                    )
                )
                duration.`should be equal to`(0)
            }

            it("should include quest at interval startDate") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    LocalDate.now(),
                    null,
                    Time.atHours(10),
                    listOf(
                        quest.copy(
                            startTime = Time.atHours(10),
                            duration = 60,
                            completedAtDate = LocalDate.now()
                        )
                    )
                )
                duration.`should be equal to`(60)
            }

            it("should include quest at interval end") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    LocalDate.now().minusDays(1),
                    LocalDate.now(),
                    Time.atHours(10),
                    listOf(
                        quest.copy(
                            startTime = Time.at(8, 59),
                            duration = 60,
                            completedAtDate = LocalDate.now()
                        )
                    )
                )
                duration.`should be equal to`(60)
            }

            it("should include quest starting before interval") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    LocalDate.now(),
                    null,
                    Time.atHours(10),
                    listOf(
                        quest.copy(
                            startTime = Time.at(9, 59),
                            duration = 61,
                            completedAtDate = LocalDate.now()
                        )
                    )
                )
                duration.`should be equal to`(60)
            }

            it("should include quest ending after interval") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    LocalDate.now().minusDays(1),
                    LocalDate.now(),
                    Time.atHours(10),
                    listOf(
                        quest.copy(
                            startTime = Time.atHours(9),
                            duration = 61,
                            completedAtDate = LocalDate.now()
                        )
                    )
                )
                duration.`should be equal to`(60)
            }
        }
    }
})