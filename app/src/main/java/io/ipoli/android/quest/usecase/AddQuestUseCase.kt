package io.ipoli.android.quest.usecase

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.common.Validator.Companion.validate
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.persistence.QuestRepository
import io.ipoli.android.quest.usecase.Result.ValidationError.EMPTY_NAME
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/30/17.
 */
sealed class Result {

    enum class ValidationError {
        EMPTY_NAME
    }

    data class Added(val quest: Quest) : Result()
    data class Invalid(val errors: List<ValidationError>) : Result()
}

class AddQuestUseCase(private val questRepository: QuestRepository) : BaseRxUseCase<Quest, Result>() {
    override fun createObservable(quest: Quest): Observable<Result> {
        val valErrors = validate(quest)
            .check<Result.ValidationError> {
                "name" {
                    given { name.isEmpty() } addError EMPTY_NAME
                }
            }

        if (valErrors.isEmpty()) {
            questRepository.save(quest).subscribe({}, {})
        }
        return when {
            valErrors.isEmpty() -> Observable.just(Result.Added(quest))
            else -> Observable.just(Result.Invalid(valErrors))
        }
    }
}