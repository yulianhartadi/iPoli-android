package io.ipoli.android.planday.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/16/2018.
 */
class CalculateAwesomenessScoreUseCase :
    UseCase<CalculateAwesomenessScoreUseCase.Params, Double> {

    override fun execute(parameters: Params): Double {
        val qs = parameters.quests
        val completeCount = qs.count { it.isCompleted }
        if (qs.size <= 5) {
            return calculateSimpleAwesomenessScore(completeCount)
        }

        val percentComplete = completeCount / qs.size.toFloat()
        return when {
            percentComplete >= .85 -> 5.0
            percentComplete >= .7 -> 4.0
            percentComplete >= .5 -> 3.0
            percentComplete >= .35 -> 2.0
            else -> 0.0
        }
    }

    private fun calculateSimpleAwesomenessScore(completeCount: Int) =
        when (completeCount) {
            5 -> 5.0
            4 -> 4.0
            3 -> 3.0
            2 -> 2.0
            else -> 0.0
        }

    data class Params(val quests: List<Quest>)

}