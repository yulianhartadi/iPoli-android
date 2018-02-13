package mypoli.android.auth

import com.google.firebase.auth.FirebaseUser
import mypoli.android.common.AppState
import mypoli.android.common.AppStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/5/18.
 */
sealed class AuthAction : Action {
    object Load : AuthAction()
    data class LoadSignUp(val isGuest: Boolean) : AuthAction()
    data class CompleteUserAuth(val user: FirebaseUser, val username: String) : AuthAction()
    data class SignUp(val username: String, val provider: AuthState.Provider) : AuthAction()
    data class Login(val provider: AuthState.Provider) : AuthAction()
    data class UsernameValidationFailed(val error: AuthState.ValidationError) : AuthAction()
    data class StartSignUp(val provider: AuthState.Provider) : AuthAction()
    object AccountsLinked : AuthAction()
    object PlayerCreated : AuthAction()
    object PlayerLoggedIn : AuthAction()
    object SwitchViewType : AuthAction()
    object DeleteAccount : AuthAction()
    object SignOutAccount : AuthAction()
    object GuestPlayerLoggedIn : AuthAction()
}

data class AuthState(
    val type: StateType,
    val usernameValidationError: ValidationError?,
    val isGuest: Boolean,
    val isLogin: Boolean
) : State {
    enum class StateType {
        IDLE,
        LOADING,
        SHOW_SIGN_UP,
        SHOW_LOGIN,
        GOOGLE_AUTH_STARTED,
        FACEBOOK_AUTH_STARTED,
        GUEST_AUTH_STARTED,
        START_GOOGLE_LOGIN,
        START_FACEBOOK_LOGIN,
        USERNAME_VALIDATION_ERROR,
        PLAYER_CREATED,
        PLAYER_LOGGED_IN,
        GUEST_PLAYER_LOGGED_IN,
        ACCOUNTS_LINKED,
        DELETE_ACCOUNT,
        SIGN_OUT_ACCOUNT
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

object AuthReducer : AppStateReducer<AuthState> {
    override fun reduce(state: AppState, action: Action): AuthState {

        val authState = state.authState

        return when (action) {
            is AuthAction.LoadSignUp -> {
                authState.copy(
                    type = AuthState.StateType.SHOW_SIGN_UP,
                    isGuest = action.isGuest
                )
            }

            is AuthAction.StartSignUp -> {
                val type = when (action.provider) {
                    AuthState.Provider.GOOGLE ->
                        AuthState.StateType.GOOGLE_AUTH_STARTED

                    AuthState.Provider.FACEBOOK ->
                        AuthState.StateType.FACEBOOK_AUTH_STARTED

                    AuthState.Provider.GUEST ->
                        AuthState.StateType.GUEST_AUTH_STARTED
                }

                return authState.copy(type = type)
            }

            is AuthAction.UsernameValidationFailed -> {
                authState.copy(
                    type = AuthState.StateType.USERNAME_VALIDATION_ERROR,
                    usernameValidationError = action.error
                )
            }

            is AuthAction.Login -> {
                val type = when (action.provider) {
                    AuthState.Provider.GOOGLE ->
                        AuthState.StateType.GOOGLE_AUTH_STARTED

                    AuthState.Provider.FACEBOOK ->
                        AuthState.StateType.FACEBOOK_AUTH_STARTED

                    AuthState.Provider.GUEST ->
                        throw IllegalArgumentException("Guest can't log in")
                }
                authState.copy(
                    type = type
                )
            }

            AuthAction.PlayerCreated -> {
                authState.copy(
                    type = AuthState.StateType.PLAYER_CREATED
                )
            }

            AuthAction.PlayerLoggedIn -> {
                authState.copy(
                    type = AuthState.StateType.PLAYER_LOGGED_IN
                )
            }

            AuthAction.GuestPlayerLoggedIn -> {
                authState.copy(
                    type = AuthState.StateType.GUEST_PLAYER_LOGGED_IN
                )
            }

            AuthAction.SwitchViewType -> {
                val isLogin = !authState.isLogin

                val type = if (isLogin)
                    AuthState.StateType.SHOW_LOGIN
                else
                    AuthState.StateType.SHOW_SIGN_UP

                authState.copy(
                    type = type,
                    isLogin = isLogin
                )
            }

            AuthAction.AccountsLinked -> {
                authState.copy(
                    type = AuthState.StateType.ACCOUNTS_LINKED
                )
            }

            AuthAction.DeleteAccount -> {
                authState.copy(
                    type = AuthState.StateType.DELETE_ACCOUNT
                )
            }

            AuthAction.SignOutAccount-> {
                authState.copy(
                    type = AuthState.StateType.SIGN_OUT_ACCOUNT
                )
            }

            else -> authState
        }

    }

    override fun defaultState() =
        AuthState(
            type = AuthState.StateType.LOADING,
            usernameValidationError = null,
            isLogin = false,
            isGuest = false
        )

}

data class AuthViewState(
    val type: AuthState.StateType,
    val usernameErrorMessage: String? = null,
    val isLogin: Boolean,
    val showGuestSignUp: Boolean
) : ViewState