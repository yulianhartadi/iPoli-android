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
import io.ipoli.android.quest.data.Category
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.persistence.RealmQuestRepository
import io.realm.Realm
import org.threeten.bp.LocalDate
import timber.log.Timber

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

        val questRepository = RealmQuestRepository()
        questRepository.save(Quest("Welcome to China", LocalDate.now(), Category.FUN)).subscribe()
    }
}