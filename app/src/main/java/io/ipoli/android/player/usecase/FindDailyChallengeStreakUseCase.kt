package io.ipoli.android.player.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.dailychallenge.data.persistence.DailyChallengeRepository

class FindDailyChallengeStreakUseCase(private val dailyChallengeRepository: DailyChallengeRepository) :
    UseCase<Unit, Int> {

    override fun execute(parameters: Unit) =
        dailyChallengeRepository.findDailyChallengeStreak()
}