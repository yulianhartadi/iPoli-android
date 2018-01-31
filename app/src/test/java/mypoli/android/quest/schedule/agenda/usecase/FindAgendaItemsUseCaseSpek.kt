package mypoli.android.quest.schedule.agenda.usecase

import mypoli.android.TestUtil
import mypoli.android.quest.schedule.agenda.usecase.FindAgendaDatesUseCase.Params.*
import mypoli.android.quest.data.persistence.QuestRepository
import org.amshove.kluent.`should be null`
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/29/2018.
 */
class FindAgendaItemsUseCaseSpek : Spek({

    describe("FindAgendaDatesUseCase") {

        fun executeUseCase(
            params: FindAgendaDatesUseCase.Params,
            questRepository: QuestRepository = TestUtil.questRepoMock()
        ) =
            FindAgendaDatesUseCase(questRepository).execute(params)

        val today = LocalDate.now()

        it("should require positive items for before") {
            val exec =
                { executeUseCase(Before(today, 0)) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should require positive items for after") {
            val exec =
                { executeUseCase(After(today, 0)) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should require positive items for all") {
            val exec =
                { executeUseCase(All(today, 0, 1)) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should give no after date when no quests are present") {
            val date = LocalDate.of(2018, 1, 11)
            val result =
                executeUseCase(
                    After(
                        date,
                        10
                    )
                )
            val res = (result as FindAgendaDatesUseCase.Result.After)
            res.date.`should be null`()
        }

        it("should give no before date when no quests are present") {
            val date = LocalDate.of(2018, 1, 11)
            val result =
                executeUseCase(
                    Before(
                        date,
                        10
                    )
                )
            val res = (result as FindAgendaDatesUseCase.Result.Before)
            res.date.`should be null`()
        }
    }
})