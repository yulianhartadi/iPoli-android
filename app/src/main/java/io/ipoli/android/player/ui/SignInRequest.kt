package io.ipoli.android.player.ui

import io.ipoli.android.player.auth.ProviderType
import io.ipoli.android.player.auth.RxSocialAuth

data class SignInRequest(val username: String, val existingPlayer: Boolean, val providerType: ProviderType, val socialAuth: RxSocialAuth)