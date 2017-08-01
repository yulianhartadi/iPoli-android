package io.ipoli.android.di

import dagger.Component
import javax.inject.Singleton

/**
 * Created by vini on 8/1/17.
 */
@Component(modules = arrayOf(AppModule::class))
@Singleton
interface AppComponent {

}