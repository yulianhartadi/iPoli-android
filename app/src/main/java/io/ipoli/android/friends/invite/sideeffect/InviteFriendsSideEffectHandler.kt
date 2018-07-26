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

    override suspend fun doExecute(action: Action, state: AppState) {
        if (action is InviteFriendsAction.CreateLink) {
            if (!internetConnectionChecker.isConnected()) {
                dispatch(InviteFriendsAction.CreateLinkError(InviteFriendsAction.CreateLinkError.ErrorType.NO_INTERNET))
                return
            }
            try {
                dispatch(InviteFriendsAction.LinkReady(inviteLinkBuilder.create()))
            } catch (e: Throwable) {
                ErrorLogger.log(e)
                dispatch(InviteFriendsAction.CreateLinkError(InviteFriendsAction.CreateLinkError.ErrorType.UNKNOWN))
            }

        }
    }

    override fun canHandle(action: Action) = action is InviteFriendsAction

}