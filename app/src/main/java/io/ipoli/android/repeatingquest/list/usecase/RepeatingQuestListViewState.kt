package io.ipoli.android.repeatingquest.list.usecase

import io.ipoli.android.repeatingquest.list.ui.RepeatingQuestViewModel

interface RepeatingQuestListViewState {

    class Loading : RepeatingQuestListViewState

    class Empty : RepeatingQuestListViewState

    data class Error(val error: Throwable) : RepeatingQuestListViewState

    data class DataLoaded(val repeatingQuests: List<RepeatingQuestViewModel>) : RepeatingQuestListViewState
}