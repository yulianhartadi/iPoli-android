package io.ipoli.android.planday.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/16/2018.
 */
class CalculateAwesomenessScoreUseCaseSpek : Spek({

    describe("CalculateAwesomenessScoreUseCase") {

        fun executeUseCase(
            completeCount: Int,
            totalCount: Int = completeCount
        ): CalculateAwesomenessScoreUseCase.AwesomenessScore {

            val cq = TestUtil.quest.copy(
                completedAtTime = Time.now(),
                completedAtDate = LocalDate.now()
            )

            val iq = TestUtil.quest

            val incompleteCount = totalCount - completeCount
            return CalculateAwesomenessScoreUseCase().execute(
                CalculateAwesomenessScoreUseCase.Params(
                    quests = 0.until(completeCount).map { cq } +
                        0.until(incompleteCount).map { iq }
                )
            )
        }

        it("should give F when no quests are completed") {
            executeUseCase(completeCount = 0).`should be`(
                CalculateAwesomenessScoreUseCase.AwesomenessScore.F
            )
        }

        it("should give F when 1 quest is completed and 1 is added") {
            executeUseCase(completeCount = 1).`should be`(
                CalculateAwesomenessScoreUseCase.AwesomenessScore.F
            )
        }

        it("should give D when 2 quests are completed and 2 are added") {
            executeUseCase(completeCount = 2).`should be`(
                CalculateAwesomenessScoreUseCase.AwesomenessScore.D
            )
        }

        it("should give C when 3 quests are completed and 3 are added") {
            executeUseCase(completeCount = 3).`should be`(
                CalculateAwesomenessScoreUseCase.AwesomenessScore.C
            )
        }

        it("should give B when 4 quests are completed and 4 are added") {
            executeUseCase(completeCount = 4).`should be`(
                CalculateAwesomenessScoreUseCase.AwesomenessScore.B
            )
        }

        it("should give A when 5 quests are completed and 5 are added") {
            executeUseCase(completeCount = 5).`should be`(
                CalculateAwesomenessScoreUseCase.AwesomenessScore.A
            )
        }

        it("should give B when 5 quests are completed and 6 are added") {
            executeUseCase(completeCount = 5, totalCount = 6).`should be`(
                CalculateAwesomenessScoreUseCase.AwesomenessScore.B
            )
        }

        it("should give A when 6 quests are completed and 6 are added") {
            executeUseCase(completeCount = 6, totalCount = 6).`should be`(
                CalculateAwesomenessScoreUseCase.AwesomenessScore.A
            )
        }

        it("should give F when 0 quests are completed and 15 are added") {
            executeUseCase(completeCount = 0, totalCount = 15).`should be`(
                CalculateAwesomenessScoreUseCase.AwesomenessScore.F
            )
        }
    }
})