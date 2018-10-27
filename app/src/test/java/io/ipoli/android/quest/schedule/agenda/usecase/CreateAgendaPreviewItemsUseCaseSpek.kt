package io.ipoli.android.quest.schedule.agenda.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.event.Event
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

class CreateAgendaPreviewItemsUseCaseSpek : Spek({

    val today = LocalDate.now()

    describe("CreateAgendaPreviewItemsUseCase") {

        describe("month items") {

            fun monthItemsFor(
                startDate: LocalDate,
                endDate: LocalDate,
                quests: List<Quest>,
                events: List<Event>
            ) =
                CreateAgendaPreviewItemsUseCase().execute(
                    CreateAgendaPreviewItemsUseCase.Params(
                        startDate = startDate,
                        endDate = endDate,
                        quests = quests,
                        events = events
                    )
                ).monthItems

            it("should have empty list") {
                val items = monthItemsFor(today, today, emptyList(), emptyList())
                items.first().indicators.`should be empty`()
            }

            it("should have single indicator") {
                val q = TestUtil.quest.copy(
                    scheduledDate = today,
                    color = Color.GREEN
                )
                val items = monthItemsFor(today, today, listOf(q), emptyList())
                items.size.`should be equal to`(1)
                items.first().date.`should be`(today)
            }

            it("should have all day event first") {
                val q = TestUtil.quest.copy(
                    scheduledDate = today,
                    color = Color.GREEN
                )
                val e = TestUtil.event.copy(
                    isAllDay = true
                )
                val items = monthItemsFor(today, today, listOf(q), listOf(e))
                val todayItems = items.first()
                todayItems.indicators.size.`should be equal to`(2)
                todayItems.indicators.first()
                    .`should be instance of`(CreateAgendaPreviewItemsUseCase.MonthPreviewItem.Indicator.Event::class)
            }

            it("should sort Quests by duration") {
                val q1 = TestUtil.quest.copy(
                    scheduledDate = today,
                    color = Color.GREEN,
                    duration = 30
                )
                val q2 = TestUtil.quest.copy(
                    scheduledDate = today,
                    color = Color.GREEN,
                    duration = 60
                )
                val ids =
                    monthItemsFor(today, today, listOf(q1, q2), emptyList())
                        .first()
                        .indicators
                ids.size.`should be equal to`(2)
                ids.first().duration.`should be equal to`(60)
                ids[1].duration.`should be equal to`(30)
            }
        }

    }
})