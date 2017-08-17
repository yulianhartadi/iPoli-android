package io.ipoli.android.player

import io.ipoli.android.BaseRxUseCase
import io.ipoli.android.auth.ProviderType
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.realm.Realm
import io.realm.SyncConfiguration
import io.realm.SyncCredentials
import io.realm.SyncUser
import java.util.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/17.
 */
class SignInUseCase(private val playerRepository: PlayerRepository, subscribeOnScheduler: Scheduler?, observeOnScheduler: Scheduler?) : BaseRxUseCase<SignInRequest, SignInStatePartialChange>(subscribeOnScheduler, observeOnScheduler) {

    override fun createObservable(params: SignInRequest): Observable<SignInStatePartialChange> {
        return params.socialAuth.login(params.username)
                .flatMap { (token, authProvider) ->
                    if (params.providerType == ProviderType.ANONYMOUS) {
                        return@flatMap PlayerRepository().save(Player(UUID.randomUUID().toString(), authProvider = authProvider))
                    }

                    val credentials = if (params.providerType == ProviderType.FACEBOOK) {
                        SyncCredentials.facebook(token)
                    } else {
                        SyncCredentials.google(token)
                    }
                    val authURL = "http://10.0.2.2:9080/auth"
                    val user = SyncUser.login(credentials, authURL)
                    val serverURL = "realm://10.0.2.2:9080/~/default"
                    val configuration = SyncConfiguration.Builder(user, serverURL).build()
                    Realm.setDefaultConfiguration(configuration)
                    playerRepository.save(Player(user.identity, authProvider = authProvider))
                }.toObservable().map {
            PlayerSignedInPartialChange() as SignInStatePartialChange
        }.startWith(SignInLoadingPartialChange())
    }
}