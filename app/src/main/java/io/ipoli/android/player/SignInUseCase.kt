package io.ipoli.android.player

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.player.auth.AuthProvider
import io.ipoli.android.player.auth.ProviderType
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.ui.PlayerSignedInPartialChange
import io.ipoli.android.player.ui.SignInLoadingPartialChange
import io.ipoli.android.player.ui.SignInRequest
import io.ipoli.android.player.ui.SignInStatePartialChange
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Realm
import io.realm.SyncConfiguration
import io.realm.SyncCredentials
import io.realm.SyncUser

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/17.
 */
class SignInUseCase(private val playerRepository: PlayerRepository) : BaseRxUseCase<SignInRequest, SignInStatePartialChange>() {

    override fun createObservable(parameters: SignInRequest): Observable<SignInStatePartialChange> {
        return parameters.socialAuth.login(parameters.username)
            .flatMap { (token, authProvider) ->
                if (parameters.providerType == ProviderType.ANONYMOUS) {
                    saveGuestPlayer(authProvider)
                } else {
                    saveAuthenticatedPlayer(parameters, token, authProvider)
                }
            }.toObservable().map {
            PlayerSignedInPartialChange() as SignInStatePartialChange
        }.startWith(SignInLoadingPartialChange())
    }

    private fun saveGuestPlayer(authProvider: AuthProvider) =
        playerRepository.save(Player(authProvider = authProvider))

    private fun saveAuthenticatedPlayer(params: SignInRequest, token: String, authProvider: AuthProvider): Single<Player> {
        val credentials = createSyncCredentials(params, token)
        val authURL = "http://10.0.2.2:9080/auth"
        val user = SyncUser.login(credentials, authURL)
        val serverURL = "realm://10.0.2.2:9080/~/default"
        Realm.setDefaultConfiguration(
            SyncConfiguration.Builder(user, serverURL)
                .build()
        )
        return playerRepository.save(Player(user.identity, authProvider = authProvider))
    }

    private fun createSyncCredentials(params: SignInRequest, token: String): SyncCredentials {
        if (params.providerType == ProviderType.FACEBOOK) {
            return SyncCredentials.facebook(token)
        } else {
            return SyncCredentials.google(token)
        }
    }
}