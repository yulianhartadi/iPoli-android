package io.ipoli.android.quest.usecase

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.reactivex.Observable

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 10/9/17.
 */
class RemoveQuestUseCase(private val questRepository: QuestRepository) : BaseRxUseCase<String, Unit>() {
    override fun createObservable(parameters: String): Observable<Unit> {
        questRepository.delete(parameters)
        return Observable.just(Unit)
    }
}