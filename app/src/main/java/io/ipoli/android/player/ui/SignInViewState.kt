package io.ipoli.android.player.ui

/**
 * Created by Venelin Valkov <venelin@ipoli.io> on 8/8/17.
 */
open class SignInViewState

class SignInInitialState : SignInViewState()

class SignInLoadingState : SignInViewState()

class PlayerSignedInState : SignInViewState()