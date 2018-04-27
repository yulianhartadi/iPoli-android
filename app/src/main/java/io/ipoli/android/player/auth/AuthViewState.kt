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
    //    data class SignUp(val username: String, val provider: AuthViewState.Provider) : AuthAction()
    data class Login(val provider: AuthViewState.Provider) : AuthAction()
    data class UsernameValidationFailed(val error: AuthViewState.ValidationError) : AuthAction()
    object AccountsLinked : AuthAction()
    object PlayerCreated : AuthAction()
    object PlayerLoggedIn : AuthAction()
    object SwitchViewType : AuthAction()
    object ExistingPlayerLoggedInFromGuest : AuthAction()
    object ShowSetUp : AuthAction()
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

//            is AuthAction.Login -> {
//                val type = when (action.provider) {
//                    AuthViewState.Provider.GOOGLE ->
//                        GOOGLE_AUTH_STARTED
//
//                    AuthViewState.Provider.FACEBOOK ->
//                        FACEBOOK_AUTH_STARTED
//
//                    AuthViewState.Provider.GUEST ->
//                        throw IllegalArgumentException("Guest can't log in")
//                }
//                subState.copy(
//                    type = type
//                )
//            }

            AuthAction.PlayerCreated -> {
                subState.copy(
                    type = PLAYER_CREATED
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

//            AuthAction.SwitchViewType -> {
//                val isLogin = !subState.isSetup
//
//                val type = if (isLogin)
//                    SHOW_SETUP
//                else
//                    SHOW_LOGIN
//
//                subState.copy(
//                    type = type,
//                    isSetup = isLogin
//                )
//            }

            AuthAction.AccountsLinked -> {
                subState.copy(
                    type = ACCOUNTS_LINKED
                )
            }

            else -> subState

        }

    override fun defaultState() =
        AuthViewState(
            type = LOADING,
            usernameValidationError = null,
            isSetup = false,
            isGuest = false
        )

}

data class AuthViewState(
    val type: AuthViewState.StateType,
    val usernameValidationError: ValidationError?,
    val isSetup: Boolean,
    val isGuest: Boolean
) : ViewState {
    enum class StateType {
        IDLE,
        LOADING,
        SHOW_LOGIN,
        SHOW_SETUP,
        SWITCH_TO_SETUP,
        USERNAME_VALIDATION_ERROR,
        PLAYER_CREATED,
        PLAYER_LOGGED_IN,
        EXISTING_PLAYER_LOGGED_IN_FROM_GUEST,
        ACCOUNTS_LINKED
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