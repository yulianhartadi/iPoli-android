package mypoli.android

import android.app.Application
import android.content.Context
import android.util.Log
import com.bluelinelabs.conductor.Router
import com.crashlytics.android.Crashlytics
import com.evernote.android.job.JobManager
import com.github.moduth.blockcanary.BlockCanary
import com.github.moduth.blockcanary.BlockCanaryContext
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.fabric.sdk.android.Fabric
import mypoli.android.common.di.*
import mypoli.android.common.job.iPoliJobCreator
import space.traversal.kapsule.transitive
import timber.log.Timber


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 7/7/17.
 */

class iPoliApp : Application() {

    companion object {
        lateinit var refWatcher: RefWatcher

        fun controllerModule(context: Context, router: Router?) = ControllerModule(
            androidModule = MainAndroidModule(context),
            navigationModule = AndroidNavigationModule(router),
            repositoryModule = CouchbaseRepositoryModule(),
            useCaseModule = MainUseCaseModule(),
            presenterModule = AndroidPresenterModule()
        ).transitive()

        fun simpleModule(context: Context) = SimpleModule(
            androidModule = MainAndroidModule(context),
            repositoryModule = CouchbaseJobRepositoryModule(),
            useCaseModule = AndroidPopupUseCaseModule(),
            presenterModule = AndroidPopupPresenterModule()
        ).transitive()
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

            Fabric.with(Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(BuildConfig.DEBUG)
                .build())

            refWatcher = LeakCanary.install(this)
        }

        JobManager.create(this).addJobCreator(iPoliJobCreator())

        val currentUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler({ thread, exception ->
            Log.println(Log.ERROR, thread.name, Log.getStackTraceString(exception))
            currentUncaughtExceptionHandler.uncaughtException(thread, exception)
        })


//        val repo = CouchbaseQuestRepository(Database("iPoli", DatabaseConfiguration(this)), UI)
//        val q = Quest(
//            name = "Welcome",
//            color = Color.GREEN,
//            category = Category("Wellness", Color.GREEN),
//            plannedSchedule = QuestSchedule(LocalDate.now(), duration = 60, time = Time.at(15, 0)),
//            reminder = Reminder(Random().nextInt().toString(), "Welcome message", Time.at(20, 0), LocalDate.now())
//        )
//
//        repo.save(q)
//
//
//
//        val quests = repo.findNextQuestsToRemind(System.currentTimeMillis())
//        quests.forEach {
//            Timber.d("AAAA $it")
//        }
//        Timber.d("AAAAA $quests")

//        TinyDancer.create().show(this)
    }
}