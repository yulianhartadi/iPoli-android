package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.persistence.HabitRepository

class RemoveHabitUseCase(private val habitRepository: HabitRepository) :
    UseCase<RemoveHabitUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        habitRepository.remove(parameters.habitId)
    }

    data class Params(val habitId: String)
}