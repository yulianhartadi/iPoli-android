package io.ipoli.android.util

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.jetbrains.spek.api.Spek

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/30/17.
 */
object RxSchedulersSpek : Spek({

    beforeGroup {
        RxAndroidPlugins.reset()
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        RxJavaPlugins.reset()
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
    }

    afterGroup {
        RxAndroidPlugins.reset()
        RxJavaPlugins.reset()
    }

})