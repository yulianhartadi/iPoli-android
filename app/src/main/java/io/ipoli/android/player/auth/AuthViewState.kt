package io.ipoli.android.player.auth

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.player.auth.AuthViewState.StateType.*
import io.ipoli.android.player.data.Avatar

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/5/18.
 */
sealed class AuthAction : Action {
    object Load : AuthAction()
    data class Loaded(
        val hasPlayer: Boolean,
        val isGuest: Boolean,
        val hasUsername: Boolean
    ) : AuthAction()

    data class UserAuthenticated(val user: FirebaseUser) : AuthAction()
    data class UsernameValidationFailed(val error: AuthViewState.ValidationError) : AuthAction()
    data class CompleteSetup(
        val username: String,
        val avatar: Avatar
    ) : AuthAction()
    data class ValidateUsername(val username: String) : AuthAction()
    data class ChangeAvatar(val avatar: Avatar) : AuthAction()

    object AccountsLinked : AuthAction()
    object GuestCreated : AuthAction()
    object PlayerSetupCompleted : AuthAction()
    object PlayerLoggedIn : AuthAction()
    object ExistingPlayerLoggedInFromGuest : AuthAction()
    object ShowSetUp : AuthAction()
    object UsernameValid : AuthAction()
}

object AuthReducer : BaseViewStateReducer<AuthViewState>() {

    override val stateKey = key<AuthViewState>()

    override fun reduce(state: AppState, subState: AuthViewState, action: Action) =
        when (action) {
            is AuthAction.Loaded -> {
                val hasPlayer = action.hasPlayer
                val isGuest = action.isGuest
                val hasUsername = action.hasUsername
                subState.copy(
                    type = if (!hasPlayer || isGuest) {
                        SHOW_LOGIN
                    } else if (!hasUsername) {
                        SHOW_SETUP
                    } else {
                        throw  IllegalStateException("Player is already authenticated and has username")
                    },
                    isGuest = action.isGuest
                )
            }

            is AuthAction.UsernameValidationFailed -> {
                subState.copy(
                    type = USERNAME_VALIDATION_ERROR,
                    usernameValidationError = action.error
                )
            }

            AuthAction.UsernameValid -> {
                subState.copy(
                    type = USERNAME_VALID
                )
            }

            AuthAction.GuestCreated -> {
                subState.copy(
                    type = GUEST_CREATED
                )
            }

            AuthAction.PlayerSetupCompleted -> {
                subState.copy(
                    type = PLAYER_SETUP_COMPLETED
                )
            }

            AuthAction.PlayerLoggedIn -> {
                subState.copy(
                    type = PLAYER_LOGGED_IN
                )
            }

            AuthAction.ExistingPlayerLoggedInFromGuest -> {
                subState.copy(
                    type = EXISTING_PLAYER_LOGGED_IN_FROM_GUEST
                )
            }

            AuthAction.ShowSetUp -> {
                subState.copy(
                    type = SWITCH_TO_SETUP
                )
            }

            AuthAction.AccountsLinked -> {
                subState.copy(
                    type = ACCOUNTS_LINKED
                )
            }

            is AuthAction.ChangeAvatar -> {
                subState.copy(
                    type = AVATAR_CHANGED,
                    playerAvatar = action.avatar
                )
            }

            else -> subState

        }

    override fun defaultState() =
        AuthViewState(
            type = LOADING,
            usernameValidationError = null,
            isGuest = false,
            playerAvatar = Avatar.AVATAR_03,
            avatars = listOf(
                Avatar.AVATAR_03,
                Avatar.AVATAR_02,
                Avatar.AVATAR_01,
                Avatar.AVATAR_04,
                Avatar.AVATAR_05,
                Avatar.AVATAR_06,
                Avatar.AVATAR_07,
                Avatar.AVATAR_11
            )
        )

}

data class AuthViewState(
    val type: AuthViewState.StateType,
    val usernameValidationError: ValidationError?,
    val isGuest: Boolean,
    val playerAvatar : Avatar,
    val avatars : List<Avatar>
) : ViewState {
    enum class StateType {
        IDLE,
        LOADING,
        SHOW_LOGIN,
        SHOW_SETUP,
        SWITCH_TO_SETUP,
        USERNAME_VALIDATION_ERROR,
        USERNAME_VALID,
        GUEST_CREATED,
        PLAYER_SETUP_COMPLETED,
        PLAYER_LOGGED_IN,
        EXISTING_PLAYER_LOGGED_IN_FROM_GUEST,
        ACCOUNTS_LINKED,
        AVATAR_CHANGED
    }

    enum class ValidationError {
        EMPTY_USERNAME,
        EXISTING_USERNAME,
        INVALID_FORMAT,
        INVALID_LENGTH
    }

    enum class Provider {
        FACEBOOK, GOOGLE, GUEST
    }
}

fun AuthViewState.usernameErrorMessage(context: Context) =
    usernameValidationError?.let {
        when (it) {
            AuthViewState.ValidationError.EMPTY_USERNAME -> context.getString(R.string.username_is_empty)
            AuthViewState.ValidationError.EXISTING_USERNAME -> context.getString(R.string.username_is_taken)
            AuthViewState.ValidationError.INVALID_FORMAT -> context.getString(R.string.username_wrong_format)
            AuthViewState.ValidationError.INVALID_LENGTH -> context.getString(
                R.string.username_wrong_length,
                Constants.USERNAME_MIN_LENGTH,
                Constants.USERNAME_MAX_LENGTH
            )
        }
    }