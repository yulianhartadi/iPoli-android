package io.ipoli.android

import android.app.Application
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.ipoli.android.common.di.AppComponent
import io.ipoli.android.common.di.AppModule
import io.ipoli.android.common.di.DaggerAppComponent
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.repeatingquest.data.Recurrence
import io.ipoli.android.repeatingquest.data.RepeatingQuest
import io.ipoli.android.repeatingquest.persistence.RealmRepeatingQuestRepository
import io.ipoli.android.reward.RealmRewardRepository
import io.ipoli.android.reward.Reward
import io.realm.Realm
import org.threeten.bp.LocalDate
import timber.log.Timber
import java.util.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/17.
 */
class iPoliApp : Application() {

    companion object {
        private var component: AppComponent? = null
        lateinit var refWatcher: RefWatcher

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
        AndroidThreeTen.init(this)
        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this)
        Logger.addLogAdapter(AndroidLogAdapter())
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                Logger.log(priority, tag, message, t)
            }
        })
        refWatcher = LeakCanary.install(this)
//        TinyDancer.create().show(this)

//
//        val rewardRepository = RealmRewardRepository()
//        rewardRepository.save(Reward(name = "Reward 1", description = "desc1", price = 200)).subscribe()

//        val questRepository = RealmQuestRepository()
//        val quest = Quest("Mystery", LocalDate.now(), Category.FUN)
//        quest.setDuration(60)
//        quest.startMinute = 360
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