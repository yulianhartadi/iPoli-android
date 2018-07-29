package io.ipoli.android.challenge.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.persistence.HabitRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/29/2018.
 */
class RemoveHabitFromChallengeUseCase(private val habitRepository: HabitRepository) :
    UseCase<RemoveHabitFromChallengeUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        habitRepository.removeFromChallenge(parameters.habitId)
    }

    data class Params(val habitId: String)
}