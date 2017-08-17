package io.ipoli.android.player

import io.ipoli.android.auth.RxSocialAuth

data class SignInRequest(val username: String, val existingPlayer: Boolean, val socialAuth: RxSocialAuth)