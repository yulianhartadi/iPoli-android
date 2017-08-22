package io.ipoli.android.repeatingquest.list.usecase

import io.ipoli.android.common.SimpleRxUseCase
import io.ipoli.android.repeatingquest.list.ui.RepeatingQuestListViewState
import io.ipoli.android.repeatingquest.list.ui.RepeatingQuestViewModel
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/22/17.
 */
class DisplayRepeatingQuestListUseCase(private val repeatingQuestRepository: RepeatingQuestRepository) : SimpleRxUseCase<RepeatingQuestListViewState>() {

    override fun createObservable(params: Unit): Observable<RepeatingQuestListViewState> {
        return repeatingQuestRepository.findAll()
            .map { repeatingQuests ->
                val viewModels = repeatingQuests.map { RepeatingQuestViewModel.create(it) }
                RepeatingQuestListViewState.DataLoaded(viewModels)
            }
            .cast(RepeatingQuestListViewState::class.java)
            .startWith(RepeatingQuestListViewState.Loading())
            .onErrorReturn { RepeatingQuestListViewState.Error(it) }
    }

}