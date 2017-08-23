package io.ipoli.android.store.avatars

import io.ipoli.android.repeatingquest.list.ui.RepeatingQuestViewModel

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/23/17.
 */
interface AvatarListViewState {

    class Loading : AvatarListViewState

    data class Error(val error: Throwable) : AvatarListViewState

    data class DataLoaded(val avatars: List<AvatarViewModel>) : AvatarListViewState

    data class AvatarBought(val avatarViewModel : AvatarViewModel) : AvatarListViewState

    data class AvatarUsed(val avatarViewModel : AvatarViewModel) : AvatarListViewState
}