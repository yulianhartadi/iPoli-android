package io.ipoli.android

import android.app.Application
import android.content.Context
import com.bluelinelabs.conductor.Router
import com.crashlytics.android.Crashlytics
import com.github.moduth.blockcanary.BlockCanary
import com.github.moduth.blockcanary.BlockCanaryContext
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.fabric.sdk.android.Fabric
import io.ipoli.android.common.di.*
import io.realm.Realm
import space.traversal.kapsule.transitive
import timber.log.Timber


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 7/7/17.
 */

class iPoliApp : Application() {

    companion object {
        lateinit var refWatcher: RefWatcher

        fun module(context: Context, router: Router) = Module(
            androidModule = MainAndroidModule(context, router),
            repositoryModule = RealmRepositoryModule(),
            useCaseModule = MainUseCaseModule(),
            presenterModule = AndroidPresenterModule()
        ).transitive()
    }

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        BlockCanary.install(this, object : BlockCanaryContext() {
            override fun provideBlockThreshold(): Int {
                return 500
            }
        }).start()

        Fabric.with(Fabric.Builder(this)
            .kits(Crashlytics())
            .debuggable(BuildConfig.DEBUG)
            .build())

        AndroidThreeTen.init(this)

        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this)
        Timber.plant(Timber.DebugTree())
//        Logger.addLogAdapter(AndroidLogAdapter())

//        Timber.plant(object : Timber.DebugTree() {
//            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//                Logger.log(priority, tag, message, t)
//            }
//        })
        refWatcher = LeakCanary.install(this)

//        TinyDancer.create().show(this)

//
//        val rewardRepository = RealmRewardRepository()
//        rewardRepository.save(Reward(name = "Reward 1", description = "desc1", price = 100)).subscribe()
//        rewardRepository.save(Reward(name = "Reward 2", description = "desc 2", price = 200)).subscribe()
//        rewardRepository.save(Reward(name = "Reward 3", description = "desc 3", price = 300)).subscribe()

//        val questRepository = RealmQuestRepository()
//        val quest = Quest("Tomorrow", LocalDate.now().plusDays(1), Category.PERSONAL)
//        quest.setDuration(30)
//        quest.startMinute = Time.at(14, 0).toMinuteOfDay()
//        quest.completedAtDate = LocalDate.now()
//        quest.completedAtMinute = 380
//        questRepository.save(quest).subscribe()

//        val repeatingQuestRepository = RealmRepeatingQuestRepository()
//
//        val rq = RepeatingQuest("Wakka")
//        rq.name = "Doodle"
//        rq.setDuration(20)
//        rq.recurrence = Recurrence.create()
//        val quest = Quest("Piki")
//        quest.id = UUID.randomUUID().toString()
//        quest.scheduledDate = LocalDate.now().plusDays(1)
////        quest.completedAt = System.currentTimeMillis()
//        rq.quests.add(quest)
//        repeatingQuestRepository.save(rq).subscribe()

//        val challenge = Challenge("Hello")
//        challenge.endDate = LocalDate.now().plusDays(2)
//        challenge.quests.add(Quest("Welcome to China", Category.CHORES))
//
//        val repeatingQuest = RepeatingQuest("Hi")
//        repeatingQuest.quests.add(Quest("Hi", Category.LEARNING))
//        challenge.repeatingQuests.add(repeatingQuest)
//
//        RealmChallengeRepository().save(challenge).subscribe()
    }
}