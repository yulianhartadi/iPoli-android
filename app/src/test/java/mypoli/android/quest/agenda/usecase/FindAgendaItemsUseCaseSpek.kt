package mypoli.android.quest.agenda.usecase

import mypoli.android.TestUtil
import mypoli.android.quest.data.persistence.QuestRepository
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
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
            FindAgendaItemsUseCase(questRepository).execute(params)

        val today = LocalDate.now()

        it("should require positive items for before") {
            val exec =
                { executeUseCase(FindAgendaItemsUseCase.Params.Before(today, 0)) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should require positive items for after") {
            val exec =
                { executeUseCase(FindAgendaItemsUseCase.Params.After(today, 0)) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should require positive items for all") {
            val exec =
                { executeUseCase(FindAgendaItemsUseCase.Params.All(today, 0, 1)) }
            exec shouldThrow IllegalArgumentException::class
        }

//        it("should return large range when no quests are present") {
//            val result =
//                executeUseCase(
//                    FindAgendaItemsUseCase.Params(
//                        date = LocalDate.of(2018, 1, 11),
//                        itemsBefore = 1,
//                        itemsAfter = 0
//                    )
//                )
//
//        }

        xit("should return after including today") {

        }
    }
})