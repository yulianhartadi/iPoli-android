package io.ipoli.android.friends.invite.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.friends.invite.AcceptFriendshipAction
import space.traversal.kapsule.required

object AcceptFriendshipSideEffectHandler : AppSideEffectHandler() {

    private val friendRepository by required { friendRepository }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is AcceptFriendshipAction.Load -> {

                if (friendRepository.isFriend(action.invitePlayerId)) {
                    dispatch(AcceptFriendshipAction.AlreadyFriends)
                    return
                }

                val friend = friendRepository.find(action.invitePlayerId)

                dispatch(
                    DataLoadedAction.AcceptFriendshipDataChanged(
                        friend.avatar,
                        friend.displayName,
                        friend.username
                    )
                )
            }

            is AcceptFriendshipAction.Accept -> {
                friendRepository.friend(action.invitePlayerId)
            }
        }
    }

    override fun canHandle(action: Action) = action is AcceptFriendshipAction

}