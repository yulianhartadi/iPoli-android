package io.ipoli.android.friends.invite.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.ErrorLogger
import io.ipoli.android.common.redux.Action
import io.ipoli.android.friends.invite.InviteFriendsAction
import space.traversal.kapsule.required

object InviteFriendsSideEffectHandler : AppSideEffectHandler() {

    private val internetConnectionChecker by required { internetConnectionChecker }
    private val inviteLinkBuilder by required { inviteLinkBuilder }
    private val playerRepository by required { playerRepository }

    override suspend fun doExecute(action: Action, state: AppState) {
        if (action is InviteFriendsAction.CreateLink) {
            if (!internetConnectionChecker.isConnected()) {
                dispatch(InviteFriendsAction.CreateLinkError(InviteFriendsAction.CreateLinkError.ErrorType.NO_INTERNET))
                return
            }
            try {
                val link = inviteLinkBuilder.create()
                val p = playerRepository.find()!!
                val newPlayer = playerRepository.save(
                    p.copy(
                        statistics = p.statistics.copy(
                            inviteForFriendCount = Math.max(
                                p.statistics.inviteForFriendCount - 1,
                                0
                            )
                        )
                    )
                )
                dispatch(InviteFriendsAction.LinkReady(link, newPlayer.statistics.inviteForFriendCount.toInt()))
            } catch (e: Throwable) {
                ErrorLogger.log(e)
                dispatch(InviteFriendsAction.CreateLinkError(InviteFriendsAction.CreateLinkError.ErrorType.UNKNOWN))
            }

        }
    }

    override fun canHandle(action: Action) = action is InviteFriendsAction

}