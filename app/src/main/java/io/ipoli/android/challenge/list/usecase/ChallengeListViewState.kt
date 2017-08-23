package io.ipoli.android.challenge.list.usecase

import io.ipoli.android.challenge.list.ui.ChallengeViewModel

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/17.
 */
interface ChallengeListViewState {
    class Loading : ChallengeListViewState

    class Empty : ChallengeListViewState

    data class Error(val error: Throwable) : ChallengeListViewState

    data class DataLoaded(val data: List<ChallengeViewModel>) : ChallengeListViewState
}