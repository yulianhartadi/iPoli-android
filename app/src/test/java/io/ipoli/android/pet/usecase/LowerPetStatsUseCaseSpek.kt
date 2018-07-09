package io.ipoli.android.pet.usecase

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.Constants
import io.ipoli.android.TestUtil
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

            fun executeUseCase(questRepository: QuestRepository, date: LocalDate = LocalDate.now()) =
                LowerPetStatsUseCase(questRepository, playerRepo, randomSeed = 42).execute(
                    LowerPetStatsUseCase.Params(date)
                )

            val wholeInterval =
                Constants.CHANGE_PET_STATS_INTERVAL_START.minutesTo(Constants.CHANGE_PET_STATS_INTERVAL_END)

            it("should lower HP & MP when no quests are done in the morning") {

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
                            duration = Constants.CHANGE_PET_STATS_INTERVAL_START.minutesTo(Constants.CHANGE_PET_STATS_INTERVAL_END),
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_START
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                newPet.healthPoints.`should be equal to`(37)
                newPet.moodPoints.`should be equal to`(86)
            }

            it("should lower only random HP & MP when 70% of the time is scheduled & quest(s) is complete") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = (LowerPetStatsUseCase.HIGH_PRODUCTIVE_TIME_COEF *
                                wholeInterval).toInt(),
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_START
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                newPet.healthPoints.`should be equal to`(37)
                newPet.moodPoints.`should be equal to`(86)
            }

            it("should lower HP & MP when 69% of the time is scheduled & quest(s) is complete") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = ((LowerPetStatsUseCase.HIGH_PRODUCTIVE_TIME_COEF - 0.01) *
                                wholeInterval).toInt(),
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_START
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                pet.healthPoints.`should be greater than`(newPet.healthPoints)
                pet.moodPoints.`should be greater than`(newPet.moodPoints)
            }

            it("should lower HP & MP when productivity is high with low penalty") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = (LowerPetStatsUseCase.MEDIUM_PRODUCTIVE_TIME_COEF *
                                wholeInterval).toInt(),
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_START
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                val expectedHealthPoints = pet.healthPoints - LowerPetStatsUseCase.LOW_PENALTY - 3
                val expectedMoodPoints = pet.moodPoints - LowerPetStatsUseCase.LOW_PENALTY - 3
                expectedHealthPoints.`should be equal to`(newPet.healthPoints)
                expectedMoodPoints.`should be equal to`(newPet.moodPoints)
            }

            it("should lower HP & MP when productivity is medium with medium penalty") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = (LowerPetStatsUseCase.LOW_PRODUCTIVE_TIME_COEF *
                                wholeInterval).toInt(),
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_START
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                val expectedHealthPoints = pet.healthPoints - LowerPetStatsUseCase.MEDIUM_PENALTY - 3
                val expectedMoodPoints = pet.moodPoints - LowerPetStatsUseCase.MEDIUM_PENALTY - 3
                expectedHealthPoints.`should be equal to`(newPet.healthPoints)
                expectedMoodPoints.`should be equal to`(newPet.moodPoints)
            }

            it("should lower HP & MP when productivity is low with high penalty") {

                val questRepo = mock<QuestRepository> {
                    on { findCompletedForDate(any()) } doReturn listOf(
                        quest.copy(
                            duration = (LowerPetStatsUseCase.LOW_PRODUCTIVE_TIME_COEF * wholeInterval).toInt() - 1,
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_START
                        )
                    )
                }

                val newPet = executeUseCase(questRepo)
                val expectedHealthPoints = pet.healthPoints - LowerPetStatsUseCase.HIGH_PENALTY - 3
                val expectedMoodPoints = 34
                expectedHealthPoints.`should be equal to`(newPet.healthPoints)
                expectedMoodPoints.`should be equal to`(newPet.moodPoints)
            }
        }


        describe("filterQuestsInInterval") {

            it("should not include quest before startDate") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_INTERVAL_START,
                    Constants.CHANGE_PET_STATS_INTERVAL_END,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_START - 60,
                            duration = 60
                        )
                    )
                )
                duration.`should be equal to`(0)
            }

            it("should not include quest after end") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_INTERVAL_START,
                    Constants.CHANGE_PET_STATS_INTERVAL_END,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_END,
                            duration = 60
                        )
                    )
                )
                duration.`should be equal to`(0)
            }

            it("should include quest at interval startDate") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_INTERVAL_START,
                    Constants.CHANGE_PET_STATS_INTERVAL_END,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_START,
                            duration = 60
                        )
                    )
                )
                duration.`should be equal to`(60)
            }

            it("should include quest at interval end") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_INTERVAL_START,
                    Constants.CHANGE_PET_STATS_INTERVAL_END,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_END - 60,
                            duration = 60
                        )
                    )
                )
                duration.`should be equal to`(60)
            }

            it("should include quest starting before interval") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_INTERVAL_START,
                    Constants.CHANGE_PET_STATS_INTERVAL_END,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_START - 1,
                            duration = 61
                        )
                    )
                )
                duration.`should be equal to`(60)
            }

            it("should include quest ending after interval") {

                val duration = LowerPetStatsUseCase.findQuestsDurationInInterval(
                    Constants.CHANGE_PET_STATS_INTERVAL_START,
                    Constants.CHANGE_PET_STATS_INTERVAL_END,
                    listOf(
                        quest.copy(
                            startTime = Constants.CHANGE_PET_STATS_INTERVAL_END - 60,
                            duration = 61
                        )
                    )
                )
                duration.`should be equal to`(60)
            }
        }
    }
})