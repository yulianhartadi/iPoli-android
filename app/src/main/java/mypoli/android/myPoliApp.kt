package mypoli.android

import android.app.Application
import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.evernote.android.job.JobManager
import com.github.moduth.blockcanary.BlockCanary
import com.github.moduth.blockcanary.BlockCanaryContext
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.fabric.sdk.android.Fabric
import mypoli.android.common.di.*
import mypoli.android.common.job.myPoliJobCreator
import space.traversal.kapsule.transitive
import timber.log.Timber


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 7/7/17.
 */

class myPoliApp : Application() {

    private lateinit var module: Module

    companion object {
        lateinit var refWatcher: RefWatcher

        lateinit var instance: myPoliApp

        fun module(context: Context) =
            (context.applicationContext as myPoliApp).module
    }

    override fun onCreate() {
        super.onCreate()


        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }


        AndroidThreeTen.init(this)
        // Initialize Realm. Should only be done once when the application starts.
//        Realm.init(this)
//        val db = Database()
        Timber.plant(Timber.DebugTree())


//        Logger.addLogAdapter(AndroidLogAdapter())

//        Timber.plant(object : Timber.DebugTree() {
//            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//                Logger.log(priority, tag, message, t)
//            }
//        })

        if (!BuildConfig.DEBUG) {

            BlockCanary.install(this, object : BlockCanaryContext() {
                override fun provideBlockThreshold(): Int {
                    return 500
                }
            }).start()

            Fabric.with(
                Fabric.Builder(this)
                    .kits(Crashlytics())
                    .debuggable(BuildConfig.DEBUG)
                    .build()
            )

            refWatcher = LeakCanary.install(this)
        }

        JobManager.create(this).addJobCreator(myPoliJobCreator())

        val currentUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler({ thread, exception ->
            Log.println(Log.ERROR, thread.name, Log.getStackTraceString(exception))
            currentUncaughtExceptionHandler.uncaughtException(thread, exception)
        })


        module = Module(
            androidModule = MainAndroidModule(this),
            repositoryModule = CouchbaseRepositoryModule(),
            useCaseModule = MainUseCaseModule(),
            presenterModule = AndroidPresenterModule(),
            stateStoreModule = AndroidStateStoreModule()
        ).transitive()

        instance = this
    }
}