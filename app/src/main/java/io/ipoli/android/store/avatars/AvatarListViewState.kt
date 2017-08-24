package io.ipoli.android.store.avatars

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/23/17.
 */
data class AvatarListViewState(val loading: Boolean = false,
                               val error: Throwable? = null,
                               val isDataNew : Boolean = false,
                               val avatars: List<AvatarViewModel>? = null,
                               val boughtAvatar: AvatarViewModel? = null,
                               val usedAvatar: AvatarViewModel? = null,
                               val avatarTooExpensive: AvatarViewModel? = null)


interface AvatarListPartialChange {
    fun computeNewState(prevState: AvatarListViewState): AvatarListViewState

    class Loading : AvatarListPartialChange {
        override fun computeNewState(prevState: AvatarListViewState): AvatarListViewState {
            return AvatarListViewState(loading = true, avatars = prevState.avatars)
        }
    }

    class Error(private val error: Throwable) : AvatarListPartialChange {
        override fun computeNewState(prevState: AvatarListViewState): AvatarListViewState {
            return AvatarListViewState(error = error)
        }

    }

    class DataLoaded(private val avatars: List<AvatarViewModel>) : AvatarListPartialChange {
        override fun computeNewState(prevState: AvatarListViewState): AvatarListViewState {
            return AvatarListViewState(avatars = avatars, isDataNew = true)
        }
    }

    class AvatarBought(private val avatar: AvatarViewModel) : AvatarListPartialChange {
        override fun computeNewState(prevState: AvatarListViewState): AvatarListViewState {
            return AvatarListViewState(avatars = prevState.avatars, boughtAvatar = avatar)
        }
    }

    class AvatarUsed(private val avatar: AvatarViewModel) : AvatarListPartialChange {
        override fun computeNewState(prevState: AvatarListViewState): AvatarListViewState {
            return AvatarListViewState(avatars = prevState.avatars, usedAvatar = avatar)
        }
    }

    class AvatarTooExpensive(private val avatar: AvatarViewModel) : AvatarListPartialChange {
        override fun computeNewState(prevState: AvatarListViewState): AvatarListViewState {
            return AvatarListViewState(avatars = prevState.avatars, avatarTooExpensive = avatar)
        }
    }

}