package io.ipoli.android.planday.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/16/2018.
 */
class CalculateAwesomenessScoreUseCase :
    UseCase<CalculateAwesomenessScoreUseCase.Params, CalculateAwesomenessScoreUseCase.AwesomenessScore> {

    override fun execute(parameters: Params): AwesomenessScore {
        val qs = parameters.quests
        val completeCount = qs.count { it.isCompleted }
        if (qs.size <= 5) {
            return calculateSimpleAwesomenessScore(completeCount)
        }

        val percentComplete = completeCount / qs.size.toFloat()
        return when {
            percentComplete >= .85 -> AwesomenessScore.A
            percentComplete >= .7 -> AwesomenessScore.B
            percentComplete >= .5 -> AwesomenessScore.C
            percentComplete >= .35 -> AwesomenessScore.D
            else -> AwesomenessScore.F
        }
    }

    private fun calculateSimpleAwesomenessScore(completeCount: Int): AwesomenessScore {
        return when (completeCount) {
            2 -> AwesomenessScore.D
            3 -> AwesomenessScore.C
            4 -> AwesomenessScore.B
            5 -> AwesomenessScore.A
            else -> AwesomenessScore.F
        }
    }

    data class Params(val quests: List<Quest>)

    enum class AwesomenessScore {
        A, B, C, D, F
    }

}