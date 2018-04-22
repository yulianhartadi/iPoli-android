package io.ipoli.android.pet.usecase

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.Constants
import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.pet.Pet
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.any
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

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

            val pet = player.pet

            val playerRepo = TestUtil.playerRepoMock(player)

            fun executeUseCase(questRepository: QuestRepository, time: Time) =
                LowerPetStatsUseCase(questRepository, playerRepo, randomSeed = 42).execute(time)

            val eveningInterval =
                Constants.CHANGE_PET_STATS_AFTERNOON_TIME.minutesTo(Constants.CHANGE_PET_STATS_EVENING_TIME)

            it("should remove fixed HP & MP in the morning") {
                val newPet = executeUseCase(mock(), Constants.CHANGE_PET_STATS_MORNING_TIME)
                val expectedHealthPoints =
                    pet.healthPoints - LowerPetStatsUseCase.MORNING_HEALTH_POINTS_PENALTIES[0]
                val expectedMoodPoints =
                    pet.moodPoints - LowerPetStatsUseCase.MORNING_MOOD_POINTS_PENALTIES[0]
                expectedHealthPoints.`should be equal to`(newPet.healthPoints)
                expectedMoodPoints.`should be equal to`(newPet.moodPoints)
            }

            it("should lower HP & MP when no quests are done in the morning") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf<Quest>()
                }

                val newPet = executeUseCase(questRepo, Constants.CHANGE_PET_STATS_AFTERNOON_TIME)
                val expectedHealthPoints = pet.healthPoints - LowerPetStatsUseCase.MAX_PENALTY
                val expectedMoodPoints = pet.moodPoints - LowerPetStatsUseCase.MAX_PENALTY
                expectedHealthPoints.`should be equal to`(newPet.healthPoints)
                expectedMoodPoints.`should be equal to`(newPet.moodPoints)
            }

            it("should not lower HP & MP when all time is scheduled & quest(s) is complete") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = Constants.CHANGE_PET_STATS_MORNING_TIME.minutesTo(Constants.CHANGE_PET_STATS_AFTERNOON_TIME),
                            startTime = Constants.CHANGE_PET_STATS_MORNING_TIME
                        )
                    )
                }

                val newPet = executeUseCase(questRepo, Constants.CHANGE_PET_STATS_AFTERNOON_TIME)
                pet.healthPoints.`should be equal to`(newPet.healthPoints)
                pet.moodPoints.`should be equal to`(newPet.moodPoints)
            }

            it("should not lower HP & MP when 70% of the time is scheduled & quest(s) is complete") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = (LowerPetStatsUseCase.HIGH_PRODUCTIVE_TIME_COEF *
                                eveningInterval).toInt(),
                            startTime = Constants.CHANGE_PET_STATS_AFTERNOON_TIME
                        )
                    )
                }

                val newPet = executeUseCase(questRepo, Constants.CHANGE_PET_STATS_EVENING_TIME)
                pet.healthPoints.`should be equal to`(newPet.healthPoints)
                pet.moodPoints.`should be equal to`(newPet.moodPoints)
            }

            it("should lower HP & MP when 69% of the time is scheduled & quest(s) is complete") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = ((LowerPetStatsUseCase.HIGH_PRODUCTIVE_TIME_COEF - 0.01) *
                                eveningInterval).toInt(),
                            startTime = Constants.CHANGE_PET_STATS_AFTERNOON_TIME
                        )
                    )
                }

                val newPet = executeUseCase(questRepo, Constants.CHANGE_PET_STATS_EVENING_TIME)
                pet.healthPoints.`should be greater than`(newPet.healthPoints)
                pet.moodPoints.`should be greater than`(newPet.moodPoints)
            }

            it("should lower HP & MP when productivity is high with low penalty") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = (LowerPetStatsUseCase.MEDIUM_PRODUCTIVE_TIME_COEF *
                                eveningInterval).toInt(),
                            startTime = Constants.CHANGE_PET_STATS_AFTERNOON_TIME
                        )
                    )
                }

                val newPet = executeUseCase(questRepo, Constants.CHANGE_PET_STATS_EVENING_TIME)
                val expectedHealthPoints = pet.healthPoints - LowerPetStatsUseCase.LOW_PENALTY
                val expectedMoodPoints = pet.moodPoints - LowerPetStatsUseCase.LOW_PENALTY
                expectedHealthPoints.`should be equal to`(newPet.healthPoints)
                expectedMoodPoints.`should be equal to`(newPet.moodPoints)
            }

            it("should lower HP & MP when productivity is medium with medium penalty") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = (LowerPetStatsUseCase.LOW_PRODUCTIVE_TIME_COEF *
                                eveningInterval).toInt(),
                            startTime = Constants.CHANGE_PET_STATS_AFTERNOON_TIME
                        )
                    )
                }

                val newPet = executeUseCase(questRepo, Constants.CHANGE_PET_STATS_EVENING_TIME)
                val expectedHealthPoints = pet.healthPoints - LowerPetStatsUseCase.MEDIUM_PENALTY
                val expectedMoodPoints = pet.moodPoints - LowerPetStatsUseCase.MEDIUM_PENALTY
                expectedHealthPoints.`should be equal to`(newPet.healthPoints)
                expectedMoodPoints.`should be equal to`(newPet.moodPoints)
            }

            it("should lower HP & MP when productivity is low with high penalty") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = (LowerPetStatsUseCase.LOW_PRODUCTIVE_TIME_COEF * eveningInterval).toInt() - 1,
                            startTime = Constants.CHANGE_PET_STATS_AFTERNOON_TIME
                        )
                    )
                }

                val newPet = executeUseCase(questRepo, Constants.CHANGE_PET_STATS_EVENING_TIME)
                val expectedHealthPoints = pet.healthPoints - LowerPetStatsUseCase.HIGH_PENALTY
                val expectedMoodPoints = pet.moodPoints - LowerPetStatsUseCase.HIGH_PENALTY
                expectedHealthPoints.`should be equal to`(newPet.healthPoints)
                expectedMoodPoints.`should be equal to`(newPet.moodPoints)
            }
        }


        describe("filterQuestsInInterval") {

            it("should not include quest before startDate") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_MORNING_TIME,
                    Constants.CHANGE_PET_STATS_AFTERNOON_TIME,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_MORNING_TIME - 60,
                            duration = 60
                        )
                    )
                )
                duration.`should be equal to`(0)
            }

            it("should not include quest after end") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_MORNING_TIME,
                    Constants.CHANGE_PET_STATS_AFTERNOON_TIME,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_AFTERNOON_TIME,
                            duration = 60
                        )
                    )
                )
                duration.`should be equal to`(0)
            }

            it("should include quest at interval startDate") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_MORNING_TIME,
                    Constants.CHANGE_PET_STATS_AFTERNOON_TIME,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_MORNING_TIME,
                            duration = 60
                        )
                    )
                )
                duration.`should be equal to`(60)
            }

            it("should include quest at interval end") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_MORNING_TIME,
                    Constants.CHANGE_PET_STATS_AFTERNOON_TIME,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_AFTERNOON_TIME - 60,
                            duration = 60
                        )
                    )
                )
                duration.`should be equal to`(60)
            }

            it("should include quest starting before interval") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_MORNING_TIME,
                    Constants.CHANGE_PET_STATS_AFTERNOON_TIME,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_MORNING_TIME - 1,
                            duration = 61
                        )
                    )
                )
                duration.`should be equal to`(60)
            }

            it("should include quest ending after interval") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_MORNING_TIME,
                    Constants.CHANGE_PET_STATS_AFTERNOON_TIME,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_AFTERNOON_TIME - 60,
                            duration = 61
                        )
                    )
                )
                duration.`should be equal to`(60)
            }
        }
    }
})