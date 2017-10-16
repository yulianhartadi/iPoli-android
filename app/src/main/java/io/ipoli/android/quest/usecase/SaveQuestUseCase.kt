package io.ipoli.android.quest.usecase

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.common.Validator.Companion.validate
import io.ipoli.android.quest.data.RealmQuest
import io.ipoli.android.quest.data.persistence.QuestRepository
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

    data class Added(val realmQuest: RealmQuest) : Result()
    data class Invalid(val errors: List<ValidationError>) : Result()
}

class SaveQuestUseCase(private val questRepository: QuestRepository) : BaseRxUseCase<RealmQuest, Result>() {
    override fun createObservable(parameters: RealmQuest): Observable<Result> {
        val quest = parameters
        val valErrors = validate(quest)
            .check<Result.ValidationError> {
                "name" {
                    given { name.isEmpty() } addError EMPTY_NAME
                }
            }

        if (valErrors.isEmpty()) {
            questRepository.save(quest).subscribe()
        }
        return when {
            valErrors.isEmpty() -> Observable.just(Result.Added(quest))
            else -> Observable.just(Result.Invalid(valErrors))
        }
    }
}