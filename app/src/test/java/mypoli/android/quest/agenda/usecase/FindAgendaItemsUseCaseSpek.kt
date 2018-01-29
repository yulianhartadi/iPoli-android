package mypoli.android.quest.agenda.usecase

import mypoli.android.TestUtil
import mypoli.android.quest.data.persistence.QuestRepository
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/29/2018.
 */
class FindAgendaItemsUseCaseSpek : Spek({

    describe("FindAgendaItemsUseCase") {

        fun executeUseCase(
            params: FindAgendaItemsUseCase.Params,
            questRepository: QuestRepository = TestUtil.questRepoMock()
        ) =
            FindAgendaItemsUseCase(questRepository).execute(
                FindAgendaItemsUseCase.Params(
                    params.date,
                    params.itemCount,
                    params.findBefore,
                    DayOfWeek.MONDAY
                )
            )

        it("should require non negative item count") {
            val exec =
                { executeUseCase(FindAgendaItemsUseCase.Params(LocalDate.now(), -1, false)) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should return empty list when no items are requested") {
            val result = executeUseCase(FindAgendaItemsUseCase.Params(LocalDate.now(), 0, false))
            result.agendaItems.`should be empty`()
        }

        it("should give empty week item when at start of week") {
            val start = LocalDate.of(2018, 1, 8)
            val result =
                executeUseCase(FindAgendaItemsUseCase.Params(start, 1, false))
            result.agendaItems.size.`should be equal to`(1)
            val agendaItem = result.agendaItems.first()
            agendaItem.`should be instance of`(FindAgendaItemsUseCase.AgendaItem.Week::class)
            val weekItem = agendaItem as FindAgendaItemsUseCase.AgendaItem.Week
            weekItem.start.`should equal`(start)
            weekItem.end.`should equal`(start.with(DayOfWeek.SUNDAY))
        }

        it("should give empty week item when at mid of week") {
            val start = LocalDate.of(2018, 1, 10)
            val result =
                executeUseCase(FindAgendaItemsUseCase.Params(start, 1, false))
            result.agendaItems.size.`should be equal to`(1)
            val agendaItem = result.agendaItems.first()
            agendaItem.`should be instance of`(FindAgendaItemsUseCase.AgendaItem.Week::class)
            val weekItem = agendaItem as FindAgendaItemsUseCase.AgendaItem.Week
            weekItem.start.`should equal`(start.with(DayOfWeek.MONDAY))
            weekItem.end.`should equal`(start.with(DayOfWeek.SUNDAY))
        }
    }
})