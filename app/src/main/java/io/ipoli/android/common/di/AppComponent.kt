package io.ipoli.android.common.di

import com.facebook.CustomTabMainActivity
import dagger.Component
import io.ipoli.android.MainActivity
import javax.inject.Singleton

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/17.
 */
@Component(modules = arrayOf(AppModule::class))
@Singleton
interface AppComponent {
    fun inject(mainActivity: MainActivity)
}