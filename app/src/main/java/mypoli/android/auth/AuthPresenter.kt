package mypoli.android.auth

import android.content.Context
import mypoli.android.Constants
import mypoli.android.R
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/5/18.
 */
class AuthPresenter : AndroidStatePresenter<AppState, AuthViewState> {
    override fun present(state: AppState, context: Context): AuthViewState {

        val authState = state.authState

        val vs = AuthViewState(
            type = authState.type,
            isLogin = authState.isLogin,
            showGuestSignUp = !authState.isGuest
        )

        if (authState.type == AuthState.StateType.USERNAME_VALIDATION_ERROR) {
            val error = authState.usernameValidationError!!
            return vs.copy(
                usernameErrorMessage = when (error) {
                    AuthState.ValidationError.EMPTY_USERNAME -> context.getString(R.string.username_is_empty)
                    AuthState.ValidationError.EXISTING_USERNAME -> context.getString(R.string.username_is_taken)
                    AuthState.ValidationError.INVALID_FORMAT -> context.getString(R.string.username_wrong_format)
                    AuthState.ValidationError.INVALID_LENGTH -> context.getString(
                        R.string.username_wrong_length,
                        Constants.USERNAME_MIN_LENGTH,
                        Constants.USERNAME_MAX_LENGTH
                    )
                }
            )
        }

        return vs
    }

}