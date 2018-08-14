package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase

class LogDataUseCase(private val challengeRepository: ChallengeRepository) :
    UseCase<LogDataUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        val log = parameters.log
        val challenge = challengeRepository.findById(parameters.challengeId)!!
        val newTrackedValues = challenge.trackedValues.map {
            if (it.id == parameters.trackedValueId) {

                val newHistory = it.history
                when (it) {
                    is Challenge.TrackedValue.Target -> {

                        if (it.isCumulative) {

                            newHistory[log.date] =
                                log.copy(value = log.value + (newHistory[log.date]?.value ?: 0.0))
                        } else {
                            newHistory[log.date] = log
                        }
                        it.copy(history = newHistory)
                    }
                    is Challenge.TrackedValue.Average -> {
                        newHistory[log.date] = log
                        it.copy(history = newHistory)
                    }
                    else -> throw IllegalArgumentException("Can't update tracked value of type $it")
                }
            } else it
        }
        challengeRepository.save(challenge.copy(trackedValues = newTrackedValues))
    }

    data class Params(
        val challengeId: String,
        val trackedValueId: String,
        val log: Challenge.TrackedValue.Log
    )
}