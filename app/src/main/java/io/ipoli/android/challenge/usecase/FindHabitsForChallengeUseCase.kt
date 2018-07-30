package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.persistence.HabitRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/29/2018.
 */
class FindHabitsForChallengeUseCase(private val habitRepository: HabitRepository) :
    UseCase<FindHabitsForChallengeUseCase.Params, Challenge> {

    override fun execute(parameters: Params) =
        parameters.challenge.copy(
            habits = habitRepository.findNotRemovedForChallenge(parameters.challenge.id)
        )

    data class Params(val challenge: Challenge)
}