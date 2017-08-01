package io.ipoli.android.di

import dagger.Component
import io.ipoli.android.rewards.RewardsPresenter
import javax.inject.Singleton

/**
 * Created by vini on 8/1/17.
 */
@Singleton
@Component(modules = arrayOf(
        AppModule::class,
        RepositoryModule::class
))
interface RewardsComponent {

    fun inject(presenter: RewardsPresenter)
}