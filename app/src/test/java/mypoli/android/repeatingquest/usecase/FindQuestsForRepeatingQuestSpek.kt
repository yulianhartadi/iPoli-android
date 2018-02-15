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
import org.amshove.kluent.*
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
                    end = end,
                    firstDayOfWeek = DayOfWeek.MONDAY,
                    lastDayOfWeek = DayOfWeek.SUNDAY
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

        it("should not schedule anything before quest start") {
            val quests = executeUseCase(
                quest = TestUtil.repeatingQuest.copy(start = lastDateOfWeek.plusDays(1)),
                start = firstDateOfWeek,
                end = lastDateOfWeek
            )

            quests.`should be empty`()
        }

        it("should not schedule anything after quest end") {
            val quests = executeUseCase(
                quest = TestUtil.repeatingQuest.copy(
                    start = firstDateOfWeek.minusDays(2),
                    end = firstDateOfWeek.minusDays(1)
                ),
                start = firstDateOfWeek,
                end = lastDateOfWeek
            )

            quests.`should be empty`()
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

            describe("flexible") {

                fun createQuest(
                    timesPerWeek: Int,
                    preferredDays: Set<DayOfWeek> = setOf()
                ): RepeatingQuest {
                    return TestUtil.repeatingQuest.copy(
                        repeatingPattern = RepeatingPattern.Flexible.Weekly(
                            timesPerWeek = timesPerWeek,
                            preferredDays = preferredDays
                        )
                    )
                }

                it("should not allow 1 time per week with 1 preferred day") {


                    val exec = {

                        executeUseCase(
                            createQuest(
                                timesPerWeek = 1,
                                preferredDays = setOf(DayOfWeek.MONDAY)
                            ),
                            firstDateOfWeek,
                            lastDateOfWeek
                        )
                    }
                    exec shouldThrow IllegalArgumentException::class
                }

                it("should not allow to repeat less than once per week") {
                    val exec = {
                        executeUseCase(
                            createQuest(
                                timesPerWeek = 0,
                                preferredDays = setOf(DayOfWeek.MONDAY)
                            ),
                            firstDateOfWeek,
                            lastDateOfWeek
                        )
                    }
                    exec shouldThrow IllegalArgumentException::class
                }

                it("should not allow to repeat more than 7 times per week") {
                    val exec = {
                        executeUseCase(
                            createQuest(
                                timesPerWeek = 8,
                                preferredDays = setOf(DayOfWeek.MONDAY)
                            ),
                            firstDateOfWeek,
                            lastDateOfWeek
                        )
                    }
                    exec shouldThrow IllegalArgumentException::class
                }

                it("should schedule 1 quest without preferred days") {
                    val quests = executeUseCase(
                        createQuest(
                            timesPerWeek = 1
                        ),
                        firstDateOfWeek,
                        lastDateOfWeek
                    )

                    quests.size.`should be`(1)
                }

                it("should schedule 1 quest with 2 preferred days") {
                    val preferredDays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
                    val quests = executeUseCase(
                        createQuest(
                            timesPerWeek = 1,
                            preferredDays = preferredDays
                        ),
                        firstDateOfWeek,
                        lastDateOfWeek
                    )

                    quests.size.`should be`(1)
                    val scheduledDate = quests.first().scheduledDate
                    scheduledDate.dayOfWeek.`should be in`(preferredDays)
                }

                it("should schedule 2 quest with 1 preferred day") {
                    val preferredDays = setOf(DayOfWeek.FRIDAY)
                    val quests = executeUseCase(
                        createQuest(
                            timesPerWeek = 2,
                            preferredDays = preferredDays
                        ),
                        firstDateOfWeek,
                        lastDateOfWeek
                    )

                    quests.size.`should be`(2)
                    val scheduledDates = quests.map { it.scheduledDate }
                    scheduledDates.filter { it.dayOfWeek == DayOfWeek.FRIDAY }.size.`should be`(1)
                }

                it("should schedule 3 quest with 4 preferred day") {
                    val preferredDays = setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                    )
                    val quests = executeUseCase(
                        createQuest(
                            timesPerWeek = 3,
                            preferredDays = preferredDays
                        ),
                        firstDateOfWeek.with(DayOfWeek.WEDNESDAY),
                        lastDateOfWeek
                    )

                    quests.size.`should be in range`(1, 2)
                    val possibleWeekDays = setOf(
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                    )
                    if (quests.size == 1) {
                        quests.first().scheduledDate.dayOfWeek.`should be in`(
                            possibleWeekDays
                        )
                    } else {
                        quests.map { it.scheduledDate.dayOfWeek }.`should contain all`(
                            possibleWeekDays
                        )
                    }
                }

                it("should schedule 6 quest for 2 days") {
                    val quests = executeUseCase(
                        createQuest(
                            timesPerWeek = 5
                        ),
                        firstDateOfWeek.with(DayOfWeek.SATURDAY),
                        lastDateOfWeek
                    )

                    quests.size.`should be in range`(1, 2)
                    val possibleWeekDays = setOf(
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY
                    )
                    if (quests.size == 1) {
                        quests.first().scheduledDate.dayOfWeek.`should be`(
                            possibleWeekDays
                        )
                    } else {
                        quests.map { it.scheduledDate.dayOfWeek }.`should contain all`(
                            possibleWeekDays
                        )
                    }
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