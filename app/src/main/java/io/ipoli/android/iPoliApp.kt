package io.ipoli.android

import android.app.Application
import android.content.Context
import com.codemonkeylabs.fpslibrary.TinyDancer
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.squareup.leakcanary.LeakCanary
import io.ipoli.android.di.AppComponent
import io.ipoli.android.di.AppModule
import io.ipoli.android.di.DaggerAppComponent
import io.realm.Realm
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/17.
 */
class iPoliApp : Application() {

    companion object {
        private var component: AppComponent? = null

        fun getComponent(c: Context): AppComponent {
            if (component == null) {
                component = DaggerAppComponent.builder()
                    .appModule(AppModule(c.applicationContext))
                    .build()
            }
            return component!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this)
        Logger.addLogAdapter(AndroidLogAdapter())
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                Logger.log(priority, tag, message, t)
            }
        })
        LeakCanary.install(this)
//        TinyDancer.create().show(this)
    }
}