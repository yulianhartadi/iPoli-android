package mypoli.android.repeatingquest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import mypoli.android.TestUtil
import mypoli.android.TestUtil.firstDateOfWeek
import mypoli.android.TestUtil.lastDateOfWeek
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/14/2018.
 */
class FindQuestsForRepeatingQuestSpek : Spek({

    describe("FindQuestsForRepeatingQuest") {
        fun executeUseCase(
            quest: RepeatingQuest,
            start: LocalDate,
            end: LocalDate,
            questRepo: QuestRepository = TestUtil.questRepoMock()
        ) =
            FindQuestsForRepeatingQuest(questRepo).execute(
                FindQuestsForRepeatingQuest.Params(
                    repeatingQuest = quest,
                    start = start,
                    end = end
                )
            )

        fun mockQuestsForRepeatingQuest(quests: List<Quest> = listOf()) =
            mock<QuestRepository> {
                on {
                    findForRepeatingQuestBetween(
                        any(),
                        any(),
                        any()
                    )
                } doReturn quests
            }

        val questId = "qid"

        it("should require end after start") {
            val exec = {
                val today = LocalDate.now()
                executeUseCase(TestUtil.repeatingQuest, today, today)
            }
            exec shouldThrow IllegalArgumentException::class
        }

        describe("repeating daily") {

            it("should schedule for every day at start of week when no quests are scheduled") {

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest,
                    start = firstDateOfWeek,
                    end = lastDateOfWeek
                )
                quests.size.`should be`(7)
            }

            it("should schedule for every day at start of week when 1 quest is scheduled") {

                val repo = mockQuestsForRepeatingQuest(
                    listOf(
                        TestUtil.quest.copy(
                            id = questId,
                            originalScheduledDate = firstDateOfWeek
                        )
                    )
                )

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest,
                    start = firstDateOfWeek,
                    end = lastDateOfWeek,
                    questRepo = repo
                )
                quests.size.`should be`(7)
                quests.first().id.`should be`(questId)
                quests.filter { it.id.isEmpty() }.size.`should be`(6)
            }

            it("should find only stored quests") {


                val repo = mockQuestsForRepeatingQuest(
                    List(7) { i ->
                        TestUtil.quest.copy(
                            id = questId,
                            originalScheduledDate = firstDateOfWeek.plusDays(i.toLong())
                        )
                    }
                )

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest,
                    start = firstDateOfWeek,
                    end = lastDateOfWeek,
                    questRepo = repo
                )
                quests.size.`should be`(7)
                quests.forEach { it.id.`should be`(questId) }
            }

            it("should not schedule for dates with deleted quest") {

                val repo = mockQuestsForRepeatingQuest(
                    listOf(
                        TestUtil.quest.copy(
                            id = questId,
                            originalScheduledDate = firstDateOfWeek,
                            isRemoved = true
                        )
                    )
                )

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest,
                    start = firstDateOfWeek,
                    end = lastDateOfWeek,
                    questRepo = repo
                )
                quests.size.`should be`(6)
                quests.filter { it.scheduledDate.isEqual(firstDateOfWeek) }
                    .`should be empty`()
            }

            it("should not schedule for dates after repeating quest end") {

                val repo = mockQuestsForRepeatingQuest(listOf())

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest.copy(
                        start = firstDateOfWeek,
                        end = firstDateOfWeek.plusDays(3)
                    ),
                    start = firstDateOfWeek,
                    end = lastDateOfWeek,
                    questRepo = repo
                )
                quests.size.`should be`(4)
            }

            it("should not schedule for dates before repeating quest start") {

                val repo = mockQuestsForRepeatingQuest(listOf())

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest.copy(
                        start = firstDateOfWeek.plusDays(1),
                        end = lastDateOfWeek
                    ),
                    start = firstDateOfWeek,
                    end = lastDateOfWeek,
                    questRepo = repo
                )
                quests.size.`should be`(6)
            }

        }

        describe("repeating weekly") {

            describe("fixed") {
                it("should schedule for every week day in pattern") {

                    val repo = mockQuestsForRepeatingQuest(listOf())

                    val quests = executeUseCase(
                        quest = TestUtil.repeatingQuest.copy(
                            repeatingPattern = RepeatingPattern.Weekly(
                                setOf(
                                    DayOfWeek.MONDAY,
                                    DayOfWeek.WEDNESDAY,
                                    DayOfWeek.FRIDAY
                                )
                            )
                        ),
                        start = firstDateOfWeek,
                        end = lastDateOfWeek.plusDays(3),
                        questRepo = repo
                    )
                    quests.size.`should be`(5)
                    quests.filter { it.id.isEmpty() }.size.`should be`(5)
                }

                it("should not schedule for moved stored quest") {

                    val repo = mockQuestsForRepeatingQuest(
                        listOf(
                            TestUtil.quest.copy(
                                id = questId,
                                originalScheduledDate = firstDateOfWeek,
                                scheduledDate = firstDateOfWeek.plusDays(1)
                            )
                        )
                    )

                    val quests = executeUseCase(
                        quest = TestUtil.repeatingQuest.copy(
                            repeatingPattern = RepeatingPattern.Weekly(
                                setOf(
                                    DayOfWeek.MONDAY,
                                    DayOfWeek.WEDNESDAY,
                                    DayOfWeek.FRIDAY
                                )
                            )
                        ),
                        start = firstDateOfWeek,
                        end = lastDateOfWeek,
                        questRepo = repo
                    )
                    quests.size.`should be`(3)
                    quests.filter { it.id.isEmpty() }.size.`should be`(2)
                }
            }
        }

        describe("repeating monthly") {
            describe("fixed") {
                it("should schedule for every month day in pattern when days present in month") {

                    val repo = mockQuestsForRepeatingQuest(listOf())

                    val start = LocalDate.of(2000, 2, 1)
                    val end = LocalDate.of(2000, 3, 15)


                    val quests = executeUseCase(
                        quest = TestUtil.repeatingQuest.copy(
                            start = start,
                            repeatingPattern = RepeatingPattern.Monthly(
                                setOf(1, 10, 29, 31)
                            )
                        ),
                        start = start,
                        end = end,
                        questRepo = repo
                    )
                    quests.size.`should be`(5)
                    quests.filter { it.id.isEmpty() }.size.`should be`(5)
                }

                it("should not schedule for stored quests") {

                    val start = LocalDate.of(2000, 2, 1)
                    val end = LocalDate.of(2000, 3, 15)

                    val repo = mockQuestsForRepeatingQuest(
                        listOf(
                            TestUtil.quest.copy(
                                id = questId,
                                originalScheduledDate = start
                            )
                        )
                    )


                    val quests = executeUseCase(
                        quest = TestUtil.repeatingQuest.copy(
                            start = start,
                            repeatingPattern = RepeatingPattern.Monthly(
                                setOf(1, 10, 29, 31)
                            )
                        ),
                        start = start,
                        end = end,
                        questRepo = repo
                    )
                    quests.size.`should be`(5)
                    quests.filter { it.id.isEmpty() }.size.`should be`(4)
                }
            }

        }

        describe("repeating yearly") {

            it("should not schedule for date outside range") {
                val repo = mockQuestsForRepeatingQuest(
                    listOf(
                        TestUtil.quest.copy(
                            id = questId,
                            originalScheduledDate = firstDateOfWeek
                        )
                    )
                )

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest.copy(
                        repeatingPattern = RepeatingPattern.Yearly(
                            firstDateOfWeek.dayOfMonth,
                            firstDateOfWeek.monthValue
                        )
                    ),
                    start = firstDateOfWeek.plusDays(1),
                    end = lastDateOfWeek,
                    questRepo = repo
                )
                quests.`should be empty`()
            }

            it("should create when no quest is scheduled") {
                val repo = mockQuestsForRepeatingQuest(listOf())

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest.copy(
                        repeatingPattern = RepeatingPattern.Yearly(
                            firstDateOfWeek.dayOfMonth,
                            firstDateOfWeek.monthValue
                        )
                    ),
                    start = firstDateOfWeek,
                    end = lastDateOfWeek,
                    questRepo = repo
                )
                quests.size.`should be`(1)
                quests.first().id.`should be empty`()
            }

            it("should return scheduled quest") {
                val repo = mockQuestsForRepeatingQuest(
                    listOf(
                        TestUtil.quest.copy(
                            id = questId,
                            originalScheduledDate = firstDateOfWeek
                        )
                    )
                )

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest.copy(
                        repeatingPattern = RepeatingPattern.Yearly(
                            firstDateOfWeek.dayOfMonth,
                            firstDateOfWeek.monthValue
                        )
                    ),
                    start = firstDateOfWeek,
                    end = lastDateOfWeek,
                    questRepo = repo
                )
                quests.size.`should be`(1)
                quests.first().id.`should be`(questId)
            }

            it("should not schedule for dates with deleted quest") {

                val repo = mockQuestsForRepeatingQuest(
                    listOf(
                        TestUtil.quest.copy(
                            id = questId,
                            originalScheduledDate = firstDateOfWeek,
                            isRemoved = true
                        )
                    )
                )

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest.copy(
                        repeatingPattern = RepeatingPattern.Yearly(
                            firstDateOfWeek.dayOfMonth,
                            firstDateOfWeek.monthValue
                        )
                    ),
                    start = firstDateOfWeek,
                    end = lastDateOfWeek,
                    questRepo = repo
                )
                quests.`should be empty`()
            }

            it("should schedule quests for 3 years") {
                val start = firstDateOfWeek
                val end = firstDateOfWeek.plusYears(3)
                val repo = mockQuestsForRepeatingQuest(listOf())

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest.copy(
                        repeatingPattern = RepeatingPattern.Yearly(
                            firstDateOfWeek.plusDays(1).dayOfMonth,
                            firstDateOfWeek.plusDays(1).monthValue
                        )
                    ),
                    start = start,
                    end = end,
                    questRepo = repo
                )
                quests.size.`should be`(3)
                quests.filter { it.id.isEmpty() }.size.`should be`(3)
            }

            it("should schedule 1 quest and use 2 stored quests for 3 years") {
                val start = firstDateOfWeek
                val end = firstDateOfWeek.plusYears(3)
                val repo = mockQuestsForRepeatingQuest(
                    listOf(
                        TestUtil.quest.copy(
                            id = "1",
                            originalScheduledDate = firstDateOfWeek.plusDays(1)
                        ),
                        TestUtil.quest.copy(
                            id = "2",
                            originalScheduledDate = firstDateOfWeek.plusDays(1).plusYears(2)
                        )
                    )
                )

                val quests = executeUseCase(
                    quest = TestUtil.repeatingQuest.copy(
                        repeatingPattern = RepeatingPattern.Yearly(
                            firstDateOfWeek.plusDays(1).dayOfMonth,
                            firstDateOfWeek.plusDays(1).monthValue
                        )
                    ),
                    start = start,
                    end = end,
                    questRepo = repo
                )
                quests.size.`should be`(3)
                quests.filter { it.id.isEmpty() }.size.`should be`(1)
            }
        }
    }
})