package io.ipoli.android.common.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import com.bluelinelabs.conductor.Router
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.player.persistence.CouchbasePlayerRepository
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.calendar.dayview.DayViewPresenter
import io.ipoli.android.quest.data.persistence.CouchbaseQuestRepository
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.ipoli.android.quest.usecase.SaveQuestUseCase
import io.ipoli.android.reminder.view.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.view.formatter.TimeUnitFormatter
import io.ipoli.android.reminder.view.picker.ReminderPickerDialogPresenter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import space.traversal.kapsule.HasModules
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/10/17.
 */
interface RepositoryModule {
    val questRepository: QuestRepository
    val playerRepository: PlayerRepository
}

class CouchbaseRepositoryModule : RepositoryModule, Injects<Module> {
    private val database by required { database }
    private val job by required { job }
    override val questRepository = CouchbaseQuestRepository(database, job + CommonPool)
    override val playerRepository = CouchbasePlayerRepository(database, job + CommonPool)
}

interface AndroidModule {
    val layoutInflater: LayoutInflater

    val sharedPreferences: SharedPreferences

    val navigator: Navigator

    val reminderTimeFormatter: ReminderTimeFormatter

    val timeUnitFormatter: TimeUnitFormatter

    val database: Database

    val job: Job
}

class MainAndroidModule(private val context: Context, private val router: Router) : AndroidModule {
    override val layoutInflater: LayoutInflater get() = LayoutInflater.from(context)

    override val sharedPreferences: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(context)

    override val navigator get() = Navigator(router)

    override val reminderTimeFormatter get() = ReminderTimeFormatter(context)

    override val timeUnitFormatter get() = TimeUnitFormatter(context)

    override val database
        get() =
            Database("iPoli", DatabaseConfiguration(context.applicationContext))

    override val job get() = Job()
}

class MainUseCaseModule : UseCaseModule, Injects<Module> {
    private val questRepository by required { questRepository }
    private val playerRepository by required { playerRepository }
    private val job by required { job }
    override val loadScheduleForDateUseCase get() = LoadScheduleForDateUseCase(questRepository, job + CommonPool)
    override val saveQuestUseCase get() = SaveQuestUseCase(questRepository)
}

interface UseCaseModule {
    val loadScheduleForDateUseCase: LoadScheduleForDateUseCase
    val saveQuestUseCase: SaveQuestUseCase
}

interface PresenterModule {
    val dayViewPresenter: DayViewPresenter
    val reminderPickerPresenter: ReminderPickerDialogPresenter
}

class AndroidPresenterModule : PresenterModule, Injects<Module> {
    private val loadScheduleForDateUseCase by required { loadScheduleForDateUseCase }
    private val saveQuestUseCase by required { saveQuestUseCase }
    private val navigator by required { navigator }
    private val reminderTimeFormatter by required { reminderTimeFormatter }
    private val timeUnitFormatter by required { timeUnitFormatter }
    private val job by required { job }
    override val dayViewPresenter get() = DayViewPresenter(loadScheduleForDateUseCase, saveQuestUseCase, job)
    override val reminderPickerPresenter get() = ReminderPickerDialogPresenter(reminderTimeFormatter, timeUnitFormatter)
}

class Module(androidModule: AndroidModule,
             repositoryModule: RepositoryModule,
             useCaseModule: UseCaseModule,
             presenterModule: PresenterModule) :
    AndroidModule by androidModule,
    RepositoryModule by repositoryModule,
    UseCaseModule by useCaseModule,
    PresenterModule by presenterModule,
    HasModules {
    override val modules = setOf(androidModule, repositoryModule, useCaseModule, presenterModule)
}